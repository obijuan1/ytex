package ytex.kernel.dao;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

public class StringArrayUserType implements UserType {
	private static final Log log = LogFactory.getLog(StringArrayUserType.class);

	public int[] sqlTypes() {
		return new int[] { Types.VARCHAR };
	}

	public Class<String[]> returnedClass() {
		return String[].class;
	}

	public boolean equals(Object x, Object y) {
		return (x == y)
				|| (x != null && y != null && java.util.Arrays.equals(
						(int[]) x, (int[]) y));
	}

	private String stringArrayToString(String[] set) {
		StringBuilder b = new StringBuilder();
		SortedSet<String> s = new TreeSet<String>();
		s.addAll(Arrays.asList(set));
		Iterator<String> iter = s.iterator();
		while (iter.hasNext()) {
			b.append(iter.next());
			if (iter.hasNext()) {
				b.append("|");
			}
		}
		return b.toString();
	}

	private String[] stringToSortedSet(String s) {
		String[] elements = s.split("\\|");
		SortedSet<String> set = new TreeSet<String>();
		List<String> l = new ArrayList<String>();
		l.addAll(Arrays.asList(elements));
		return l.toArray(new String[] {});
	}

	public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
			throws HibernateException, SQLException {
		String s = rs.getString(names[0]);
		return stringToSortedSet(s);
	}

	@SuppressWarnings("unchecked")
	public void nullSafeSet(PreparedStatement st, Object value, int index)
			throws HibernateException, SQLException {
		st.setString(index, stringArrayToString((String[]) value));
	}

	public Object deepCopy(Object value) {
		if (value == null)
			return null;

		String source[] = (String[]) value;
		String copy[] = new String[source.length];
		for (int i = 0; i < source.length; i++)
			copy[i] = source[i];
		return copy;
	}

	public boolean isMutable() {
		return true;
	}

	public Object assemble(Serializable cached, Object owner)
			throws HibernateException {
		return cached;
	}

	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable) value;
	}

	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return original;
	}

	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

}
