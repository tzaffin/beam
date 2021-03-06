package beam.integration

import beam.sim.BeamHelper
import com.typesafe.config.ConfigValueFactory
import org.scalatest.{Matchers, WordSpecLike}

/**
  * Created by fdariasm on 29/08/2017
  *
  */

class TransitPriceSpec extends WordSpecLike with Matchers with BeamHelper with IntegrationSpecCommon {

  "Running beam with modeChoice ModeChoiceMultinomialLogit and increasing transitPrice value" must {
    "create more entries for mode choice transit as value increases" in {
      val inputTransitPrice = Seq(0.1, 1.0)
      val modeChoice = inputTransitPrice.map(tc => new StartWithCustomConfig(
        baseConfig
          .withValue("beam.agentsim.agents.modalBehaviors.modeChoiceClass", ConfigValueFactory.fromAnyRef("ModeChoiceMultinomialLogit"))
          .withValue("beam.agentsim.tuning.transitPrice", ConfigValueFactory.fromAnyRef(tc))
      ).groupedCount)

      val tc = modeChoice
        .map(_.get("transit"))
        .filter(_.isDefined)
        .map(_.get)

      isOrdered(tc)((a, b) => a >= b) shouldBe true
    }
  }


}
