package ytex.kernel.dao;

import java.io.IOException;

import ytex.kernel.model.ConceptGraph;

/**
 * create/retrieve concept graphs. store concept graph on file system as they
 * can get big (>10MB). This is not a problem for sql server/oracle, but may
 * require increasing the max_packet_size on mysql.
 * 
 * @author vijay
 * 
 */
public interface ConceptDao {

	/**
	 * retrieve an existing concept graph.
	 * 
	 * @param name
	 *            name of concept graph. Will retrieve from file system. @see
	 *            #createConceptGraph
	 * @return
	 */
	public abstract ConceptGraph getConceptGraph(String name);

	/**
	 * create the concept graph with specified name using specified query.
	 * 
	 * @param name
	 *            name of concept graph. will create file
	 *            ${ytex.conceptGraphDir}/[name].gz
	 * @param query
	 *            returns 2 string columns, 1st column is the child concept, 2nd
	 *            column is the parent concept.
	 * @return ConceptGraph the concept graph generated using this query.
	 */
	public abstract void createConceptGraph(String name, String query,
			final boolean checkCycle) throws IOException;

}