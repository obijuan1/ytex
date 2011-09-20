package ytex.model;

public class BooleanUtil {
	public static String toYNString(Boolean bool) {
		String ynString;

		if (bool != null) {
			if (bool.booleanValue()) {
				ynString = "Y";
			} else {
				ynString = "N";
			}
		} else {
			ynString = "N";
		}

		return ynString;
	}

	public static Boolean parseYN(String in) {
		if (in != null
				&& (in.equalsIgnoreCase("y") || in.equalsIgnoreCase("yes")
						|| in.equalsIgnoreCase("true") || in
						.equalsIgnoreCase("1"))) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
}