@setlocal
@rem to start tomcat
@call %~dp0setenv.cmd
@mkdir %CATALINA_BASE%\temp
%CATALINA_HOME%\bin\catalina.bat run
@endlocal
