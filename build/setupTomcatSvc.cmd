@setlocal
@call setenv.cmd
%CATALINA_HOME%\bin\service.bat install ytex
%CATALINA_HOME%\bin\tomcat6.exe //US//ytex --Environment=PATH=%PATH%
%CATALINA_HOME%\bin\tomcat6.exe //US//ytex --Classpath=%JAVA_HOME%\lib\tools.jar;%CATALINA_HOME%\bin\bootstrap.jar;%JDBC_CP%
@REM TODO -Dorg.apache.el.parser.COERCE_TO_ZERO=false
@REM %CATALINA_HOME%\bin\tomcat6.exe //US//ytex --Classpath=%JAVA_HOME%\lib\tools.jar;%CATALINA_HOME%\bootstrap.jar;%SQLJDBC_HOME%\enu\sqljdbc4.jar;%YTEX_HOME%\config\desc;%CATALINA_HOME%\lib\catalina.jar
@echo manually update registry key HKEY_LOCAL_MACHINE\SOFTWARE\Wow6432Node\Apache Software Foundation\Procrun 2.0\ytex\Parameters - replaces PATH spaces with semicolons
@endlocal