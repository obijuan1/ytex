package ytex.kernel.model;

public class SemiLClassifierEvaluation extends ClassifierEvaluation {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	int distanceType;
	boolean softLabel;
	boolean normalizedLaplacian;
	double mu;
	double lambda;
	double gamma;
	double percentLabeled;
	public int getDistanceType() {
		return distanceType;
	}
	public void setDistanceType(int distanceType) {
		this.distanceType = distanceType;
	}
	public boolean isSoftLabel() {
		return softLabel;
	}
	public void setSoftLabel(boolean softLabel) {
		this.softLabel = softLabel;
	}
	public boolean isNormalizedLaplacian() {
		return normalizedLaplacian;
	}
	public void setNormalizedLaplacian(boolean normalizedLaplacian) {
		this.normalizedLaplacian = normalizedLaplacian;
	}
	public double getMu() {
		return mu;
	}
	public void setMu(double mu) {
		this.mu = mu;
	}
	public double getLambda() {
		return lambda;
	}
	public void setLambda(double lambda) {
		this.lambda = lambda;
	}
	public double getGamma() {
		return gamma;
	}
	public void setGamma(double gamma) {
		this.gamma = gamma;
	}
	public double getPercentLabeled() {
		return percentLabeled;
	}
	public void setPercentLabeled(double percentLabeled) {
		this.percentLabeled = percentLabeled;
	}
}
