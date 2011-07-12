load data
infile umls_snomed_map.txt
into table umls_snomed_map
fields terminated by '\t'
 (
  cui CHAR TERMINATED BY WHITESPACE,
  code CHAR TERMINATED BY WHITESPACE
)
