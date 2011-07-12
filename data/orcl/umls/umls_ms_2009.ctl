load data
infile umls_ms_2009.txt
into table umls_ms_2009
fields terminated by '\t'
 (
  cui CHAR TERMINATED BY WHITESPACE,
  fword CHAR TERMINATED BY WHITESPACE,
  text CHAR(1024),
  code CHAR TERMINATED BY WHITESPACE,
  sourcetype CHAR TERMINATED BY WHITESPACE,
  tui CHAR TERMINATED BY WHITESPACE
)
