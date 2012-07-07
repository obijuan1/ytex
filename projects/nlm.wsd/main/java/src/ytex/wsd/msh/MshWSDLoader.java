package ytex.wsd.msh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;
import ytex.kernel.KernelContextHolder;

public class MshWSDLoader {
	private static final Log log = LogFactory.getLog(MshWSDLoader.class);
	private JdbcTemplate template;
	private static final String INSERT = "insert into msh_wsd (word, pmid, abs, cui, abs_ambiguity_start, abs_ambiguity_end) values (?,?,?,?,?,?)";
	private static final Pattern p = Pattern.compile("<e>(.*)</e>");

	public static void main(String args[]) throws IOException {
		(new MshWSDLoader()).processDir(args[0]);
	}

	public MshWSDLoader() {
		this.template = new JdbcTemplate((DataSource) KernelContextHolder
				.getApplicationContext().getBean("dataSource"));
	}

	public void processDir(String dir) throws IOException {
		File fDir = new File(dir);
		if (!fDir.exists() || !fDir.isDirectory())
			throw new IOException("Bad dir: " + dir);

		for (File f : fDir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".arff");
			}
		})) {
			processWord(f);
		}
	}

	public void processWord(File arffFile) throws IOException {
		String word = arffFile.getName().substring(0,
				arffFile.getName().length() - "_pmids_tagged.arff".length());
		BufferedReader reader = new BufferedReader(new FileReader(arffFile));
		ArffReader arff = new ArffReader(reader);
		Instances data = arff.getData();
		String cuis[] = data.relationName().split("_");
		@SuppressWarnings("unchecked")
		Enumeration<Instance> e = data.enumerateInstances();
		while (e.hasMoreElements()) {
			Instance inst = e.nextElement();
			processInstance(word, cuis, inst);
		}
	}

	private void processInstance(String word, String[] cuis, Instance inst) {
		int pmid = (int) inst.value(0);
		String abs = inst.stringValue(1);
		String cls = inst.stringValue(2);
		String cui = cuis[Integer.parseInt(cls.substring(1)) - 1];
		Matcher m = p.matcher(abs);
		if (m.find()) {
			// trim out the <e> and </e>, figure out offset for ambiguity
			int ambiguityStart = m.start();
			String ambiguity = m.group(1);
			int ambiguityEnd = ambiguityStart + ambiguity.length();
			StringBuilder absTrimmed = new StringBuilder(abs.substring(0,
					ambiguityStart));
			absTrimmed.append(ambiguity);
			absTrimmed.append(abs.substring(m.end()));
			template.update(INSERT, word, pmid, absTrimmed.toString(), cui,
					ambiguityStart, ambiguityEnd);
		} else {
			log.error("could not find ambiguous word: word=" + word + ", pmid="
					+ pmid + ", abs=" + abs);
		}
	}
}
