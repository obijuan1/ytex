package ytex.kernel;

import junit.framework.TestCase;

public class PairTest extends TestCase {
	
	public void testArrayEq() {
		String arr1[] = {"foo", "bar"};
		String arr2[] = {"foo", "bar"};
		System.out.println(arr1.equals(arr2) ? "yes" : "no");
	}
	public void testPairEq() {
		Pair<String> p1 = new Pair<String>("foo", "bar");
		Pair<String> p2 = new Pair<String>("foo", "bar");
		System.out.println(p1.equals(p2) ? "yes" : "no");
	}
	public void testOrderedPairEq() {
		OrderedPair<String> p1 = new OrderedPair<String>("foo", "bar");
		OrderedPair<String> p2 = new OrderedPair<String>("bar", "foo");
		System.out.println(p1.equals(p2) ? "yes" : "no");
	}	
}
