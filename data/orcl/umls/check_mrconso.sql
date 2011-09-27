/*
 * simply sqlplus script to check the existence of the mrconso table
 * write to file mrconso-check.txt
 */
set pagesize 0;
set trimspool on;
set headsep off;  
SET VERIFY OFF;
SET HEADING OFF;

spool 'mrconso-check.txt' 
select count(*) from all_tables where lower(table_name) = 'mrconso' and lower(owner) = lower('&1');
spool off
exit