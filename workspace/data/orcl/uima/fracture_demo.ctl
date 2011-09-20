options (direct=true)
load data
characterset UTF8 length semantics char
infile '../../examples/fracture_demo.txt'
badfile 'fracture_demo.bad'
discardfile 'fracture_demo.dsc'
truncate
into table fracture_demo
fields terminated by '\t'
trailing nullcols
(note_id integer external,
site_id char(10),
note_text char(3000),
fracture char(20),
note_set char(10)
)