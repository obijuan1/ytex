package my.mas.jdl.common;

/**
 * Singleton example.
 * 
 * @author mas
 */
public final class Singleton {
	private static Singleton istance;

	private Singleton() {
	}

	/**
	 * @return the instance
	 */
	public static Singleton getIstance() {
		if (istance == null) {
			istance = new Singleton();
		}
		return istance;
	}
}
