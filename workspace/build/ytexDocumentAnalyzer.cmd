@setlocal
@call %~dp0setenv.cmd
java %JAVA_OPTS% org.apache.uima.tools.docanalyzer.DocumentAnalyzer
@endlocal