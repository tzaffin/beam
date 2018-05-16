package beam.agentsim.agents.choice.logit;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * BEAM
 */
public interface AbstractLogit {
    DiscreteProbabilityDistribution evaluateProbabilities(Map<String, Map<String, Double>> inputData);

    String makeRandomChoice(Map<String, Map<String, Double>> inputData, Random rand);

    Double getExpectedMaximumUtility();

    Double getUtilityOfAlternative(Map<String, Map<String, Double>> inputData);

    void clear(); // Delete any state stored for use in evaluating distribution of given inputs
}
