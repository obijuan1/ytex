@setlocal
@call %~dp0setenv.cmd
java -classpath %TOOLS_CP% %TOOLS_OPTIONS% ytex.weka.ExportBagOfWords %*
@endlocal