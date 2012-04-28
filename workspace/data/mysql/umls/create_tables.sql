
create table umls_aui_fword (
	aui varchar(10) not null primary key,
	fword varchar(70) not null,
	fstem varchar(70) null,
	tok_str varchar(250) not null,
	stem_str varchar(250) null
) engine=myisam, CHARACTER SET utf8;

