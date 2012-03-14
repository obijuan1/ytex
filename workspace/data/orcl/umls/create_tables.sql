create table umls_aui_fword (
	aui varchar2(9) not null primary key,
	fword varchar2(100) not null,
	fstem varchar2(100) null,
	tok_str varchar2(250) not null,
	stem_str varchar2(250) null
);

