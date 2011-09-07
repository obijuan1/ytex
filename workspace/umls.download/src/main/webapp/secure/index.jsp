<jsp:include page="/top.jsp"/>
		<p>
		Download the YTEX UMLS database archives from this site.
		For more information, visit the <a href="http://code.google.com/p/ytex">YTEX google code website</a>.
		</p>
	
		<h1>YTEX v0.3</h1>
		<ul>
			<li><a href="<%=request.getContextPath()%>/secure/umlsdownload?version=0.3&platform=mysql">MySQL</a>
			</li>
			<li><a href="<%=request.getContextPath()%>/secure/umlsdownload?version=0.3&platform=mssql">Microsoft
					SQL Server</a>
			</li>
			<li><a href="<%=request.getContextPath()%>/secure/umlsdownload?version=0.3&platform=orcl">Oracle</a>
			</li>
		</ul>
		<h1>YTEX v0.4 and greater</h1>
		<ul>
			<li><a href="<%=request.getContextPath()%>/secure/umlsdownload?version=0.4&platform=mysql">MySQL</a>
			</li>
			<li><a href="<%=request.getContextPath()%>/secure/umlsdownload?version=0.4&platform=mssql">Microsoft
					SQL Server</a>
			</li>
			<li><a href="<%=request.getContextPath()%>/secure/umlsdownload?version=0.4&platform=orcl">Oracle</a>
			</li>
		</ul>
<jsp:include page="/bottom.jsp"/>