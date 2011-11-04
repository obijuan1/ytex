/*
CREATE TABLE umls_ms_2009 (
  cui varchar(10) NOT NULL,
  fword varchar(80) NOT NULL,
  text text,
  code varchar(45) NOT NULL,
  sourcetype varchar(45) NOT NULL,
  tui varchar(4) NOT NULL
) engine=myisam, CHARACTER SET utf8;


CREATE TABLE umls_snomed_map (
  cui varchar(10) NOT NULL,
  code varchar(45) NOT NULL
) engine=myisam, CHARACTER SET utf8;

load data local infile 'umls_ms_2009.txt'
into table umls_ms_2009
;

load data local infile 'umls_snomed_map.txt'
into table umls_snomed_map
;
*/

create table umls_aui_fword (
	aui varchar(10) not null primary key,
	fword varchar(100) not null,
	fstem varchar(100) null,
	tok_str varchar(250) not null,
	stem_str varchar(250) null
) engine=myisam, CHARACTER SET utf8;

