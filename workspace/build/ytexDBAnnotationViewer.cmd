@setlocal
@call %~dp0setenv.cmd
java %JAVA_OPTS% ytex.tools.DBAnnotationViewerMain
@endlocal
