create table $(db_schema).umls_aui_fword (
	aui varchar(9) not null primary key,
	fword nvarchar(100) not null,
	fstem nvarchar(100) null,
	tok_str nvarchar(250) not null,
	stem_str nvarchar(250) null
);