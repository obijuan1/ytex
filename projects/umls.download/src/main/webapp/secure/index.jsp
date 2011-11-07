<%@ page import="ytex.umls.*"%>
<%@ page import="ytex.umls.dao.*"%>
<%@ page import="org.springframework.web.context.support.*"%>

<jsp:include page="/top.jsp" />
<%
	DownloadDAO dlDao = WebApplicationContextUtils
			.getWebApplicationContext(this.getServletContext())
			.getBean(DownloadDAO.class);
	dlDao.saveDownloadEntry(request.getUserPrincipal().getName(), null,
			null);
	DownloadURLGenerator dg = WebApplicationContextUtils
			.getWebApplicationContext(this.getServletContext())
			.getBean(DownloadURLGenerator.class);
	String url03mysql = dg.getDownloadURL("0.3", "mysql");
	String url03mssql = dg.getDownloadURL("0.3", "mssql");
	String url03orcl = dg.getDownloadURL("0.3", "orcl");
	String url04mysql = dg.getDownloadURL("0.4", "mysql");
	String url04mssql = dg.getDownloadURL("0.4", "mssql");
	String url04orcl = dg.getDownloadURL("0.4", "orcl");
%>
<p>
	Download the YTEX UMLS database archives from this site. For more
	information, visit the <a href="http://code.google.com/p/ytex">YTEX
		google code website</a>.
</p>

<h1>YTEX v0.3</h1>
<ul>
	<li><a href="<%=url03mysql%>">MySQL</a></li>
	<li><a href="<%=url03mssql%>">Microsoft SQL Server</a></li>
	<li><a href="<%=url03orcl%>">Oracle</a></li>
</ul>
<h1>YTEX v0.4</h1>
<ul>
	<li><a href="<%=url04mysql%>">MySQL</a></li>
	<li><a href="<%=url04mssql%>">Microsoft SQL Server</a></li>
	<li><a href="<%=url04orcl%>">Oracle</a></li>
</ul>
<h1>YTEX v0.5 or greater</h1>
<ul>
	<li><a href="<%=dg.getDownloadURL("0.5", "mysql")%>">MySQL</a></li>
	<li><a href="<%=dg.getDownloadURL("0.5", "mssql")%>">Microsoft SQL Server</a></li>
	<li><a href="<%=dg.getDownloadURL("0.5", "orcl")%>">Oracle</a></li>
</ul>
<jsp:include page="/bottom.jsp" />