package beam.agentsim.agents.rideHail

import akka.actor.FSM.Failure
import akka.actor.{ActorRef, Props}
import beam.agentsim.agents.BeamAgent._
import beam.agentsim.agents.PersonAgent.{Moving, Waiting}
import beam.agentsim.agents.TriggerUtils._
import beam.agentsim.agents.modalBehaviors.DrivesVehicle
import beam.agentsim.agents.rideHail.RideHailingAgent.RideHailingAgentData
import beam.agentsim.agents.vehicles.VehicleProtocol.{BecomeDriverSuccess, BecomeDriverSuccessAck}
import beam.agentsim.agents.vehicles.{BeamVehicle, ModifyPassengerScheduleAck, ReservationResponse}
import beam.agentsim.agents.{BeamAgent, InitializeTrigger, PersonAgent}
import beam.agentsim.events.SpaceTime
import beam.agentsim.scheduler.TriggerWithId
import beam.router.BeamRouter.Location
import beam.router.RoutingModel
import beam.router.RoutingModel.{BeamTrip, EmbodiedBeamLeg, EmbodiedBeamTrip}
import beam.sim.{BeamServices, HasServices}
import com.conveyal.r5.transit.TransportNetwork
import org.matsim.api.core.v01.events.{PersonDepartureEvent, PersonEntersVehicleEvent}
import org.matsim.api.core.v01.population.Person
import org.matsim.api.core.v01.{Coord, Id}
import org.matsim.core.api.experimental.events.EventsManager
import org.matsim.vehicles.Vehicle

object RideHailingAgent {
  val idPrefix: String = "rideHailingAgent"

  def props(services: BeamServices, scheduler: ActorRef, transportNetwork: TransportNetwork, eventsManager: EventsManager, rideHailingAgentId: Id[RideHailingAgent], vehicle: BeamVehicle, location: Coord) =
    Props(new RideHailingAgent(rideHailingAgentId, scheduler, vehicle, location, eventsManager, services, transportNetwork))

  case class RideHailingAgentData() extends BeamAgentData

  case object Idle extends BeamAgentState

  case object Traveling extends BeamAgentState

  case class PickupCustomer(confirmation: ReservationResponse, customerId: Id[Person], pickUpLocation: Location, destination: Location, trip2DestPlan: Option[BeamTrip], trip2CustPlan: Option[BeamTrip])

  case class DropOffCustomer(newLocation: SpaceTime)

  def isRideHailingLeg(currentLeg: EmbodiedBeamLeg): Boolean = {
    currentLeg.beamVehicleId.toString.contains("rideHailingVehicle")
  }

  def getRideHailingTrip(chosenTrip: EmbodiedBeamTrip): Vector[RoutingModel.EmbodiedBeamLeg] = {
    chosenTrip.legs.filter(l => isRideHailingLeg(l))
  }

  def isRideHailingTrip(chosenTrip: EmbodiedBeamTrip): Boolean = {
    getRideHailingTrip(chosenTrip).nonEmpty
  }
  def exchangeVehicleId(vehicleId: Id[Vehicle]): Id[Vehicle] ={
    Id.createVehicleId(s"rideHailingVehicle-${vehicleId.toString}")
  }

}

class RideHailingAgent(override val id: Id[RideHailingAgent], val scheduler: ActorRef, vehicle: BeamVehicle, initialLocation: Coord,
                       val eventsManager: EventsManager, val beamServices: BeamServices, val transportNetwork: TransportNetwork)
  extends DrivesVehicle[RideHailingAgentData] {
  override val data: RideHailingAgentData = RideHailingAgentData()
  override def logPrefix(): String = s"RideHailingAgent $id: "

  chainedWhen(Uninitialized) {
    case Event(TriggerWithId(InitializeTrigger(tick), triggerId), _: BeamAgentInfo[RideHailingAgentData]) =>
      vehicle.becomeDriver(self).fold(fa =>
        stop(Failure(s"RideHailingAgent $self attempted to become driver of vehicle ${vehicle.id} " +
          s"but driver ${vehicle.driver.get} already assigned.")), fb => {
        holdTickAndTriggerId(tick,triggerId)
        vehicle.driver.get ! BecomeDriverSuccess(None, vehicle.id)
        vehicle.checkInResource(Some(SpaceTime(initialLocation,tick.toLong)),context.dispatcher)
        eventsManager.processEvent(new PersonDepartureEvent(tick, Id.createPersonId(id), null, "be_a_tnc_driver"))
        eventsManager.processEvent(new PersonEntersVehicleEvent(tick, Id.createPersonId(id), vehicle.id))
        goto(PersonAgent.Waiting)
      })
  }

  override def passengerScheduleEmpty(tick: Double, triggerId: Long) = {
    vehicle.checkInResource(Some(lastVisited),context.dispatcher)
    scheduler ! completed(triggerId)
    goto(Waiting)
  }

  chainedWhen (AnyState) {
    case Event (ModifyPassengerScheduleAck (Some (msgId) ), _) =>
      stay
    case Event(BecomeDriverSuccessAck, _) =>
      val (tick, triggerId) = releaseTickAndTriggerId()
      scheduler ! completed(triggerId)
      stay
    case Event (Finish, _) =>
      stop
  }


  //// BOILERPLATE /////

  when (Waiting) {
  case ev@Event (_, _) =>
  handleEvent (stateName, ev)
  case msg@_ =>
  stop (Failure (s"Unrecognized message $msg") )
}

  when (Moving) {
  case ev@Event (_, _) =>
  handleEvent (stateName, ev)
  case msg@_ =>
  stop (Failure (s"Unrecognized message $msg") )
}

  when (AnyState) {
  case ev@Event (_, _) =>
  handleEvent (stateName, ev)
  case msg@_ =>
  stop (Failure (s"Unrecognized message $msg") )
}

}


