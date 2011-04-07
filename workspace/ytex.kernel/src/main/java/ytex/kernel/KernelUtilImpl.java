package ytex.kernel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

public class KernelUtilImpl implements KernelUtil {
	private JdbcTemplate jdbcTemplate = null;

	public DataSource getDataSource() {
		return jdbcTemplate.getDataSource();
	}

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.kernel.DataExporter#loadProperties(java.lang.String,
	 * java.util.Properties)
	 */
	@Override
	public void loadProperties(String propertyFile, Properties props)
			throws FileNotFoundException, IOException,
			InvalidPropertiesFormatException {
		InputStream in = null;
		try {
			in = new FileInputStream(propertyFile);
			if (propertyFile.endsWith(".xml"))
				props.loadFromXML(in);
			else
				props.load(in);
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.kernel.DataExporter#loadInstances(java.lang.String,
	 * java.util.SortedMap)
	 */
	@Override
	public InstanceData loadInstances(String strQuery) {
		final InstanceData instanceLabel = new InstanceData();
		jdbcTemplate.query(strQuery, new RowCallbackHandler() {

			@Override
			public void processRow(ResultSet rs) throws SQLException {
				String label = "";
				int run = 0;
				int fold = 0;
				boolean train = true;
				int instanceId = rs.getInt(1);
				String className = rs.getString(2);
				if (rs.getMetaData().getColumnCount() >= 3)
					train = rs.getBoolean(3);
				if (rs.getMetaData().getColumnCount() >= 4)
					label = rs.getString(4);
				if (rs.getMetaData().getColumnCount() >= 5)
					fold = rs.getInt(5);
				if (rs.getMetaData().getColumnCount() >= 6)
					run = rs.getInt(6);
				// get runs for label
				SortedMap<Integer, SortedMap<Integer, SortedMap<Boolean, SortedMap<Integer, String>>>> runToInstanceMap = instanceLabel
						.getLabelToInstanceMap().get(label);
				if (runToInstanceMap == null) {
					runToInstanceMap = new TreeMap<Integer, SortedMap<Integer, SortedMap<Boolean, SortedMap<Integer, String>>>>();
					instanceLabel.getLabelToInstanceMap().put(label,
							runToInstanceMap);
				}
				// get folds for run
				SortedMap<Integer, SortedMap<Boolean, SortedMap<Integer, String>>> foldToInstanceMap = runToInstanceMap
						.get(run);
				if (foldToInstanceMap == null) {
					foldToInstanceMap = new TreeMap<Integer, SortedMap<Boolean, SortedMap<Integer, String>>>();
					runToInstanceMap.put(run, foldToInstanceMap);
				}
				// get train/test set for fold
				SortedMap<Boolean, SortedMap<Integer, String>> ttToClassMap = foldToInstanceMap
						.get(fold);
				if (ttToClassMap == null) {
					ttToClassMap = new TreeMap<Boolean, SortedMap<Integer, String>>();
					foldToInstanceMap.put(fold, ttToClassMap);
				}
				// get instances for train/test set
				SortedMap<Integer, String> instanceToClassMap = ttToClassMap
						.get(train);
				if (instanceToClassMap == null) {
					instanceToClassMap = new TreeMap<Integer, String>();
					ttToClassMap.put(train, instanceToClassMap);
				}
				// set the instance class
				instanceToClassMap.put(instanceId, className);
				// add the class to the labelToClassMap
				SortedSet<String> labelClasses = instanceLabel
						.getLabelToClassMap().get(label);
				if (labelClasses == null) {
					labelClasses = new TreeSet<String>();
					instanceLabel.getLabelToClassMap().put(label, labelClasses);
				}
				if (!labelClasses.contains(className))
					labelClasses.add(className);
			}
		});
		return instanceLabel;
	}
}
