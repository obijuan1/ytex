-- drop 'reference' data
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[ref_uima_type]') AND type in (N'U'))
	drop table $(db_schema).ref_uima_type;
go
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[ref_named_entity_regex]') AND type in (N'U'))
	drop table $(db_schema).ref_named_entity_regex;
go
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[ref_segment_regex]') AND type in (N'U'))
	drop table $(db_schema).ref_segment_regex;
go
