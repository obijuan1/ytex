@setlocal
@rem to start tomcat
@call %~dp0setenv.cmd
%CATALINA_HOME%\bin\catalina.bat run
@endlocal
