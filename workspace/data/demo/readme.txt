demo contains only umls concepts for fracture demo

mysql demo data:
select fw.*
into outfile 'e:/temp/umls_aui_fword.txt'
from umls_aui_fword fw
inner join umls.mrconso mrc on fw.aui = mrc.aui
inner join
(
select distinct code from anno_ontology_concept
) c on c.code = mrc.cui;


select CUI,
LAT,
TS,
LUI,
STT,
SUI,
ISPREF,
AUI,
coalesce(SAUI,''),
coalesce(SCUI,''),
coalesce(SDUI,''),
SAB,
TTY,
mrc.CODE,
STR,
SRL,
SUPPRESS,
coalesce(CVF,'')
into outfile 'e:/temp/MRCONSO.RRF'
fields terminated by '|' ESCAPED BY '' lines terminated by '\r\n'
from umls.MRCONSO mrc
inner join
(
select distinct code from anno_ontology_concept
) c on mrc.cui = c.code;

mysql real data:
mysqldump --tab --user=ytex --password=ytex ytex umls_aui_fword > umls_aui_fword.txt
and zip up the MRCONSO.rrf file