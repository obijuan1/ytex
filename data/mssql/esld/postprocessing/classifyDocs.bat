time /T
call runScriptOnAnalysisBatch.bat classify_radiology.sql %1
call runScriptOnAnalysisBatch.bat classify_ascites.sql %1
call runScriptOnAnalysisBatch.bat classify_varices.sql %1
call runScriptOnAnalysisBatch.bat classify_livermass.sql %1
time /T