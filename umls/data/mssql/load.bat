@rem JAVA_HOME must be set and 
@rem bcp must be in the path for this script to work

@rem where the RRF files were generated for MySQL
set RRF_HOME=E:\bio\umlsmysql\2010AB\META

@rem where to store the converted RRF files
set RRF_TEMP=E:\bio\umlsmysql\2010AB\META\unc

@rem sql server database object prefix
set DB=UMLS.dbo

@rem sql server host
set SERVER=localhost

@rem location of RRFtoWideTab.class
set CLASSPATH=E:\projects\ytex\umls\bin

set CONVERT_CMD=%JAVA_HOME%\bin\java RRFtoWideTab

@echo MRSAB
call :import MRSAB 
@echo MRFILES
@echo MRCOLS
@echo MRCOLS
@echo MRSAB
@echo MRDOC

:import
@echo %~1
@rem %CONVERT_CMD% %RRF_HOME%\%~1.rrf %RRF_TEMP%\%~1.txt
@rem bcp %DB%.%~1 in %RRF_TEMP%\%~1 -S localhost -w -T
goto :eof
