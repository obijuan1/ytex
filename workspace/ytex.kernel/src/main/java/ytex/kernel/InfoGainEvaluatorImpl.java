package ytex.kernel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import ytex.kernel.model.FeatureInfogain;

/**
 * 
 * @author vijay
 *
 */
public class InfoGainEvaluatorImpl {
	protected SimpleJdbcTemplate simpleJdbcTemplate;
	protected JdbcTemplate jdbcTemplate;
	protected PlatformTransactionManager transactionManager;
	protected TransactionTemplate txNew;

	/**
	 * 
	 * @param labelQuery - query to get class counts per label - used to compute p(Y).
	 * 	Required; this assumes that we have prior knowledge about the marginal class distributions.
	 * 	When doing a stratified cross validation, this is always the case.
	 *  Returns following fields:
	 *  <ul>
	 *  	<li> label (string)
	 *  	<li> class (string)
	 *  	<li> count (int)
	 *  </ul>
	 * @param featureQuery - query to get feature frequency - used to compute p(X).  
	 * Optional; if not supplied will use feature frequency from classFeatureFrequency.  
	 * Returns following fields:
	 *  <ul>
	 *  	<li> feature name (string)
	 *  	<li> frequency (double)
	 *  </ul>
	 * @param classFeatureQuery - query to get joint class-feature counts for all folds for the specfied label.
	 * Used to compute p(X,Y).  Must contain named parameter <code>label</code>.  
	 * Must be ordered by fold and feature name. Must return following fields:
	 * 	<ul>
	 *  	<li> fold id (int, can be null)
	 *  	<li> feature name (string)
	 *  	<li> class (string)
	 *  	<li> feature bin (string)
	 *  	<li> bin count (int)
	 *  </ul>
	 *  Iterate through the results and create the joint probability table, 
	 *  and compute mutual information from this.
	 */
	public void storeInfoGain(String labelQuery, String featureQuery,
			String classFeatureQuery) {
		Map<String, Map<String,Double>> labelClassMap = loadY(labelQuery);
		Map<String,Double> featureFreqMap = loadX(labelQuery);
		for(Map.Entry<String, Map<String,Double>> labelClass : labelClassMap.entrySet()) {
			storeInfoGain(labelClass.getKey(), labelClass.getValue(), featureFreqMap, featureQuery);
		}
	}
	
	/**
	 * iterates through query results and computes infogain
	 * @author vijay
	 *
	 */
	public class InfoGainRowCallbackHandler implements RowCallbackHandler {
		private List<FeatureInfogain> listInfogain;
		Integer currentFold;
		String currentFeature;
		int currentBins[];
		Map<String, Double> pY;
		Map<String, Double> pX;
		String label;
		
		public InfoGainRowCallbackHandler(List<FeatureInfogain> listInfogain,
				Map<String, Double> pY, Map<String, Double> pX, String label) {
			super();
			this.listInfogain = listInfogain;
			this.pY = pY;
			this.pX = pX;
			this.label = label;
		}

		@Override
		public void processRow(ResultSet rs) throws SQLException {
			Integer cvFoldId = rs.getInt(1);
			if(rs.wasNull())
				cvFoldId = null;
		}		
	}

	private void storeInfoGain(final String label, final Map<String, Double> pY,
			final Map<String, Double> pX, final String featureQuery) {
		final List<FeatureInfogain> listInfogain = new ArrayList<FeatureInfogain>();
		final InfoGainRowCallbackHandler handler = new InfoGainRowCallbackHandler(listInfogain, pY, pX, label);
		txNew.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus txStatus) {
				jdbcTemplate.query(new PreparedStatementCreator() {

					@Override
					public PreparedStatement createPreparedStatement(
							Connection conn) throws SQLException {
						PreparedStatement ps = conn.prepareStatement(featureQuery,
								ResultSet.TYPE_FORWARD_ONLY,
								ResultSet.CONCUR_READ_ONLY);
						ps.setString(1,label);
						return ps;
					}

				}, handler);
				return null;
			}
		});
		//insert the infogain
	}

	private Map<String, Double> loadX(String labelQuery) {
		// TODO Auto-generated method stub
		return null;
	}

	private Map<String, Map<String, Double>> loadY(String labelQuery) {
		// TODO Auto-generated method stub
		return null;
	}
}
