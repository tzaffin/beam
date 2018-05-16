package beam.agentsim.agents

import akka.actor.FSM.Failure
import akka.actor.{ActorRef, Props}
import akka.cluster.sharding.ShardRegion
import akka.cluster.sharding.ShardRegion.Passivate
import beam.agentsim.agents.BeamAgent._
import beam.agentsim.agents.PersonAgent.{
  DrivingData,
  PassengerScheduleEmpty,
  VehicleStack,
  WaitingToDrive
}
import beam.agentsim.agents.TransitDriverAgent.{
  EmptyDriverData,
  InitTransitDrive,
  TransitDriverData,
  TransitInitiated
}
import beam.agentsim.agents.modalBehaviors.DrivesVehicle
import beam.agentsim.agents.modalBehaviors.DrivesVehicle.StartLegTrigger
import beam.agentsim.agents.vehicles.{BeamVehicle, PassengerSchedule}
import beam.agentsim.scheduler.BeamAgentScheduler.{
  CompletionNotice,
  IllegalTriggerGoToError,
  ScheduleTrigger
}
import beam.agentsim.scheduler.TriggerWithId
import beam.router.RoutingModel.BeamLeg
import beam.sim.BeamServices
import com.conveyal.r5.transit.TransportNetwork
import org.matsim.api.core.v01.Id
import org.matsim.api.core.v01.events.{
  PersonDepartureEvent,
  PersonEntersVehicleEvent
}
import org.matsim.core.api.experimental.events.EventsManager
import org.matsim.vehicles.Vehicle

/**
  * BEAM
  */
object TransitDriverAgent {
  def props(scheduler: ActorRef,
            services: BeamServices,
            transportNetwork: TransportNetwork,
            eventsManager: EventsManager): Props = {
    Props(
      new TransitDriverAgent(scheduler,
                             services,
                             transportNetwork,
                             eventsManager))
  }

  final case class TransitDataEnvelope(transitDriverId: Id[Vehicle],
                                       payload: Any)

  case object EmptyDriverData extends DrivingData {
    override def currentVehicle: VehicleStack = Vector()
    override def passengerSchedule: PassengerSchedule = PassengerSchedule()
    override def currentLegPassengerScheduleIndex: Int = 0
    override def withPassengerSchedule(
        newPassengerSchedule: PassengerSchedule): DrivingData = EmptyDriverData

    override def withCurrentLegPassengerScheduleIndex(
        currentLegPassengerScheduleIndex: Int): DrivingData = EmptyDriverData
  }

  case class TransitDriverData(vehicle: BeamVehicle,
                               legs: Seq[BeamLeg],
                               currentVehicle: VehicleStack = Vector(),
                               passengerSchedule: PassengerSchedule =
                                 PassengerSchedule(),
                               currentLegPassengerScheduleIndex: Int = 0)
      extends DrivingData {
    override def withPassengerSchedule(
        newPassengerSchedule: PassengerSchedule): DrivingData =
      copy(passengerSchedule = newPassengerSchedule)

    override def withCurrentLegPassengerScheduleIndex(
        currentLegPassengerScheduleIndex: Int): DrivingData =
      copy(currentLegPassengerScheduleIndex = currentLegPassengerScheduleIndex)
  }

  case class InitTransitDrive(transitVehId: Id[Vehicle],
                              vehicle: BeamVehicle,
                              legs: Seq[BeamLeg])
  case class TransitInitiated(ref: ActorRef)

  private val numberOfShards = 2
  val extractEntityId: ShardRegion.ExtractEntityId = {
    case TransitDataEnvelope(id, payload) =>
      (createAgentIdFromVehicleId(id).toString, payload)
  }
  val extractShardId: ShardRegion.ExtractShardId = {
    case TransitDataEnvelope(id, _) => (id.hashCode() % numberOfShards).toString
  }

  private def createAgentIdFromVehicleId(
      transitVehicle: Id[Vehicle]): Id[TransitDriverAgent] = {
    Id.create("TransitDriverAgent-" + BeamVehicle.noSpecialChars(
                transitVehicle.toString),
              classOf[TransitDriverAgent])
  }
}

class TransitDriverAgent(val scheduler: ActorRef,
                         val beamServices: BeamServices,
                         val transportNetwork: TransportNetwork,
                         val eventsManager: EventsManager)
    extends DrivesVehicle[DrivingData] {
  override val id: Id[TransitDriverAgent] =
    Id.create(self.path.name, classOf[TransitDriverAgent])

  override def logPrefix(): String = s"TransitDriverAgent:$id "

  startWith(Uninitialized, EmptyDriverData)

  when(Uninitialized) {
    case Event(InitTransitDrive(transitVehId, vehicle, legs),
               _ @EmptyDriverData) =>
      logDebug(s" $id has been created, updating data")
      beamServices.vehicles += (transitVehId -> vehicle)
      stay() using TransitDriverData(vehicle, legs) replying TransitInitiated(
        self)
    case Event(TriggerWithId(InitializeTrigger(tick), triggerId),
               data: TransitDriverData) =>
      logInfo(s" $id has been initialized, going to Waiting state")
      data.vehicle
        .becomeDriver(self)
        .fold(
          fa => {
            context.parent ! Passivate(
              stopMessage = stop(Failure(
                s"BeamAgent $id attempted to become driver of vehicle $id " +
                  s"but driver ${data.vehicle.driver.get} already assigned.")))
            stay
          },
          fb => {
            eventsManager.processEvent(
              new PersonDepartureEvent(tick,
                                       Id.createPersonId(id),
                                       null,
                                       "be_a_transit_driver"))
            eventsManager.processEvent(
              new PersonEntersVehicleEvent(tick,
                                           Id.createPersonId(id),
                                           data.vehicle.id))
            val schedule = data.passengerSchedule.addLegs(data.legs)
            goto(WaitingToDrive) using data
              .copy(currentVehicle = Vector(data.vehicle.id))
              .withPassengerSchedule(schedule)
              .asInstanceOf[TransitDriverData] replying
              CompletionNotice(
                triggerId,
                Vector(
                  ScheduleTrigger(
                    StartLegTrigger(schedule.schedule.firstKey.startTime,
                                    schedule.schedule.firstKey),
                    self)))
          }
        )
  }

  when(PassengerScheduleEmpty) {
    case Event(PassengerScheduleEmptyMessage(_), _) =>
      val (_, triggerId) = releaseTickAndTriggerId()
      scheduler ! CompletionNotice(triggerId)
      context.parent ! Passivate(stopMessage = Stop)
      stay
  }

  val myUnhandled: StateFunction = {
    case Event(IllegalTriggerGoToError(reason), _) =>
      context.parent ! Passivate(stopMessage = Failure(reason))
      stay
    case Event(Finish, _) =>
      context.parent ! Passivate(stopMessage = Stop)
      stay
    case Event(Stop, _) =>
      stop
    case Event(Failure(reason), _) =>
      stop(Failure(reason))
  }

  whenUnhandled(drivingBehavior.orElse(myUnhandled))

}
