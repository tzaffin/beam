package beam.agentsim.agents.choice.logit;

public class NestedLogitData {
	private String nestName;
  private Double elasticity = 1.0d;
  private double expectedMaximumUtility =  Double.NaN;
	private UtilityFunction utility;
	
//	public NestedLogitData(Double elasticity, UtilityFunction utility) {
//		this.elasticity = elasticity;
//		this.utility = utility;
//	}
//
//  public NestedLogitData() {
//  }

  String getNestName() {
    return nestName;
  }

  void setNestName(String nestName) {
    this.nestName = nestName;
  }

  Double getElasticity() {
		return elasticity;
	}

	void setElasticity(Double elasticity) {
		this.elasticity = elasticity;
	}

	public UtilityFunction getUtility() {
		return utility;
	}

	public void setUtility(UtilityFunction utility) {
		this.utility = utility;
	}

	public String toString(){
		return nestName;
	}

	Double getExpectedMaxUtility() {
		return expectedMaximumUtility;
	}

	void setExpectedMaxUtility(Double expectedMaximumUtility) {
		this.expectedMaximumUtility = expectedMaximumUtility;
	}

}
