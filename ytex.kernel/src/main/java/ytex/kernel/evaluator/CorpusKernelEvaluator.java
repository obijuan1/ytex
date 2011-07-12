package ytex.kernel.evaluator;

import java.util.Map;

import ytex.kernel.tree.Node;

public interface CorpusKernelEvaluator {
	public void evaluateKernelOnCorpus();

	public void evaluateKernelOnCorpus(Map<Integer, Node> instanceMap,
			int nMod, int nSlice);

	public abstract void evaluateKernelOnCorpus(Map<Integer, Node> instanceIDMap, int nMod)
			throws InterruptedException;
}