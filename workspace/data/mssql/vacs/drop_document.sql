IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).anno_docdate') AND type in (N'U'))
	drop table $(db_schema).anno_docdate;
go
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).anno_dockey') AND type in (N'U'))
	drop table $(db_schema).anno_dockey;
go
