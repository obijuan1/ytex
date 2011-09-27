@setlocal
@rem wrapper to run build-tools.xml build script
ant -Dytex.home=%YTEX_HOME% -Dbasedir=. -buildfile %YTEX_HOME%\build-tools.xml %*
@endlocal