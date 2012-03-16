@setlocal
@rem to start tomcat
@call %~dp0setenv.cmd
@set JAVA_OPTS=-Xmx400m -XX:+CMSClassUnloadingEnabled -XX:+CMSPermGenSweepingEnabled -XX:MaxPermSize=128M 
@mkdir %CATALINA_BASE%\temp
%CATALINA_HOME%\bin\catalina.bat run
@endlocal
