package ytex.i2b2;

import java.io.File;

import javax.sql.DataSource;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.ParserAdapter;

import ytex.kernel.KernelContextHolder;

/**
 * create table for storing data like this:
create table i2b2_2008_system_output (
    i2b2_2008_system_output_id int auto_increment not null primary key,
    system varchar(50) not null,
    output varchar(50) not null,
    source varchar(50) not null,
    disease varchar(50) not null,
    docId int not null,
    judgment char(1) not null,
    unique index NK_i2b2_2008_sys_out (system, output, source, disease, docId)
);

 * @author vijay
 *
 */
public class SystemOutputLoaderImpl {
	private SimpleJdbcTemplate jdbcTemplate;

	protected void insertSystemOutputRecord(String system, String output,
			String source, String disease, int docId, String judgment) {
		jdbcTemplate
				.update("insert into i2b2_2008_system_output (system, output, source, disease, docId, judgment) values (?,?,?,?,?,?)",
						system, output, source, disease, docId, judgment);
	}

	public SystemOutputLoaderImpl() {
		DataSource ds = KernelContextHolder.getApplicationContext().getBean(
				DataSource.class);
		jdbcTemplate = new SimpleJdbcTemplate(ds);
	}

	public class OutputHandler extends DefaultHandler {
		String system;
		String output;
		String source;
		String disease;

		public OutputHandler(String system, String output) {
			super();
			this.system = system;
			this.output = output;
		}

		public void startElement(String namespace, String localName,
				String qName, Attributes atts) {
			String currTag = localName.toLowerCase();
			if (currTag.equals("diseases")) {
				source = atts.getValue("source");
			} else if (currTag.equals("disease")) {
				disease = atts.getValue("name");
			} else if (currTag.equals("doc")) {
				int docId = Integer.parseInt(atts.getValue("id"));
				String judgement = atts.getValue("judgment");
				insertSystemOutputRecord(system, output, source, disease,
						docId, judgement);
			}
		}

	}

	protected void loadFile(String system, File outputFile) throws Exception {
		String output = outputFile.getName();
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();
		ParserAdapter pa = new ParserAdapter(sp.getParser());
		pa.setContentHandler(new OutputHandler(system, output));
		pa.parse(outputFile.toURL().toString());

	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		File systemDir = new File(args[0]);
		SystemOutputLoaderImpl loader = new SystemOutputLoaderImpl();
		for (File outputFile : systemDir.listFiles()) {
			loader.loadFile(systemDir.getName(), outputFile);
		}
	}

}
