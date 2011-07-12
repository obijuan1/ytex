@setlocal
@call %~dp0setenv.cmd
javaw -classpath %TOOLS_CP% %TOOLS_OPTIONS% org.apache.uima.tools.docanalyzer.DocumentAnalyzer
@endlocal