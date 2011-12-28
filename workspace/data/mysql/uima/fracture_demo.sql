load data local infile './examples/fracture_demo.txt' 
into table fracture_demo 
fields terminated by '\t' 
ESCAPED BY '' 
lines terminated by '\r\n'
;

update fracture_demo set note_text = replace(note_text, '<br/>', CHAR(10));