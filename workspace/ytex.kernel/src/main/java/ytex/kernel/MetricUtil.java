package ytex.kernel;

public class MetricUtil {

	public static IRMetrics calculateIRMetrics(int targetClassLabels[],
			int predictedClassLabels[], int targetClass) {
		int tp = 0;
		int fp = 0;
		int tn = 0;
		int fn = 0;
		for (int i = 0; i < targetClassLabels.length; i++) {
			if (targetClassLabels[i] == targetClass) {
				if (predictedClassLabels[i] == targetClassLabels[i])
					tp++;
				else
					fn++;
			} else {
				if (predictedClassLabels[i] == targetClassLabels[i])
					tn++;
				else
					fp++;
			}
		}
		return new IRMetrics(tp, fp, tn, fn);
	}

}
