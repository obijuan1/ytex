package my.mas;

import my.mas.jdl.AppJdl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * Java data loader Main.
 * 
 * @author mas
 */
public final class AppMain {
	private static final String OPT_XDL_CONN_LONG = "conn";
	private static final String OPT_XDL_DATA_LONG = "data";
	private static final String OPT_XDL_LOAD_LONG = "load";
	private static final String OPT_XDL_CONN_DESCR = "conn file";
	private static final String OPT_XDL_DATA_DESCR = "data file";
	private static final String OPT_XDL_LOAD_DESCR = "load file";
	public static final String OPT_XDL_CONN = "c";
	public static final String OPT_XDL_DATA = "d";
	public static final String OPT_XDL_LOAD = "l";

	private AppMain() {
	}

	/**
	 * @return the options of the commandLine
	 */
	public static Options getOprions() {
		Option optXdlConn = new Option(OPT_XDL_CONN, OPT_XDL_CONN_LONG, true, OPT_XDL_CONN_DESCR);
		optXdlConn.setRequired(true);
		Option optXdlData = new Option(OPT_XDL_DATA, OPT_XDL_DATA_LONG, true, OPT_XDL_DATA_DESCR);
		optXdlData.setRequired(true);
		Options options = new Options();
		options.addOption(optXdlConn);
		options.addOption(optXdlData);
		options.addOption(OPT_XDL_LOAD, OPT_XDL_LOAD_LONG, true, OPT_XDL_LOAD_DESCR);
		return options;
	}

	/**
	 * @param arguments
	 *            the arguments to parse
	 * @return the commandLine
	 * @throws ParseException
	 *             exception
	 */
	public static CommandLine parsingCLI(String[] arguments) throws ParseException {
		Options options = getOprions();
		PosixParser posix = new PosixParser();
		return posix.parse(options, arguments);
	}

	/**
	 * @param args
	 *            input string
	 */
	public static void main(String[] args) {
		String cmdLineSyntax = "java [-options] -jar jarfile";
		String header = "Java Data Loader";
		try {
			CommandLine cl = parsingCLI(args);
			String srcConn = cl.getOptionValue(OPT_XDL_CONN);
			String srcData = cl.getOptionValue(OPT_XDL_DATA);
			String srcLoad = cl.getOptionValue(OPT_XDL_LOAD);
			new AppJdl(srcConn, srcData, srcLoad).execute();
		} catch (ParseException e) {
			new HelpFormatter().printHelp(cmdLineSyntax, header, getOprions(), null, true);
		}
	}
}
