/*
CREATE TABLE $(db_schema).umls_ms_2009 (
  cui nvarchar(10) NOT NULL,
  fword nvarchar(80) NOT NULL,
  text ntext,
  code nvarchar(45) NOT NULL,
  sourcetype nvarchar(45) NOT NULL,
  tui nvarchar(4) NOT NULL
);


CREATE TABLE $(db_schema).umls_snomed_map (
  cui nvarchar(10) NOT NULL,
  code nvarchar(45) NOT NULL,
);
*/

create table $(db_schema).umls_aui_fword (
	aui nvarchar(9) not null primary key,
	fword nvarchar(100) not null,
	fstem nvarchar(100) null,
	tok_str nvarchar(250) not null,
	stem_str nvarchar(250) null
);