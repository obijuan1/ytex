<%@page session="false"%>
<%
	String url = request.getRequestURL().substring(1,
			request.getRequestURL().indexOf("/") + 1)
			+ request.getContextPath();
	response.sendRedirect(url);
%>

<html>
<title>Session Timeout</title>
<body>
<h2>Invalid Session</h2>

<p>Your session appears to have timed out. Please <a href="<%=url%>" />start
again</a>.</p>
</body>
</html>
