package ytex.libsvm;

import java.io.IOException;
import java.io.PrintStream;

import ytex.kernel.IRMetrics;
import ytex.kernel.MetricUtil;
import ytex.kernel.SCut;
import ytex.kernel.SVMResults;

public class ScutLibsvm {

	/**
	 * @param args
	 * @throws Exception
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException, Exception {
		String instanceFile = args[0];
		String predictionFile = args[1];
		ScutLibsvm scutLibsvm = new ScutLibsvm();
		Object[] scutOutput = scutLibsvm.calculateScutLibsvm(predictionFile,
				instanceFile);
		scutLibsvm.printIRMetrics(System.out, "raw\t",
				(IRMetrics) scutOutput[1]);
		scutLibsvm.printIRMetrics(System.out, "scut\t" + scutOutput[0],
				(IRMetrics) scutOutput[2]);
		// Object[] scutApplyOutput = scutLibsvm.applyScutLibsvm(predictionFile,
		// instanceFile, (Double)scutOutput[0]);
		// scutLibsvm.printIRMetrics(System.out, "apply scut\t" + scutOutput[0],
		// (IRMetrics) scutApplyOutput[1]);
	}

	/**
	 * 
	 * @param predictionFile
	 * @param instanceFile
	 * @return [Double - scut threshold, IRMetrics raw - ir scores using
	 *         libsvm's default threshold, IRMetrics scut]
	 * @throws Exception
	 * @throws IOException
	 */
	public Object[] calculateScutLibsvm(String predictionFile,
			String instanceFile) throws IOException, Exception {
		SCut scut = new SCut();
		LibSVMParser p = new LibSVMParser();
		SVMResults results = p.parse(predictionFile, instanceFile);
		int scutLabels[] = new int[results.getResults().size()];
		double scutThreshold = scut.getScutThreshold(
				results.getProbabilities(), results.getTargetClassIds(), 1,
				SCut.TargetStatistic.F1, scutLabels);
		IRMetrics irRaw = MetricUtil.calculateIRMetrics(results
				.getTargetClassIds(), results.getPredictedClassIds(), 1);
		IRMetrics irScut = MetricUtil.calculateIRMetrics(results
				.getTargetClassIds(), scutLabels, 1);
		return new Object[] { scutThreshold, irRaw, irScut };
	}

	/**
	 * 
	 * @param predictionFile
	 * @param instanceFile
	 * @param scutThreshold
	 *            threshold to apply
	 * @return [IRMetrics raw - ir scores using libsvm's default threshold,
	 *         IRMetrics scut]
	 * @throws Exception
	 */
	public Object[] applyScutLibsvm(String predictionFile, String instanceFile,
			double scutThreshold) throws Exception {
		SCut scut = new SCut();
		LibSVMParser p = new LibSVMParser();
		SVMResults results = p.parse(predictionFile, instanceFile);
		int scutLabels[] = scut.applyScutThreshold(results.getProbabilities(),
				scutThreshold);
		return new Object[] {
				MetricUtil.calculateIRMetrics(results.getTargetClassIds(),
						results.getPredictedClassIds(), 1),
				MetricUtil.calculateIRMetrics(results.getTargetClassIds(),
						scutLabels, 1) };
	}

	public void printIRMetrics(PrintStream ps, String prefix, IRMetrics irScut) {
		ps.println(prefix + "\t" + irScut);
	}
}
