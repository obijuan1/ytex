demo contains only umls concepts for fracture demo

mysql demo data:
select
CUI,
TUI,
STN,
STY,
ATUI,
coalesce(CVF,'')
into outfile 'E:/projects/ytex/data/mysql/umls/MRSTY.RRF'
fields terminated by '|' ESCAPED BY '' lines terminated by '\r\n'
from umls.MRSTY mst
inner join (select distinct code from anno_ontology_concept) c on mst.cui = c.code
;


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
into outfile 'E:/projects/ytex/data/mysql/umls/MRCONSO.RRF'
fields terminated by '|' ESCAPED BY '' lines terminated by '\r\n'
from umls.MRCONSO mrc
inner join
(
select distinct code from anno_ontology_concept
) c on mrc.cui = c.code
where mrc.sab = 'SNOMEDCT';

select fw.*
into outfile 'E:/projects/ytex/data/mysql/umls/umls_aui_fword.txt'
from umls_aui_fword fw
inner join umls.mrconso mrc on fw.aui = mrc.aui and mrc.sab = 'SNOMEDCT'
inner join
(
select distinct code from anno_ontology_concept
) c on c.code = mrc.cui;


mysql real data:
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
into outfile 'E:/projects/umls-new/mysql/MRCONSO.RRF'
fields terminated by '|' ESCAPED BY '' lines terminated by '\r\n'
from umls.MRCONSO mrc
where SAB in ('SNOMEDCT', 'RXNORM', 'SRC')
;


select
sty.CUI,
TUI,
STN,
STY,
ATUI,
coalesce(CVF,'')
into outfile 'E:/projects/umls-new/mysql/MRSTY.RRF'
fields terminated by '|' ESCAPED BY '' lines terminated by '\r\n'
from umls.MRSTY sty
inner join
	(
	select distinct cui
	from umls.MRCONSO
	where SAB in ('SNOMEDCT', 'RXNORM', 'SRC')
	) c on sty.cui = c.cui
;

select fw.*
into outfile 'E:/projects/umls-new/mysql/umls_aui_fword.txt'
from umls_aui_fword fw
inner join umls.mrconso mrc on fw.aui = mrc.aui and mrc.sab in ('SNOMEDCT', 'RXNORM', 'SRC')
;