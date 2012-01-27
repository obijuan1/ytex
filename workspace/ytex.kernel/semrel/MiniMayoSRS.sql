create table minimayosrs 
(
    physicians double,
    coders double,
    cui1 char(8),
    cui2 char(8),
    term1 varchar(50),
    term2 varchar(50),
    conceptId1 bigint,
    conceptId2 bigint
) engine = myisam;

load data local infile 'MiniMayoSRS.csv'
into table minimayosrs
FIELDS terminated by ','
lines terminated by '\r\n'
ignore 1 lines;