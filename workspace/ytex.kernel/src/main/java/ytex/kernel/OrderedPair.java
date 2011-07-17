package ytex.kernel;

/**
 * simple pair class used for cache keys where the result is symmetric, e.g.
 * similarity measures. Order o1 and o2 so that the results of the comparison is
 * the same, regardless of the order of o1 and o2.
 * 
 * @author vijay
 * 
 * @param <T>
 */
public class OrderedPair<T extends Comparable<T>> {
	private T o1;
	private T o2;

	public OrderedPair(T o1, T o2) {
		super();
		if (o1.compareTo(o2) <= 0) {
			this.o1 = o1;
			this.o2 = o2;
		} else {
			this.o1 = o2;
			this.o2 = o1;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((o1 == null) ? 0 : o1.hashCode());
		result = prime * result + ((o2 == null) ? 0 : o2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		OrderedPair<T> other = (OrderedPair<T>) obj;
		if (o1 == null) {
			if (other.o1 != null)
				return false;
		} else if (!o1.equals(other.o1))
			return false;
		if (o2 == null) {
			if (other.o2 != null)
				return false;
		} else if (!o2.equals(other.o2))
			return false;
		return true;
	}

}
