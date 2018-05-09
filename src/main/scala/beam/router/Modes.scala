package beam.router

import com.conveyal.r5.api.util.{LegMode, TransitModes}
import enumeratum.values._
import org.matsim.api.core.v01.TransportMode

import scala.collection.immutable

/**
  * [[ValueEnum]] containing all of the translations b/w BEAM <==> R5[[LegMode]] MATSim [[TransportMode]].
  *
  * Note: There is an implicit conversion
  *
  * Created by sfeygin on 4/5/17.
  */
object Modes {

  sealed trait BeamMode extends StringEnumEntry {
    override def value: String
    val r5Mode: Option[Either[LegMode, TransitModes]]
    val matsimMode: String
    def isTransit(): Boolean = isR5TransitMode(this)
  }

  object BeamMode extends StringEnum[BeamMode] with StringCirceEnum[BeamMode] {

    override val values: immutable.IndexedSeq[BeamMode] = findValues

    // Driving / Automobile-like (hailed rides are a bit of a hybrid)

    case object CAR extends BeamMode {
      override val value = "car"
      override val r5Mode = Some(Left(LegMode.CAR))
      override val matsimMode: String = TransportMode.car
    }

    case object RIDE_HAIL extends BeamMode {
      override val value = "ride_hailing"
      override val r5Mode = Some(Left(LegMode.CAR))
      override val matsimMode: String = TransportMode.other
    }

    case object EV extends BeamMode {
      override val value = "ev"
      override val r5Mode = Some(Left(LegMode.CAR))
      override val matsimMode: String = TransportMode.other
    }

    // Transit

    case object BUS extends BeamMode {
      override val value = "bus"
      override val r5Mode = Some(Right(TransitModes.BUS))
      override val matsimMode: String = TransportMode.pt
    }

    case object FUNICULAR extends BeamMode {
      override val value = "funicular"
      override val r5Mode = Some(Right(TransitModes.FUNICULAR))
      override val matsimMode: String = TransportMode.pt
    }

    case object GONDOLA extends BeamMode {
      override val value = "gondola"
      override val r5Mode = Some(Right(TransitModes.GONDOLA))
      override val matsimMode: String = TransportMode.pt
    }

    case object CABLE_CAR extends BeamMode {
      override val value = "cable_car"
      override val r5Mode = Some(Right(TransitModes.CABLE_CAR))
      override val matsimMode: String = TransportMode.pt
    }

    case object FERRY extends BeamMode {
      override val value = "ferry"
      override val r5Mode = Some(Right(TransitModes.FERRY))
      override val matsimMode: String = TransportMode.pt
    }

    case object TRANSIT extends BeamMode {
      override val value = "transit"
      override val r5Mode = Some(Right(TransitModes.TRANSIT))
      override val matsimMode: String = TransportMode.pt
    }

    case object RAIL extends BeamMode {
      override val value = "rail"
      override val r5Mode = Some(Right(TransitModes.RAIL))
      override val matsimMode: String = TransportMode.pt
    }

    case object SUBWAY extends BeamMode {
      override val value = "subway"
      override val r5Mode = Some(Right(TransitModes.SUBWAY))
      override val matsimMode: String = TransportMode.pt
    }

    case object TRAM extends BeamMode {
      override val value = "tram"
      override val r5Mode = Some(Right(TransitModes.TRAM))
      override val matsimMode: String = TransportMode.pt
    }

    // Non-motorized

    case object WALK extends BeamMode {
      override val value = "walk"
      override val r5Mode = Some(Left(LegMode.WALK))
      override val matsimMode: String = TransportMode.walk
    }

    case object BIKE extends BeamMode {
      override val value = "bike"
      override val r5Mode = Some(Left(LegMode.BICYCLE))
      override val matsimMode: String = TransportMode.walk
    }

    // Transit-specific non-motorized
    case object LEG_SWITCH extends BeamMode {
      override val value = "leg_switch"
      override val r5Mode = None
      override val matsimMode: String = TransportMode.other
    } // This is kind-of like a transit walk, but not really... best to make leg_switch its own type

    case object WALK_TRANSIT extends BeamMode {
      override val value = "walk_transit"
      override val r5Mode = Some(Right(TransitModes.TRANSIT))
      override val matsimMode: String = TransportMode.transit_walk
    }

    case object DRIVE_TRANSIT extends BeamMode {
      override val value = "drive_transit"
      override val r5Mode = Some(Right(TransitModes.TRANSIT))
      override val matsimMode: String = TransportMode.pt
    }

    case object WAITING extends BeamMode {
      override val value = "waiting"
      override val r5Mode = None
      override val matsimMode: String = TransportMode.other
    }

    val chainBasedModes = Seq(CAR, EV, BIKE)
  }

  def isChainBasedMode(beamMode: BeamMode): Boolean =
    BeamMode.chainBasedModes.contains(beamMode)

  implicit def beamMode2R5Mode(
      beamMode: BeamMode): Either[LegMode, TransitModes] = beamMode.r5Mode.get

  def isR5TransitMode(beamMode: BeamMode): Boolean = {
    beamMode.r5Mode match {
      case Some(Right(_)) =>
        true
      case _ => false
    }
  }

  def isR5LegMode(beamMode: BeamMode): Boolean = {
    beamMode.r5Mode match {
      case Some(Left(_)) =>
        true
      case _ => false
    }
  }

  def isOnStreetTransit(beamMode: BeamMode): Boolean = {
    beamMode.r5Mode match {
      case Some(Left(_)) =>
        false
      case Some(Right(transitMode)) =>
        transitMode match {
          case TransitModes.BUS =>
            true
          case _ =>
            false
        }
      case _ =>
        false
    }
  }

  def mapLegMode(mode: LegMode): BeamMode = mode match {
    case LegMode.BICYCLE | LegMode.BICYCLE_RENT => BeamMode.BIKE
    case LegMode.WALK                           => BeamMode.WALK
    case LegMode.CAR | LegMode.CAR_PARK         => BeamMode.CAR
  }

  def mapTransitMode(mode: TransitModes): BeamMode = mode match {
    case TransitModes.TRANSIT   => BeamMode.TRANSIT
    case TransitModes.SUBWAY    => BeamMode.SUBWAY
    case TransitModes.BUS       => BeamMode.BUS
    case TransitModes.FUNICULAR => BeamMode.FUNICULAR
    case TransitModes.GONDOLA   => BeamMode.GONDOLA
    case TransitModes.CABLE_CAR => BeamMode.CABLE_CAR
    case TransitModes.FERRY     => BeamMode.FERRY
    case TransitModes.RAIL      => BeamMode.RAIL
    case TransitModes.TRAM      => BeamMode.TRAM
  }

  def filterForTransit(modes: Vector[BeamMode]): Vector[BeamMode] =
    modes.filter(mode => isR5TransitMode(mode))

  def filterForStreet(modes: Vector[BeamMode]): Vector[BeamMode] =
    modes.filter(mode => isR5LegMode(mode))

}
