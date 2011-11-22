package ytex.kernel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ytex.semil.SemiLFormatterFactory.SemiLDataFormatter;

/**
 * for each train/test pair create the following files:
 * <ul>
 * <li>[prefix]class.txt - as in semil: instance id \t train/test flag \t target
 * class id</li
 * <li>[prefix]code.properties - map of codes to classes. currently only do a
 * one-against-all coding
 * <li>[prefix]code[n]_label.txt - for each class
 * </ul>
 * 
 * @author vijay
 * 
 */
public class SVMLinFormatterFactory implements SparseDataFormatterFactory {
	public static class SVMLinDataFormatter extends SemiLDataFormatter {
		private static final Log log = LogFactory
				.getLog(SVMLinDataFormatter.class);

		public SVMLinDataFormatter(KernelUtil kernelUtil) {
			super(kernelUtil);
		}

		@Override
		protected void exportData(SparseData sparseData, String label,
				Integer run, Integer fold) throws IOException {
			exportAttributeNames(sparseData, label, run, fold);
			String filename = FileUtil.getScopedFileName(outdir, label, run,
					fold, "data.txt");
			BufferedWriter wData = null;
			try {
				wData = new BufferedWriter(new FileWriter(filename));
				for (long instanceId : sparseData.getInstanceIds()) {
					// get line with sparse attribute indices and values
					SortedMap<Integer, Double> instanceValues = getSparseLineValues(
							sparseData, numericAttributeMap,
							nominalAttributeMap, instanceId);
					// write the line
					writeLibsvmLine(wData, instanceValues);
				}
			} finally {
				if (wData != null) {
					wData.close();
				}
			}
		}

		/**
		 * recode the classes. the codes are bits in some sort of class coding
		 * scheme. this creates one-against-all codes.
		 * <p>
		 * creates [scope]code.properties. file to write the codes to. When
		 * parsing results, we will read this properties file.
		 * <p>
		 * creates [scope]code[n]_label.txt.  Class label files for one-against-all
		 * classification.
		 * 
		 * @param trainInstanceIdToClass
		 *            map of training instance id to class id
		 * @return map of code to map of instance id - recoded class id
		 * @throws IOException
		 */
		private void exportOneAgainstAllCodes(String label, Integer run,
				Integer fold, SortedMap<Long, Integer> trainInstanceIdToClass)
				throws IOException {
			// file to write the map between codes and classes
			String classFileName = FileUtil.getScopedFileName(outdir, label,
					run, fold, "code.properties");
			SortedSet<Integer> classIds = new TreeSet<Integer>();
			classIds.addAll(trainInstanceIdToClass.values());
			classIds.remove(0);
			// if there is only 1 class, abort
			if (classIds.size() < 2) {
				log.warn("<2 classes, skipping export for label " + label
						+ " run " + run + " fold " + fold);
				return;
			}
			Properties props = new Properties();
			StringBuilder bCodeList = new StringBuilder();
			int code = 1;
			Integer[] classIdArray = classIds.toArray(new Integer[0]);
			for (int i = 0; i < classIdArray.length; i++) {
				int classId = classIdArray[i];
				// recode the instances
				SortedMap<Long, Integer> mapRecodedInstanceIdToClass = new TreeMap<Long, Integer>();
				for (Map.Entry<Long, Integer> instanceIdToClassEntry : trainInstanceIdToClass
						.entrySet()) {
					int trainClassId = instanceIdToClassEntry.getValue();
					int codedClassId = 0; // default to unlabeled
					if (trainClassId == classId) {
						codedClassId = 1;
					} else if (trainClassId != 0) {
						codedClassId = -1;
					}
					mapRecodedInstanceIdToClass.put(
							instanceIdToClassEntry.getKey(), codedClassId);
				}
				String labelFileBaseName = FileUtil.getScopedFileName(outdir,
						label, run, fold, "code" + code + "_label");
				exportLabel(labelFileBaseName+".txt", mapRecodedInstanceIdToClass);
				// add the map from code to class
				props.setProperty(labelFileBaseName + ".class",
						Integer.toString(classId));
				// add the code to the list of codes
				bCodeList.append(labelFileBaseName).append(",");
				// if there are just 2 classes, stop here
				if (classIdArray.length == 2) {
					props.setProperty("classOther",
							Integer.toString(classIdArray[1]));
					break;
				}
				// increment the code
				code++;
			}
			props.setProperty("codes", bCodeList.toString());
			Writer w = null;
			try {
				w = new BufferedWriter(new FileWriter(classFileName));
				props.store(w, "oneAgainstAll");
			} finally {
				if (w != null) {
					try {
						w.close();
					} catch (Exception e) {
					}
				}
			}
			// return mapCodeToInstanceClass;
		}

		@Override
		public void initializeFold(SparseData sparseData, String label,
				Integer run, Integer fold,
				SortedMap<Boolean, SortedMap<Long, String>> foldInstanceLabelMap)
				throws IOException {
			if (SCOPE_FOLD.equals(this.exportProperties.getProperty(SCOPE))) {
				exportData(sparseData, label, run, fold);
			}
			String idFileName = FileUtil.getScopedFileName(outdir, label, run,
					fold, "class.txt");
			SortedMap<Long, Integer> trainInstanceIdToClass = super
					.getTrainingClassMap(idFileName,
							foldInstanceLabelMap.get(true),
							foldInstanceLabelMap.get(false),
							this.labelToClassIndexMap.get(label),
							sparseData.getInstanceIds());
			exportOneAgainstAllCodes(label, run, fold, trainInstanceIdToClass);
		}
	}



	private KernelUtil kernelUtil;

	@Override
	public SparseDataFormatter getFormatter() {
		return new SVMLinDataFormatter(kernelUtil);
	}
	public KernelUtil getKernelUtil() {
		return kernelUtil;
	}

	public void setKernelUtil(KernelUtil kernelUtil) {
		this.kernelUtil = kernelUtil;
	}

}
