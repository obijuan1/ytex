@setlocal
@rem wrapper to run build-setup.xml build script
@rem must be executed from YTEX_HOME directory

@call setenv.cmd
%ANT_HOME%\bin\ant -Dytex.home=%YTEX_HOME% -buildfile build-setup.xml %*
@endlocal