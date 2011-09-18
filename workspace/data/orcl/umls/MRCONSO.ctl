options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRCONSO.rrf' 
badfile 'MRCONSO.bad'
discardfile 'MRCONSO.dsc'
truncate
into table MRCONSO
fields terminated by '|'
trailing nullcols
(CUI	char(8),
LAT	char(3),
TS	char(1),
LUI	char(10),
STT	char(3),
SUI	char(10),
ISPREF	char(1),
AUI	char(9),
SAUI	char(50),
SCUI	char(50),
SDUI	char(50),
SAB	char(20),
TTY	char(20),
CODE	char(50),
STR	char(3000),
SRL	integer external,
SUPPRESS	char(1),
CVF	integer external
)