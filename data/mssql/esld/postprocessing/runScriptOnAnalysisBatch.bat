@set DB=VACS_PROGNOTES
@set SERVER=VHACONSQLR
@set SCHEMA=ESLD

sqlcmd -S VHACONSQLR -E -d VACS_PROGNOTES -v analysis_batch="%2" -i %1