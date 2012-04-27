IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[umls_aui_fword]') AND type in (N'U'))
	drop table $(db_schema).[umls_aui_fword]
;

