package beam.agentsim.agents.choice.mode

import java.io.File
import java.util
import java.util.{LinkedHashMap, Random}

import beam.agentsim.agents.modalBehaviors.ModeChoiceCalculator
import beam.agentsim.agents.choice.logit.MulitnomialLogit
import beam.agentsim.agents.choice.mode.ModeChoiceMultinomialLogit.ModeCostTime
import beam.router.Modes.BeamMode
import beam.router.Modes.BeamMode.{CAR, RIDEHAIL, TRANSIT}
import beam.router.RoutingModel
import beam.router.RoutingModel.EmbodiedBeamTrip
import beam.sim.BeamServices
import beam.utils.DebugLib
import org.jdom.Document
import org.jdom.Element
import org.jdom.JDOMException
import org.jdom.input.SAXBuilder

import scala.collection.JavaConverters._
import scalaz.Digit._1


/**
  * BEAM
  */
class ModeChoiceMultinomialLogit(val beamServices: BeamServices, val model: MulitnomialLogit ) extends ModeChoiceCalculator {

  override def clone(): ModeChoiceCalculator = {
    val  mnl: MulitnomialLogit = this.model.clone()
    new ModeChoiceMultinomialLogit(beamServices,mnl)
  }

  override def apply(alternatives: Vector[EmbodiedBeamTrip]) = {
    val inputData: util.LinkedHashMap[java.lang.String, util.LinkedHashMap[java.lang.String, java.lang.Double]] = new util.LinkedHashMap[java.lang.String, util.LinkedHashMap[java.lang.String, java.lang.Double]]()


    val modeCostTimes = alternatives.map { alt => ModeCostTime(alt.tripClassifier, alt.costEstimate, alt.totalTravelTime) }
    val groupedByMode = (modeCostTimes ++ ModeChoiceMultinomialLogit.defaultAlternatives).sortBy(_.mode.value).groupBy(_.mode)

    val bestInGroup = groupedByMode.map { case (mode, modeCostTimeSegment) =>
      // Which dominates at $18/hr
      modeCostTimeSegment.map { mct => (mct.time / 3600 * 18 + mct.cost.toDouble, mct) }.sortBy(_._1).head._2
    }

    bestInGroup.foreach{ mct =>
      val altData: util.LinkedHashMap[java.lang.String, java.lang.Double] = new util.LinkedHashMap[java.lang.String, java.lang.Double]()
      altData.put("cost",mct.cost.toDouble)
      altData.put("time",mct.time)
      inputData.put(mct.mode.value, altData)
    }

    val chosenMode = model.makeRandomChoice(inputData, new Random())
    model.clear()
    val chosenAlts = alternatives.filter(_.tripClassifier.value.equalsIgnoreCase(chosenMode))

    chosenAlts.isEmpty match {
      case true =>
        None
      case false =>
        Some(chosenAlts.head)
    }
  }

  object BridgeTolls {

    val tollPricesBeamVille = Map[String, Double](
      "1" -> 100,
      "2" -> 200
    )

    // source: https://www.transit.wiki/
    val tollPricesSFBay = Map[String, Double](
      "1191692" -> 5,
      "502" -> 5,
      "998142" -> 5,
      "722556" -> 5,
      "1523426" -> 5,
      "1053032" -> 5,
      "1457468" -> 7,
      "668214" -> 5
    )

    def estimateBrdigesFares(alternatives: Vector[EmbodiedBeamTrip]): Vector[BigDecimal] = {
      var tollPrices: Map[String, Double] = Map();
      if (beamServices.beamConfig.beam.agentsim.simulationName.equalsIgnoreCase("beamville")) {
        tollPrices = tollPricesBeamVille;
      } else if (beamServices.beamConfig.beam.agentsim.simulationName.equalsIgnoreCase("sf-bay")) {
        tollPrices = tollPricesSFBay;
      }

      alternatives.map { alt =>
        alt.tripClassifier match {
          case CAR =>
            BigDecimal(alt.toBeamTrip().legs.map { beamLeg =>
              if (beamLeg.mode.toString.equalsIgnoreCase("CAR")) {
                beamLeg.travelPath.linkIds.filter(tollPrices.contains(_)).map{ linkId =>
                  tollPrices.get(linkId).get
                }.sum
              } else {
                0
              }
            }.sum)
          case _ =>
            BigDecimal(0)
        }
      }

  }}

  //    val altUtilities = for (alt <- altModesAndTimes) yield altUtility(alt._1, alt._2)
//    val sumExpUtilities = altUtilities.foldLeft(0.0)(_ + math.exp(_))
//    val altProbabilities = for (util <- altUtilities) yield math.exp(util) / sumExpUtilities
//    val cumulativeAltProbabilities = altProbabilities.scanLeft(0.0)(_ + _)
//    //TODO replace with RNG in services
//    val randDraw = (new Random()).nextDouble()
//    val chosenIndex = for (i <- 1 until cumulativeAltProbabilities.length if randDraw < cumulativeAltProbabilities(i)) yield i - 1
//    if (chosenIndex.size > 0) {
//      Some(alternatives(chosenIndex.head))
//    } else {
//      None
//    }
//  private def altUtility(mode: BeamMode, travelTime: Double): Double = {
//    val intercept = if(mode.equals(CAR)){ -3.0 }else{ if(mode.equals(RIDEHAIL)){ -5.0}else{0.0} }
//    intercept + -0.001 * travelTime
//  }



}
object ModeChoiceMultinomialLogit {
  case class ModeCostTime(mode: BeamMode, cost: BigDecimal, time: Double)

  val defaultAlternatives = Vector(
    ModeCostTime(BeamMode.WALK,BigDecimal(Double.MaxValue),Double.PositiveInfinity),
    ModeCostTime(BeamMode.CAR,BigDecimal(Double.MaxValue),Double.PositiveInfinity),
    ModeCostTime(BeamMode.RIDEHAIL,BigDecimal(Double.MaxValue),Double.PositiveInfinity),
    ModeCostTime(BeamMode.BIKE,BigDecimal(Double.MaxValue),Double.PositiveInfinity),
    ModeCostTime(BeamMode.TRANSIT,BigDecimal(Double.MaxValue),Double.PositiveInfinity)
  )

  def apply(beamServices: BeamServices): ModeChoiceMultinomialLogit = {
    new ModeChoiceMultinomialLogit(beamServices,ModeChoiceMultinomialLogit.parseInputForMNL(beamServices))
  }

  def parseInputForMNL(beamServices: BeamServices): MulitnomialLogit = {
    val modeChoiceParametersFile = beamServices.beamConfig.beam.agentsim.agents.modalBehaviors.modeChoiceParametersFile
    val builder: SAXBuilder = new SAXBuilder()
    val document: Document = builder.build(new File(modeChoiceParametersFile)).asInstanceOf[Document]
    var theModelOpt: Option[MulitnomialLogit] = None

    document.getRootElement.getChildren.asScala.foreach{child =>
      if(child.asInstanceOf[Element].getChild("className").getValue.toString.equals("ModeChoiceMultinomialLogit")) {
        val rootNode = child.asInstanceOf[Element].getChild("parameters").asInstanceOf[Element].getChild("multinomialLogit").asInstanceOf[Element]
        theModelOpt = Some(MulitnomialLogit.MulitnomialLogitFactory(rootNode))
      }
    }
    theModelOpt match {
      case Some(theModel) =>
        theModel
      case None =>
        throw new RuntimeException(s"Cannot find a mode choice model of type ModeChoiceMultinomialLogit in file: ${modeChoiceParametersFile}")
    }
  }

}
