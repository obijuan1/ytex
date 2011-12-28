IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[V_DOCUMENT]') AND type in (N'V'))
	drop view $(db_schema).[V_DOCUMENT]
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[V_ANNOTATION]') AND type in (N'V'))
	drop view $(db_schema).[V_ANNOTATION]
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[v_document_cui_sent]') AND type in (N'V'))
	drop view $(db_schema).v_document_cui_sent
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[V_DOCUMENT_ONTOANNO]') AND type in (N'V'))
	drop VIEW $(db_schema).[V_DOCUMENT_ONTOANNO]
;

