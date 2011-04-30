package ytex.kernel.model;

/**
 * summary IR statistics. useful especially for lame databases like mysql.
 * 
 * @author vijay
 * 
 */
public class ClassifierEvaluationIRStat {
	ClassifierEvaluation classifierEvaluation;
	int classifierEvaluationIRStatId;
	double f1;
	int fn;
	int fp;
	int irClassId;
	double npv;
	double ppv;
	double sensitivity;
	double specificity;
	int tn;
	int tp;

	public ClassifierEvaluationIRStat() {
		super();
	}

	public ClassifierEvaluationIRStat(
			ClassifierEvaluation classifierEvaluation, Integer irClassId,
			int tp, int tn, int fp, int fn) {
		super();
		this.classifierEvaluation = classifierEvaluation;
		this.irClassId = irClassId;
		this.tp = tp;
		this.tn = tn;
		this.fp = fp;
		this.fn = fn;
		this.ppv = tp + fp > 0 ? ((double) tp) / (double) (tp + fp) : 0;
		this.npv = tn + fn > 0 ? ((double) tn) / (double) (tn + fn) : 0;
		this.sensitivity = tp + fn > 0 ? ((double) tp) / (double) (tp + fn) : 0;
		this.specificity = tn + fp > 0 ? ((double) tn) / (double) (tn + fp) : 0;
		this.f1 = ppv + sensitivity > 0 ? 2 * ppv * sensitivity
				/ (ppv + sensitivity) : 0;
	}

	public ClassifierEvaluation getClassifierEvaluation() {
		return classifierEvaluation;
	}

	public int getClassifierEvaluationIRStatId() {
		return classifierEvaluationIRStatId;
	}

	public double getF1() {
		return f1;
	}

	public int getFn() {
		return fn;
	}

	public int getFp() {
		return fp;
	}

	public int getIrClassId() {
		return irClassId;
	}

	public double getNpv() {
		return npv;
	}

	public double getPpv() {
		return ppv;
	}

	public double getSensitivity() {
		return sensitivity;
	}

	public double getSpecificity() {
		return specificity;
	}

	public int getTn() {
		return tn;
	}

	public int getTp() {
		return tp;
	}

	public void setClassifierEvaluation(
			ClassifierEvaluation classifierEvaluation) {
		this.classifierEvaluation = classifierEvaluation;
	}

	public void setClassifierEvaluationIRStatId(int classifierEvaluationIRStatId) {
		this.classifierEvaluationIRStatId = classifierEvaluationIRStatId;
	}

	public void setF1(double f1) {
		this.f1 = f1;
	}

	public void setFn(int fn) {
		this.fn = fn;
	}

	public void setFp(int fp) {
		this.fp = fp;
	}

	public void setIrClassId(int ir_classId) {
		this.irClassId = ir_classId;
	}

	public void setNpv(double npv) {
		this.npv = npv;
	}

	public void setPpv(double ppv) {
		this.ppv = ppv;
	}

	public void setSensitivity(double sensitivity) {
		this.sensitivity = sensitivity;
	}

	public void setSpecificity(double specificity) {
		this.specificity = specificity;
	}

	public void setTn(int tn) {
		this.tn = tn;
	}

	public void setTp(int tp) {
		this.tp = tp;
	}
}
