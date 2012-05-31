package ytex.kernel;

import weka.core.ContingencyTables;
import junit.framework.TestCase;

public class ContingencyTableTest extends TestCase {
	public void testIG() {
		double[][] ct = new double[][] { { 226, 439 }, { 26, 11 } };
		System.out.println(ContingencyTables.entropyOverColumns(ct)
				- ContingencyTables.entropyConditionedOnRows(ct));
		System.out.println(ContingencyTables.entropyOverRows(ct)
				- ContingencyTables.entropyConditionedOnColumns(ct));
		System.out.println("entropyOverColumns " + ContingencyTables.entropyOverColumns(ct));
		System.out.println("entropyConditionedOnRows " + ContingencyTables.entropyConditionedOnRows(ct));
		System.out.println("entropyConditionedOnColumns " + ContingencyTables.entropyConditionedOnColumns(ct));
		System.out.println("1,2 " +  ct[0][1]);
	}
}
