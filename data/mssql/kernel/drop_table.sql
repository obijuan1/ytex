IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[hotspot]') AND type in (N'U'))
	drop TABLE  $(db_schema).hotspot
go

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[hotspot_zero_vector]') AND type in (N'U'))
	drop TABLE  $(db_schema).hotspot_zero_vector
go

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[hotspot_zero_vector_tt]') AND type in (N'U'))
	drop TABLE  $(db_schema).hotspot_zero_vector_tt
go

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[hotspot_feature_eval]') AND type in (N'U'))
	drop TABLE  $(db_schema).hotspot_feature_eval
go

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[hotspot_sentence]') AND type in (N'U'))
	drop TABLE  $(db_schema).hotspot_sentence
go

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[feature_rank]') AND type in (N'U'))
	drop TABLE  $(db_schema).feature_rank
go

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[feature_eval]') AND type in (N'U'))
	drop TABLE  $(db_schema).feature_eval
go

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[tfidf_termfreq]') AND type in (N'U'))
	drop TABLE  $(db_schema).tfidf_termfreq
go

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[tfidf_doclength]') AND type in (N'U'))
	drop TABLE  $(db_schema).tfidf_doclength
go

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[tfidf_docfreq]') AND type in (N'U'))
	drop TABLE  $(db_schema).tfidf_docfreq
go

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[stopword]') AND type in (N'U'))
	drop TABLE  $(db_schema).stopword
go

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[classifier_eval_ir]') AND type in (N'U'))
DROP TABLE $(db_schema).[classifier_eval_ir]
GO

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[classifier_eval_svm]') AND type in (N'U'))
DROP TABLE $(db_schema).[classifier_eval_svm]
GO

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[classifier_eval_semil]') AND type in (N'U'))
DROP TABLE $(db_schema).[classifier_eval_semil]
GO

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[classifier_instance_eval_prob]') AND type in (N'U'))
DROP TABLE $(db_schema).[classifier_instance_eval_prob]
GO

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[classifier_instance_eval]') AND type in (N'U'))
DROP TABLE $(db_schema).[classifier_instance_eval]
GO

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[classifier_eval]') AND type in (N'U'))
DROP TABLE $(db_schema).[classifier_eval]
GO

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[cv_fold]') AND type in (N'U'))
DROP TABLE $(db_schema).[cv_fold]
GO

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[cv_fold_instance]') AND type in (N'U'))
DROP TABLE $(db_schema).[cv_fold_instance]
GO

