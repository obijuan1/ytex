options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRSTY.rrf'
badfile 'MRSTY.bad'
discardfile 'MRSTY.dsc'
truncate
into table MRSTY
fields terminated by '|'
trailing nullcols
(CUI	char(8),
TUI	char(4),
STY	char(50)
)