package ytex.kernel.model;

public class SVMClassifierEvaluation extends ClassifierEvaluation {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Double cost;
	String weight;
	Integer degree;
	Double gamma;
	Integer kernel;
	Integer supportVectors;
	Double vcdim;
	
	public Double getVcdim() {
		return vcdim;
	}
	public void setVcdim(Double vcdim) {
		this.vcdim = vcdim;
	}
	public Double getCost() {
		return cost;
	}
	public void setCost(Double cost) {
		this.cost = cost;
	}
	public String getWeight() {
		return weight;
	}
	public void setWeight(String weight) {
		this.weight = weight;
	}
	public Integer getDegree() {
		return degree;
	}
	public void setDegree(Integer degree) {
		this.degree = degree;
	}
	public Double getGamma() {
		return gamma;
	}
	public void setGamma(Double gamma) {
		this.gamma = gamma;
	}
	public Integer getKernel() {
		return kernel;
	}
	public void setKernel(Integer kernel) {
		this.kernel = kernel;
	}
	public Integer getSupportVectors() {
		return supportVectors;
	}
	public void setSupportVectors(Integer supportVectors) {
		this.supportVectors = supportVectors;
	}

}
