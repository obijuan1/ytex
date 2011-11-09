IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[v_classifier_eval_ir]') AND type in (N'V'))
	drop view $(db_schema).v_classifier_eval_ir
go
