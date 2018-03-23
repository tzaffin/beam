package beam.agentsim.agents.rideHail

import beam.agentsim.agents.BeamAgent.BeamAgentData
import java.util.concurrent.TimeUnit

import akka.actor.FSM.Failure
import akka.actor.{ActorRef, Props}
import akka.pattern._
import akka.util.Timeout
import beam.agentsim
import beam.agentsim.Resource._
import beam.agentsim.ResourceManager.VehicleManager
import beam.agentsim.agents.BeamAgent.BeamAgentData
import beam.agentsim.agents.PersonAgent.Waiting
import beam.agentsim.agents.{PersonAgent, TriggerUtils}
import beam.agentsim.agents.TriggerUtils._
import beam.agentsim.agents.household.HouseholdActor.ReleaseVehicleReservation
import beam.agentsim.agents.modalBehaviors.DrivesVehicle.{GetBeamVehicleResult, StartLegTrigger, VehicleState}
import beam.agentsim.agents.rideHail.RideHailingManager._
import beam.agentsim.agents.vehicles.AccessErrorCodes.{CouldNotFindRouteToCustomer, RideHailVehicleTakenError, UnknownInquiryIdError, UnknownRideHailReservationError}
import beam.agentsim.agents.vehicles.EnergyEconomyAttributes.Powertrain
import beam.agentsim.agents.vehicles.VehicleProtocol.StreetVehicle
import beam.agentsim.agents.vehicles._
import beam.agentsim.events.SpaceTime
import beam.agentsim.events.resources.ReservationError
import beam.agentsim.scheduler.BeamAgentScheduler.{CompletionNotice, ScheduleTrigger}
import beam.agentsim.scheduler.{Trigger, TriggerWithId}
import beam.analysis.plots.{GraphRideHailingRevenue, GraphSurgePricing}
import beam.router.BeamRouter.{Location, RoutingRequest, RoutingResponse}
import beam.router.Modes.BeamMode._
import beam.router.RoutingModel
import beam.router.RoutingModel.{BeamLeg, BeamTime, BeamTrip, DiscreteTime}
import beam.sim.{BeamServices, HasServices}
import com.conveyal.r5.streets.TravelTimeCalculator
import com.eaio.uuid.UUIDGen
import com.google.common.cache.{Cache, CacheBuilder}
import com.sun.xml.internal.bind.v2.TODO
import com.vividsolutions.jts.geom.Envelope
import org.matsim.api.core.v01.network.Link
import org.matsim.api.core.v01.{Coord, Id}
import org.matsim.core.utils.collections.QuadTree
import org.matsim.core.utils.geometry.CoordUtils
import org.matsim.vehicles.Vehicle
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.{Failure, Random, Success}









//TODO: Build RHM from XML to be able to specify different kinds of TNC/Rideshare types and attributes
case class RideHailingManagerData() extends BeamAgentData


// TODO: remove name variable, as not used currently in the code anywhere?
class RideHailingManager(val name: String, val beamServices: BeamServices, val router: ActorRef, val boundingBox: Envelope, val surgePricingManager: RideHailSurgePricingManager) extends VehicleManager with HasServices {

  import scala.collection.JavaConverters._ // TODO: check if still needed

  override val resources: collection.mutable.Map[Id[BeamVehicle], BeamVehicle] = collection.mutable.Map[Id[BeamVehicle], BeamVehicle]()













  // TODO: currently 'DefaultCostPerMile' is not used anywhere in the code, therefore commented it out -> needs to be used!
  // val DefaultCostPerMile = BigDecimal(beamServices.beamConfig.beam.agentsim.agents.rideHailing.defaultCostPerMile)
  val DefaultCostPerMinute = BigDecimal(beamServices.beamConfig.beam.agentsim.agents.rideHailing.defaultCostPerMinute)

  val selfTimerTimoutDuration=10*60 // TODO: set from config
  val radius: Double = 5000

  private implicit val timeout = Timeout(50000, TimeUnit.SECONDS)

  val rideHailIterationHistoryActorRef = context.actorSelection("akka://beam-actor-system/user/RideHailIterationHistoryActor")

  var tncMultiIterationData: TNCMultiIterationData = _

  rideHailIterationHistoryActorRef ! GetWaitingTimes()

//  val future = rideHailIterationHistoryActorRef.ask(GetWaitingTimes())
//  val updateHistoricWaitingTimes=Await.ready(future, timeout.duration).value.get match {
//    case Success(history) => history.asInstanceOf[UpdateHistoricWaitingTimes]
//    case Failure(exception) => throw exception
//  }

 //val updateHistoricWaitingTimes:UpdateHistoricWaitingTimes=future.


  //TODO improve search to take into account time when available
  private val availableRideHailingAgentSpatialIndex = {
    new QuadTree[RideHailingAgentLocation](
      boundingBox.getMinX,
      boundingBox.getMinY,
      boundingBox.getMaxX,
      boundingBox.getMaxY)
  }
  private val inServiceRideHailingAgentSpatialIndex = {
    new QuadTree[RideHailingAgentLocation](
      boundingBox.getMinX,
      boundingBox.getMinY,
      boundingBox.getMaxX,
      boundingBox.getMaxY)
  }
  private val availableRideHailVehicles = collection.concurrent.TrieMap[Id[Vehicle], RideHailingAgentLocation]()
  private val inServiceRideHailVehicles = collection.concurrent.TrieMap[Id[Vehicle], RideHailingAgentLocation]()

  private val vehicleStates = collection.concurrent.TrieMap[Id[Vehicle], VehicleState]()


  // TODO: discuss again, if inheritence might have been a better choice.
  var tncResourceAllocationManager:TNCResourceAllocationManager=new TNCDefaultResourceAllocationManager()  // TODO: initiaize this somewhere
  // TODO: depending on what we select in config, select default or stanford tnc resource allocator
  //beam.agentsim.agents.rideHailing.resourceAllocationManager="STANFORD_ALLOCATION_MANAGER_V1"



  // TODO: move ride hailing inquiries to the resource allocation manager!



  private val pendingModifyPassengerScheduleAcks = collection.concurrent.TrieMap[Id[RideHailingInquiry],
    ReservationResponse]()
  private var lockedVehicles = Set[Id[Vehicle]]()

  private var movedVehilce=false

  private val repositioningVehicleLegs= collection.mutable.TreeMap[Id[Vehicle],(BeamLeg,Double,Long)]()

  def receive = uninitialized

  def uninitialized: Receive = {
    case UpdateHistoricWaitingTimes(data) =>
      tncMultiIterationData = data
      context.become(initialized)
    case m => log.error(s"Unknown message while uninitialized $m")
  }

  def initialized: Receive = {
    case NotifyIterationEnds() =>
      try {
        val graphSurgePricing: GraphSurgePricing = new GraphSurgePricing();
        graphSurgePricing.createGraph(surgePricingManager)

        val graphRideHailingRevenue: GraphRideHailingRevenue = new GraphRideHailingRevenue();
        graphRideHailingRevenue.createGraph(surgePricingManager)
      } catch {
        // print out exceptions, otherwise hidden, leads to difficult debugging
        case e: Exception => e.printStackTrace()
      }

      surgePricingManager.updateRevenueStats()
      surgePricingManager.updateSurgePriceLevels()
      surgePricingManager.incrementIteration()

      sender() ! () // return empty object to blocking caller

    case RegisterResource(vehId: Id[Vehicle]) =>
      resources.put(agentsim.vehicleId2BeamVehicleId(vehId), beamServices.vehicles(vehId))

    case UpdateHistoricWaitingTimes(_) =>
      print()

    case NotifyResourceIdle(vehId: Id[Vehicle], whenWhere) =>
      // TODO: send message to Drivers Vehicle here to get the state of the vehicle
      // perform following update only after we have receive the following message back.

      updateLocationOfAgent(vehId, whenWhere, false)


    case vehicleState @ VehicleState(vehicleId: Id[Vehicle], location: SpaceTime, stateOfCharge: Double, batteryCapacityInJoules: Double, powertrain: Powertrain) =>

      // TODO: fix compile error
      vehicleStates.put(vehicleId,vehicleState)

    case NotifyResourceInUse(vehId: Id[Vehicle], whenWhere) =>

      // TODO: in reality this message is never sent in code anywhere - think about how to handle this.
      updateLocationOfAgent(vehId, whenWhere, false)

    case CheckInResource(vehicleId: Id[Vehicle], availableIn: Option[SpaceTime]) =>
      resources.get(agentsim.vehicleId2BeamVehicleId(vehicleId)).orElse(beamServices.vehicles.get(vehicleId)).get.driver.foreach(driver => {
        val rideHailingAgentLocation = RideHailingAgentLocation(driver, vehicleId, availableIn.get)
        makeAvailable(rideHailingAgentLocation)
        sender ! CheckInSuccess
      })

    case RepositionResponse(rnd1, rideHailingAgent2CustomerResponse,tick,triggerId) =>

      val timesToCustomer: Vector[Long] = rideHailingAgent2CustomerResponse.itineraries.map(t => t.totalTravelTime)

      val itins2Cust = rideHailingAgent2CustomerResponse.itineraries.filter(x => x.tripClassifier.equals(RIDE_HAIL))

        val modRHA2Cust = itins2Cust.map(l => l.copy(legs = l.legs.map(c => c.copy(asDriver = true))))

        val rideHailingAgent2CustomerResponseMod = RoutingResponse(modRHA2Cust)


      val passengerSchedule = PassengerSchedule()
      passengerSchedule.addLegs(rideHailingAgent2CustomerResponse.itineraries.head.toBeamTrip.legs)
      rnd1.rideHailAgent ! ModifyPassengerSchedule(passengerSchedule, Some(Id.create("reposition-" + rnd1.vehicleId,classOf[String])))


      repositioningVehicleLegs.put(rnd1.vehicleId,(rideHailingAgent2CustomerResponse.itineraries.head.toBeamTrip.legs.head,tick,triggerId))

    case TriggerWithId(RepositioningTimer(tick), triggerId) => {

      if (!movedVehilce) {
      tryMove(tick, triggerId)
        movedVehilce=true
    }


      tncResourceAllocationManager.allocatePassengers(null)

      tncResourceAllocationManager.repositionIdleVehicles()

      // move all idling vehicles


      //print()
      // TODO: add initial timer

      // TODO: reposition vehicles

      //  beamServices.schedulerRef ! RepositioningTimer -> make trigger

      // get two random idling TNCs
      // move one TNC to the other.


      //      updateLocationOfAgent(rnd1, )
      // TODO: schedule next Timer
      // start relocate?

      // end relocate?

   //   repositionVehicle(null,null,null)


      val timerTrigger = RepositioningTimer(tick + selfTimerTimoutDuration)
      val timerMessage = ScheduleTrigger(timerTrigger, self)
      beamServices.schedulerRef ! timerMessage
      beamServices.schedulerRef ! TriggerUtils.completed(triggerId)
    }


    case UpdateResource(resourceId:Id[BeamVehicle], resource: BeamVehicle) => {
      update(resourceId,resource)
    }

    case GetResource(resourceId:Id[BeamVehicle]) => {
      sender() ! GetResourceResult(findResource(resourceId).get)
    }

    case GetBeamVehicleResult(beamVehicle:BeamVehicle) => {
      update(beamVehicle.id, beamVehicle)
    }


    case CheckOutResource(_) =>
      // Because the RideHail Manager is in charge of deciding which specific vehicles to assign to customers, this should never be used
      throw new RuntimeException("Illegal use of CheckOutResource, RideHailingManager is responsible for checking out vehicles in fleet.")


    case RideHailingInquiry(inquiryId, personId, customerPickUp, departAt, destination) =>
      val customerAgent = sender()


      // TODO: implement first before uncommenting
      //customerAgent ! RideHailingInquiryResponse(inquiryId, Vector(tncResourceAllocationManager.getNonBindingTravelProposalAsEstimate(null,null)))
        // TODO: push all code into tncResourceAllocationManager.getNonBindingTravelProposalAsEstimate related to getting travel proposal (default implementation)


      //

      getClosestRideHailingAgent(customerPickUp, radius) match {
        case Some((rideHailingLocation, shortDistanceToRideHailingAgent)) =>
          lockedVehicles += rideHailingLocation.vehicleId

          // Need to have this dispatcher here for the future execution below
          import context.dispatcher

          val (futureRideHailingAgent2CustomerResponse, futureRideHailing2DestinationResponse) =
            createCustomerInquiryResponse(personId, customerPickUp, departAt, destination, rideHailingLocation)

          for {
            rideHailingAgent2CustomerResponse <- futureRideHailingAgent2CustomerResponse.mapTo[RoutingResponse]
            rideHailing2DestinationResponse <- futureRideHailing2DestinationResponse.mapTo[RoutingResponse]
          } {
            // TODO: could we just call the code, instead of sending the message here?
            self ! RoutingResponses(customerAgent, inquiryId, personId, customerPickUp, departAt, rideHailingLocation, shortDistanceToRideHailingAgent, rideHailingAgent2CustomerResponse, rideHailing2DestinationResponse)
          }
        case None =>
          // no rides to hail
          customerAgent ! RideHailingInquiryResponse(inquiryId, Vector(), error = Option(CouldNotFindRouteToCustomer))
      }

    case RoutingResponses(customerAgent, inquiryId, personId, customerPickUp, departAt, rideHailingLocation, shortDistanceToRideHailingAgent, rideHailingAgent2CustomerResponse, rideHailing2DestinationResponse) =>
      val timesToCustomer: Vector[Long] = rideHailingAgent2CustomerResponse.itineraries.map(t => t.totalTravelTime)
      // TODO: Find better way of doing this error checking than sentry value
      val timeToCustomer = if (timesToCustomer.nonEmpty) {
        timesToCustomer.min
      } else Long.MaxValue
      // TODO: Do unit conversion elsewhere... use squants or homegrown unit conversions, but enforce
      val rideHailingFare = DefaultCostPerMinute / 60.0 * surgePricingManager.getSurgeLevel(customerPickUp, departAt.atTime.toDouble)

      val customerPlans2Costs: Map[RoutingModel.EmbodiedBeamTrip, BigDecimal] = rideHailing2DestinationResponse.itineraries.map(t => (t, rideHailingFare * t.totalTravelTime)).toMap
      val itins2Cust = rideHailingAgent2CustomerResponse.itineraries.filter(x => x.tripClassifier.equals(RIDE_HAIL))
      val itins2Dest = rideHailing2DestinationResponse.itineraries.filter(x => x.tripClassifier.equals(RIDE_HAIL))
      if (timeToCustomer < Long.MaxValue && customerPlans2Costs.nonEmpty && itins2Cust.nonEmpty && itins2Dest.nonEmpty) {
        val (customerTripPlan, cost) = customerPlans2Costs.minBy(_._2)

        //TODO: include customerTrip plan in response to reuse( as option BeamTrip can include createdTime to check if the trip plan is still valid
        //TODO: we response with collection of TravelCost to be able to consolidate responses from different ride hailing companies

        val modRHA2Cust = itins2Cust.map(l => l.copy(legs = l.legs.map(c => c.copy(asDriver = true))))
        val modRHA2Dest = itins2Dest.map(l => l.copy(legs = l.legs.zipWithIndex.map(c => c._1.copy(asDriver = c._1.beamLeg.mode == WALK,
          unbecomeDriverOnCompletion = c._2 == 2,
          beamLeg = c._1.beamLeg.copy(startTime = c._1.beamLeg.startTime + timeToCustomer),
          cost = if (c._1.beamLeg == l.legs(1).beamLeg) {
            cost
          } else {
            0.0
          }
        ))))

        val rideHailingAgent2CustomerResponseMod = RoutingResponse(modRHA2Cust)
        val rideHailing2DestinationResponseMod = RoutingResponse(modRHA2Dest)

        val travelProposal = TravelProposal(rideHailingLocation, timeToCustomer, cost, Option(FiniteDuration
        (customerTripPlan.totalTravelTime, TimeUnit.SECONDS)), rideHailingAgent2CustomerResponseMod,
          rideHailing2DestinationResponseMod)
        pendingInquiries.put(inquiryId, (travelProposal, modRHA2Dest.head.toBeamTrip()))
        log.debug(s"Found ride to hail for  person=$personId and inquiryId=$inquiryId within " +
          s"$shortDistanceToRideHailingAgent meters, timeToCustomer=$timeToCustomer seconds and cost=$$$cost")

        customerAgent ! RideHailingInquiryResponse(inquiryId, Vector(travelProposal))
      } else {
        log.debug(s"Router could not find route to customer person=$personId for inquiryId=$inquiryId")
        lockedVehicles -= rideHailingLocation.vehicleId

        customerAgent ! RideHailingInquiryResponse(inquiryId, Vector(), error = Option(CouldNotFindRouteToCustomer))
      }

    case ReserveRide(inquiryId, vehiclePersonIds, customerPickUp, departAt, destination) =>

      if (tncResourceAllocationManager.bufferReservationRequests()){

      } else {


        if (pendingInquiries.asMap.containsKey(inquiryId)) {
          tncResourceAllocationManager.allocatePassenger(inquiryId,surgePricingManager, departAt)



        } else {
          sender() ! ReservationResponse(Id.create(inquiryId.toString, classOf[ReservationRequest]), Left
          (UnknownInquiryIdError))
        }



      }


    case ModifyPassengerScheduleAck(inquiryIDOption) =>


      // TODO: double check if this is correct
      if (!inquiryIDOption.get.toString.startsWith("repo")){
        completeReservation(Id.create(inquiryIDOption.get.toString, classOf[RideHailingInquiry]))
      } else {
        val rideHailId=Id.create(inquiryIDOption.get.toString.replaceAll("reposition-",""),classOf[Vehicle]);
        val (leg, tick,triggerId)=repositioningVehicleLegs.get(rideHailId).get
        sender() ! TriggerWithId(StartLegTrigger(tick, leg), triggerId)
      }




    case ReleaseVehicleReservation(_, vehId) =>
      lockedVehicles -= vehId

    case msg =>
      log.warn(s"unknown message received by RideHailingManager $msg")


  }




  private def repositionVehicle(legs: Seq[BeamLeg], inquiryId: Id[RideHailingManager.RideHailingInquiry],rideHailAgent: ActorRef):Unit={
    val passengerSchedule = PassengerSchedule()
    passengerSchedule.addLegs(legs)
    rideHailAgent ! ModifyPassengerSchedule(passengerSchedule, Some(inquiryId))


  }



  private def tryMove(tick:Double,triggerId: Long)={

        val rnd = new Random
        val availableKeyset = availableRideHailVehicles.keySet.toArray
        implicit val timeout: Timeout = Timeout(50000, TimeUnit.SECONDS)
        import context.dispatcher
        if(availableKeyset.size > 1) {

          //TODO: check value of availableRideHailVehicles in more detail! -> why are values zero?


          val idRnd1 = Id.create(availableKeyset(0), classOf[Vehicle])
          val idRnd2 = Id.create(availableKeyset(1), classOf[Vehicle])



       //   val idRnd1 =Id.create("rideHailingVehicle-person=13",classOf[Vehicle])
       //   val idRnd2 =Id.create("rideHailingVehicle-person=16",classOf[Vehicle])

          //val idRnd1 = availableKeyset.apply(rnd.nextInt(availableKeyset.size))
          //val idRnd2 = availableKeyset
          //  .filterNot(_.equals(idRnd1))
          //  .apply(rnd.nextInt(availableKeyset.size - 1))

          for{
            rnd1 <- availableRideHailVehicles.get(idRnd1)
            rnd2 <- availableRideHailVehicles.get(idRnd2)
          } yield {
            val departureTime: BeamTime = DiscreteTime(tick.toInt)


            val rideHailingVehicl1 = StreetVehicle(idRnd1, SpaceTime(
              (rnd1.currentLocation.loc, tick.toLong)), CAR, asDriver = false)


            val futureRnd1AgentResponse = router ? RoutingRequest(
              rnd1.currentLocation.loc, rnd2.currentLocation.loc, departureTime, Vector(), Vector(rideHailingVehicl1)) //TODO what should go in vectors
            // get route from customer to destination
            // val futureRnd2AgentResponse  = router ? RoutingRequest(
            //   rnd2.currentLocation.loc, rnd1.currentLocation.loc, departureTime, Vector(), Vector()) //TODO what should go in vectors
            for{
              rnd1Response <- futureRnd1AgentResponse.mapTo[RoutingResponse]
              //  rnd2Response <- futureRnd2AgentResponse.mapTo[RoutingResponse]
            } yield {
              self ! RepositionResponse(rnd2, rnd1Response,tick,triggerId)
            }
          }
        }


    }






  private def createCustomerInquiryResponse(personId: Id[PersonAgent], customerPickUp: Location, departAt: BeamTime, destination: Location, rideHailingLocation: RideHailingAgentLocation): (Future[Any], Future[Any]) = {
    val customerAgentBody = StreetVehicle(Id.createVehicleId(s"body-$personId"), SpaceTime((customerPickUp,
      departAt.atTime)), WALK, asDriver = true)
    val rideHailingVehicleAtOrigin = StreetVehicle(rideHailingLocation.vehicleId, SpaceTime(
      (rideHailingLocation.currentLocation.loc, departAt.atTime)), CAR, asDriver = false)
    val rideHailingVehicleAtPickup = StreetVehicle(rideHailingLocation.vehicleId, SpaceTime((customerPickUp,
      departAt.atTime)), CAR, asDriver = false)

    //TODO: Error handling. In the (unlikely) event of a timeout, this RideHailingManager will silently be
    //TODO: restarted, and probably someone will wait forever for its reply.
    implicit val timeout: Timeout = Timeout(50000, TimeUnit.SECONDS)

    // get route from ride hailing vehicle to customer
    val futureRideHailingAgent2CustomerResponse = router ? RoutingRequest(rideHailingLocation
          .currentLocation.loc, customerPickUp, departAt, Vector(), Vector(rideHailingVehicleAtOrigin))
    //XXXX: customer trip request might be redundant... possibly pass in info

    // get route from customer to destination
    val futureRideHailing2DestinationResponse = router ? RoutingRequest(customerPickUp, destination, departAt, Vector(), Vector(customerAgentBody, rideHailingVehicleAtPickup))
    (futureRideHailingAgent2CustomerResponse, futureRideHailing2DestinationResponse)
  }


  private def updateLocationOfAgent(vehicleId: Id[Vehicle], whenWhere: SpaceTime, isAvailable: Boolean) = {
    if (isAvailable) {
      availableRideHailVehicles.get(vehicleId) match {
        case Some(prevLocation) =>
          val newLocation = prevLocation.copy(currentLocation = whenWhere)
          availableRideHailingAgentSpatialIndex.remove(prevLocation.currentLocation.loc.getX, prevLocation.currentLocation.loc.getY, prevLocation)
          availableRideHailingAgentSpatialIndex.put(newLocation.currentLocation.loc.getX, newLocation.currentLocation.loc.getY, newLocation)
          availableRideHailVehicles.put(newLocation.vehicleId, newLocation)
        case None =>
      }
    } else {
      inServiceRideHailVehicles.get(vehicleId) match {
        case Some(prevLocation) =>
          val newLocation = prevLocation.copy(currentLocation = whenWhere)
          inServiceRideHailingAgentSpatialIndex.remove(prevLocation.currentLocation.loc.getX, prevLocation.currentLocation.loc.getY, prevLocation)
          inServiceRideHailingAgentSpatialIndex.put(newLocation.currentLocation.loc.getX, newLocation.currentLocation.loc.getY, newLocation)
          inServiceRideHailVehicles.put(newLocation.vehicleId, newLocation)
        case None =>
      }
    }
  }

  private def makeAvailable(agentLocation: RideHailingAgentLocation) = {
    availableRideHailVehicles.put(agentLocation.vehicleId, agentLocation)
    availableRideHailingAgentSpatialIndex.put(agentLocation.currentLocation.loc.getX,
      agentLocation.currentLocation.loc.getY, agentLocation)
    inServiceRideHailVehicles.remove(agentLocation.vehicleId)
    inServiceRideHailingAgentSpatialIndex.remove(agentLocation.currentLocation.loc.getX,
      agentLocation.currentLocation.loc.getY, agentLocation)
  }

  private def putIntoService(agentLocation: RideHailingAgentLocation) = {
    availableRideHailVehicles.remove(agentLocation.vehicleId)
    availableRideHailingAgentSpatialIndex.remove(agentLocation.currentLocation.loc.getX,
      agentLocation.currentLocation.loc.getY, agentLocation)
    inServiceRideHailVehicles.put(agentLocation.vehicleId, agentLocation)
    inServiceRideHailingAgentSpatialIndex.put(agentLocation.currentLocation.loc.getX,
      agentLocation.currentLocation.loc.getY, agentLocation)
  }





  private def completeReservation(inquiryId: Id[RideHailingInquiry]): Unit = {
    pendingModifyPassengerScheduleAcks.remove(inquiryId) match {
      case Some(response) =>
        log.debug(s"Completed reservation for $inquiryId")
        val customerRef = beamServices.personRefs(response.response.right.get.passengerVehiclePersonId.personId)
        customerRef ! response
      case None =>
        log.error(s"Vehicle was reserved by another agent for inquiry id $inquiryId")
        sender() ! ReservationResponse(Id.create(inquiryId.toString, classOf[ReservationRequest]), Left
        (RideHailVehicleTakenError))
    }

  }

  private def getClosestRideHailingAgent(pickupLocation: Coord, radius: Double): Option[(RideHailingAgentLocation,
    Double)] = {
    val nearbyRideHailingAgents = availableRideHailingAgentSpatialIndex.getDisk(pickupLocation.getX, pickupLocation.getY,
      radius).asScala.toVector
    val distances2RideHailingAgents = nearbyRideHailingAgents.map(rideHailingAgentLocation => {
      val distance = CoordUtils.calcProjectedEuclideanDistance(pickupLocation, rideHailingAgentLocation
        .currentLocation.loc)
      (rideHailingAgentLocation, distance)
    })
    //TODO: Possibly get multiple taxis in this block
    distances2RideHailingAgents.filterNot(x => lockedVehicles(x._1.vehicleId)).sortBy(_._2).headOption
  }


  def getAllTNCVehicleStates():Vector[VehicleState] = {
    vehicleStates.values.toVector
  }

  def getIdlingTNCVehicleStates():Vector[VehicleState] = {
    vehicleStates.filterKeys(availableRideHailVehicles.contains(_)).values.toVector
  }


  def getEstimatedLinkTravelTimesWithAdditionalLoad(linkLoads:Vector[DynamicRouteLoad]):TravelTimeCalculator = {
    // TODO: we need have access to the TravelTimeCalculator or something similar for this

    // create a priority queue, which goes through route with lowest DynamicRouteLoad (peel off which already considered) and puts its load in
    // right bin


    // start with a simple model, e.g. which takes density of current and outgoing links into account for calculation of the speed on the link.

      // use some simple function to start with.


    // the object we give out should be able to calculate


    // first add loads dynamically (take travel time per link into account).

    ???
  }

  def assignTNC(inquiryId: RideHailingInquiry, vehicleId:Id[Vehicle]) =





    ??? // just do what implementation is doing at the moment
  // move code from reserveRide in here?

  def moveIdleTNCTo(vehicleId: Id[Vehicle], coord: Coord) = ???

  // TODO: queue all quries which arrive before timeout

}

/**
  * BEAM
  */

case class VehicleState(vehicleId: Id[Vehicle], location: SpaceTime, stateOfCharge: Double, batteryCapacityInkWh: Double, energyConsumptionPerMeter: Double)

case class DynamicRouteLoad(startTime:Double, linkIds:Vector[Id[Link]])



object RideHailingManager {
  val RIDE_HAIL_MANAGER = "RideHailingManager";
  val log: Logger = LoggerFactory.getLogger(classOf[RideHailingManager])

  def nextRideHailingInquiryId: Id[RideHailingInquiry] = Id.create(UUIDGen.createTime(UUIDGen.newTime()).toString,
    classOf[RideHailingInquiry])

  case class NotifyIterationEnds()

  case class RideHailingInquiry(inquiryId: Id[RideHailingInquiry], customerId: Id[PersonAgent],
                                pickUpLocation: Location, departAt: BeamTime, destination: Location)

  case class TravelProposal(rideHailingAgentLocation: RideHailingAgentLocation, timesToCustomer: Long,
                            estimatedPrice: BigDecimal, estimatedTravelTime: Option[Duration],
                            responseRideHailing2Pickup: RoutingResponse, responseRideHailing2Dest: RoutingResponse)

  case class RideHailingInquiryResponse(inquiryId: Id[RideHailingInquiry], proposals: Seq[TravelProposal],
                                        error: Option[ReservationError] = None)

  case class ReserveRide(inquiryId: Id[RideHailingInquiry], customerIds: VehiclePersonId, pickUpLocation: Location,
                         departAt: BeamTime, destination: Location)

  private case class RoutingResponses(customerAgent: ActorRef, inquiryId: Id[RideHailingInquiry],
                                      personId: Id[PersonAgent], customerPickUp: Location,departAt:BeamTime, rideHailingLocation: RideHailingAgentLocation,
                                      shortDistanceToRideHailingAgent: Double,
                                      rideHailingAgent2CustomerResponse: RoutingResponse,
                                      rideHailing2DestinationResponse: RoutingResponse)

  case class ReserveRideResponse(inquiryId: Id[RideHailingInquiry], data: Either[ReservationError, RideHailConfirmData])

  case class RideHailConfirmData(rideHailAgent: ActorRef, customerId: Id[PersonAgent], travelProposal: TravelProposal)

  case class RegisterRideAvailable(rideHailingAgent: ActorRef, vehicleId: Id[Vehicle], availableSince: SpaceTime)

  case class RegisterRideUnavailable(ref: ActorRef, location: Coord)

  case class RideHailingAgentLocation(rideHailAgent: ActorRef, vehicleId: Id[Vehicle], currentLocation: SpaceTime)

  case object RideUnavailableAck

  case object RideAvailableAck

  case class RepositioningTimer(tick: Double) extends Trigger

  case class RepositionResponse(rnd1: RideHailingAgentLocation,
                                rnd1Response: RoutingResponse, tick:Double,triggerId: Long)


  def props(name: String, services: BeamServices, router: ActorRef, boundingBox: Envelope, surgePricingManager: RideHailSurgePricingManager) = {
    Props(new RideHailingManager(name, services, router, boundingBox,surgePricingManager))
  }
}