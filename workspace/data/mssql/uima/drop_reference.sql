-- drop 'reference' data
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[hibernate_sequences]') AND type in (N'U'))
	drop table $(db_schema).hibernate_sequences
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[anno_base_sequence]') AND type in (N'U'))
	drop table $(db_schema).anno_base_sequence
;




IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[ref_uima_type]') AND type in (N'U'))
	drop table $(db_schema).ref_uima_type
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[ref_named_entity_regex]') AND type in (N'U'))
	drop table $(db_schema).ref_named_entity_regex
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[ref_segment_regex]') AND type in (N'U'))
	drop table $(db_schema).ref_segment_regex
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[ref_stopword]') AND type in (N'U'))
	drop table $(db_schema).ref_stopword
;
