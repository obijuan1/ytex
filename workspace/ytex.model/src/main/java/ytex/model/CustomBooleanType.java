package ytex.model;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

/**
 * copied from https://www.hibernate.org/189.html
 * @author vijay
 *
 */
public class CustomBooleanType implements UserType {

	public CustomBooleanType() {

	}

	public int[] sqlTypes() {

		return new int[] { Types.CHAR };

	}

	@SuppressWarnings("unchecked")
	public Class returnedClass() {

		return Boolean.class;

	}

	public boolean equals(Object x, Object y) throws HibernateException {

		return (x == y) || (x != null && y != null && (x.equals(y)));

	}

	public Object nullSafeGet(ResultSet inResultSet, String[] names, Object o)

	throws HibernateException, SQLException {
		String val = (String) Hibernate.STRING.nullSafeGet(inResultSet,
				names[0]);

		return BooleanUtil.parseYN(val);

	}

	public void nullSafeSet(PreparedStatement inPreparedStatement, Object o,
			int i)

	throws HibernateException, SQLException {

		String val = BooleanUtil.toYNString((Boolean) o);

		inPreparedStatement.setString(i, val);

	}

	public Object deepCopy(Object o) throws HibernateException {

		if (o == null)
			return null;

		return new Boolean(((Boolean) o).booleanValue());

	}

	public boolean isMutable() {

		return false;

	}

//	@Override
	public Object assemble(Serializable arg0, Object arg1)
			throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
	public Serializable disassemble(Object arg0) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
	public int hashCode(Object arg0) throws HibernateException {
		return arg0.hashCode();
	}

	public Object replace(Object arg0, Object arg1, Object arg2)
			throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

}
