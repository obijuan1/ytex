options (direct=true)
load data
characterset UTF8 length semantics char
infile 'umls_aui_fword.txt' 
badfile 'umls_aui_fword.bad'
discardfile 'umls_aui_fword.dsc'
truncate
into table umls_aui_fword
fields terminated by '\t'
trailing nullcols
(
aui char(9),
fword	char(100),
fstem char(100),
tok_str char(250),
stem_str char(250)
)