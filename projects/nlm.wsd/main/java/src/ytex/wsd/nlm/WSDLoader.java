package ytex.wsd.nlm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import ytex.kernel.KernelContextHolder;

/**
 * import nlm wsd data into nlm_wsd database table.
 * <p/>
 * parameter is the directory containing all the word directories, i.e. the
 * directory that contains adjustment, association, ...
 * <p/>
 * iterate through the subdirs and load the word set, i.e.
 * adjustment/adjustment_set, association/association_set, ...
 * 
 * @author vijay
 * 
 */
public class WSDLoader {

	private static final String INSERT = "insert into nlm_wsd (word, choice_id, sentence_reference, choice_code, sentence, sent_ambiguity, sent_ambiguity_alias, sent_context_start, sent_context_end, sent_ambiguity_start, sent_ambiguity_end, sent_immediate_context, UI, TI, AB, cite_ambiguity, cite_ambiguity_alias, cite_context_start, cite_context_end, cite_ambiguity_start, cite_ambiguity_end, cite_immediate_context) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private static final Pattern headerPattern = Pattern
			.compile("(\\d+)\\|(.*)\\|(\\w+)");
	private static final Pattern annoPattern = Pattern
			.compile("(.+)\\|(.+)\\|(\\d+)\\|(\\d+)\\|(\\d+)\\|(\\d+)\\|(.*)\\|");
	private static final Pattern uiPattern = Pattern
			.compile("UI\\s+-\\s(\\d+)");
	private static final Pattern tiPattern = Pattern
			.compile("\\w\\w\\s+-\\s(.+)");
	JdbcTemplate template = null;

	public static void main(String args[]) throws IOException {
		WSDLoader l = new WSDLoader();
		File baseDir = new File(args[0]);
		// iterate over directories in this dir
		for (File wordDir : baseDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		})) {
			l.loadWordSet(wordDir);
		}
	}

	WSDLoader() {
		this.template = new JdbcTemplate((DataSource) KernelContextHolder
				.getApplicationContext().getBean("dataSource"));
	}

	public void loadWordSet(File dir) throws IOException {
		System.err.println("processing dir: " + dir.getName());
		String word = dir.getName();
		BufferedReader r = null;
		try {
			r = new BufferedReader(new FileReader(dir.getAbsolutePath()
					+ File.separator + word + "_set"));
			String line = null;
			;
			while ((line = r.readLine()) != null) {
				if (line.length() != 0) {
					// expect 5 lines
					String header = line;
					String sent = null;
					String sentAnno = null;
					String ui = null;
					String ti = null;
					String ab = null;
					String tiAnno = null;
					sent = r.readLine();
					if (sent != null && !sent.isEmpty()) {
						sentAnno = r.readLine();
					}
					if (sentAnno != null && !sentAnno.isEmpty())
						ui = r.readLine();
					if (ui != null && !ui.isEmpty())
						ti = r.readLine();
					if (ti != null && !ti.isEmpty())
						ab = r.readLine();
					if (ab != null && !ab.isEmpty())
						tiAnno = r.readLine();
					if (tiAnno == null || tiAnno.isEmpty()) {
						tiAnno = ab;
						ab = null;
					}
					if (tiAnno != null && !ti.isEmpty())
						this.storeWord(word, header, sent, sentAnno, ui, ti,
								ab, tiAnno);
				}
			}

		} finally {
			if (r != null) {
				try {
					r.close();
				} catch (IOException e) {
				}
			}
		}

	}

	private void storeWord(String word, String header, String sentence,
			String sentAnno, String strUi, String ti, String ab, String tiAnno)
			throws IOException {
		int choice_id = 0;
		String sentence_reference = null;
		String choice_code, sent_ambiguity, sent_ambiguity_alias = null;
		int sent_context_start, sent_context_end, sent_ambiguity_start, sent_ambiguity_end;
		String sent_immediate_context = null;
		int UI = 0;
		String TI = null;
		String AB = null;
		String cite_ambiguity = null;
		String cite_ambiguity_alias = null;
		int cite_context_start, cite_context_end, cite_ambiguity_start, cite_ambiguity_end = 0;
		String cite_immediate_context = null;
		Matcher m = headerPattern.matcher(header);
		if (m.find()) {
			choice_id = Integer.parseInt(m.group(1));
			sentence_reference = m.group(2);
			choice_code = m.group(3);
			m = annoPattern.matcher(sentAnno);
			if (m.find()) {
				sent_ambiguity = m.group(1);
				sent_ambiguity_alias = m.group(2);
				sent_context_start = Integer.parseInt(m.group(3));
				sent_context_end = Integer.parseInt(m.group(4));
				sent_ambiguity_start = Integer.parseInt(m.group(5));
				sent_ambiguity_end = Integer.parseInt(m.group(6));
				sent_immediate_context = m.group(7);
				m = uiPattern.matcher(strUi);
				if (m.find()) {
					UI = Integer.parseInt(m.group(1));
					m = tiPattern.matcher(ti);
					if (m.find()) {
						TI = m.group(1);
						if (ab != null) {
							m = tiPattern.matcher(ab);
							if (m.find()) {
								AB = m.group(1);
							}
						}
						m = annoPattern.matcher(tiAnno);
						if (m.find()) {
							cite_ambiguity = m.group(1);
							cite_ambiguity_alias = m.group(2);
							cite_context_start = Integer.parseInt(m.group(3));
							cite_context_end = Integer.parseInt(m.group(4));
							cite_ambiguity_start = Integer.parseInt(m.group(5));
							cite_ambiguity_end = Integer.parseInt(m.group(6));
							cite_immediate_context = m.group(7);
							// OK - found everything
							template.update(INSERT, word, choice_id,
									sentence_reference, choice_code, sentence,
									sent_ambiguity, sent_ambiguity_alias,
									sent_context_start, sent_context_end,
									sent_ambiguity_start, sent_ambiguity_end,
									sent_immediate_context, UI, TI, AB,
									cite_ambiguity, cite_ambiguity_alias,
									cite_context_start, cite_context_end,
									cite_ambiguity_start, cite_ambiguity_end,
									cite_immediate_context);
							return;
						}
					}

				}
			}
		}
		throw new IOException("error parsing, header=" + header);
	}
}
