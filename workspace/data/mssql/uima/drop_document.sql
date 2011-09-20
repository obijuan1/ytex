-- drop 'operational' data
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[fracture_demo]') AND type in (N'U'))
	drop table $(db_schema).fracture_demo;
go
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[document_class]') AND type in (N'U'))
	drop table $(db_schema).document_class;
go
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[anno_contain]') AND type in (N'U'))
	drop table $(db_schema).anno_contain;
go
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[anno_source_doc_info]') AND type in (N'U'))
	drop table $(db_schema).anno_source_doc_info;
go
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[anno_num_token]') AND type in (N'U'))
	drop table $(db_schema).anno_num_token;
go
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[anno_word_token]') AND type in (N'U'))
	drop table $(db_schema).anno_word_token;
go
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[anno_base_token]') AND type in (N'U'))
	drop table $(db_schema).anno_base_token;
go
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[anno_segment]') AND type in (N'U'))
	drop table $(db_schema).anno_segment;
go
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[anno_umls_concept]') AND type in (N'U'))
	drop table $(db_schema).anno_umls_concept;
go
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[anno_ontology_concept]') AND type in (N'U'))
	drop table $(db_schema).anno_ontology_concept;
go
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[anno_named_entity]') AND type in (N'U'))
	drop table $(db_schema).anno_named_entity;
go
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[anno_sentence]') AND type in (N'U'))
	drop table $(db_schema).anno_sentence;
go
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[anno_date]') AND type in (N'U'))
	drop table $(db_schema).anno_date;
go
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[anno_base]') AND type in (N'U'))
	drop table $(db_schema).anno_base;
go
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[document]') AND type in (N'U'))
	drop table $(db_schema).document;
go
