IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[V_ANNOTATION]') AND type in (N'V'))
	drop view $(db_schema).[V_ANNOTATION];
go

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[v_document_cui_sent]') AND type in (N'V'))
	drop view $(db_schema).v_document_cui_sent
go

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[V_DOCUMENT_ONTOANNO]') AND type in (N'V'))
	drop VIEW $(db_schema).[V_DOCUMENT_ONTOANNO]
go

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[v_anno_segment]') AND type in (N'V'))
	drop view $(db_schema).v_anno_segment
go

