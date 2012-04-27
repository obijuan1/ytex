IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[v_snomed_fword_lookup]') AND type in (N'V'))
	drop view $(db_schema).v_snomed_fword_lookup
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[v_snomed_fword_lookup]') AND type in (N'U'))
	drop table $(db_schema).v_snomed_fword_lookup
;
