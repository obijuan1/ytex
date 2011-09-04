package ytex.umls;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FileDownloadServlet extends HttpServlet {
	private static final int DEFAULT_BUFFER_SIZE = 10240;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String[] VERSIONS = { "0.3", "0.4" };
	private static final String[] PLATFORMS = { "mssql", "mysql", "orcl" };

	private static Set<String> versions = new HashSet<String>();
	private static Set<String> platforms = new HashSet<String>();
	private static String umlsDir = System.getProperty("umls.download.dir",
			"e:/projects/ytex-umls");

	static {
		versions.addAll(Arrays.asList(VERSIONS));
		platforms.addAll(Arrays.asList(PLATFORMS));
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// Get requested file by path info.
		String version = request.getParameter("version");
		String platform = request.getParameter("platform");
		if (!versions.contains(version) || !platforms.contains(platform)) {
			throw new IOException("invalid platform/version");
		}
		String filePath = umlsDir + "/" + version + "/umls-" + platform
				+ ".zip";
		// Decode the file name (might contain spaces and on) and prepare file
		// object.
		File file = new File(filePath);

		// Check if file actually exists in filesystem.
		if (!file.exists()) {
			// Do your thing if the file appears to be non-existing.
			// Throw an exception, or send 404, or show default/warning page, or
			// just ignore it.
			response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
			return;
		}

		// Get content type by filename.
		String contentType = getServletContext().getMimeType(file.getName());

		// If content type is unknown, then set the default value.
		// For all content types, see:
		// http://www.w3schools.com/media/media_mimeref.asp
		// To add new content types, add new mime-mapping entry in web.xml.
		if (contentType == null) {
			contentType = "application/octet-stream";
		}

		// Init servlet response.
		response.reset();
		response.setBufferSize(DEFAULT_BUFFER_SIZE);
		response.setContentType(contentType);
		response.setHeader("Content-Length", String.valueOf(file.length()));
		response.setHeader("Content-Disposition", "attachment; filename=\""
				+ file.getName() + "\"");

		// Prepare streams.
		BufferedInputStream input = null;
		OutputStream output = null;

		try {
			// Open streams.
			input = new BufferedInputStream(new FileInputStream(file),
					DEFAULT_BUFFER_SIZE);
			output = response.getOutputStream();

			// Write file contents to response.
			byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
			int length;
			while ((length = input.read(buffer)) > 0) {
				output.write(buffer, 0, length);
			}
		} finally {
			// Gently close streams.
			// close(output);
			close(input);
		}
	}

	private static void close(Closeable resource) {
		if (resource != null) {
			try {
				resource.close();
			} catch (IOException e) {
				// Do your thing with the exception. Print it, log it or mail
				// it.
				e.printStackTrace();
			}
		}
	}

}
