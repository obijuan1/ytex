package ytex.kernel.model;

public class ObjPair<T1 extends Object, T2 extends Object> {
	@Override
	public String toString() {
		return "ObjPair [v1=" + v1 + ", v2=" + v2 + "]";
	}

	public T1 v1;
	public T2 v2;

	public ObjPair(T1 v1, T2 v2) {
		this.v1 = v1;
		this.v2 = v2;
	}
}
