package beam.agentsim.events

import java.util

import beam.agentsim.infrastructure.ParkingStall
import org.matsim.api.core.v01.Id
import org.matsim.api.core.v01.events.Event
import org.matsim.api.core.v01.population.Person
import org.matsim.core.api.internal.HasPersonId
import org.matsim.vehicles.Vehicle


//trait LeavingParkingEventAttrs {
//  val EVENT_TYPE: String = "LeavingParkingEvent"
//  val ATTRIBUTE_PARKING_ID: String = "parkingId"
//  val ATTRIBUTE_SCORE: String = "score"
//}

class LeavingParkingEvent(time: Double, stall: ParkingStall, val score: Double, vehId: Id[Vehicle]) extends Event(time) with LeavingParkingEventAttrs with HasPersonId{

  override def getPersonId: Id[Person] = Id.create(vehId, classOf[Person])

  override def getEventType: String = LeavingParkingEventAttrs.EVENT_TYPE

  override def getAttributes: util.Map[String, String] = {
    val attr: util.Map[String, String] = super.getAttributes

    for{
      stallValues <- stall.stallValues
      parkingId <- stallValues.parkingId
    } yield{
      attr.put(LeavingParkingEventAttrs.ATTRIBUTE_PARKING_ID, parkingId.toString)
    }

    attr.put(LeavingParkingEventAttrs.ATTRIBUTE_SCORE, score.toString)
    attr.put(ParkEventAttrs.ATTRIBUTE_PARKING_TYPE, stall.attributes.parkingType.toString)

    attr
  }
}

//object LeavingParkingEvent extends LeavingParkingEventAttrs{
//}