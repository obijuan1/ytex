-- drop 'reference' data
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).ref_document_type') AND type in (N'U'))
	drop table $(db_schema).ref_document_type
go

