package ytex.cmc;

public interface DocumentLoader {

	public abstract void process(String urlString, String documentSet)
			throws Exception;

}