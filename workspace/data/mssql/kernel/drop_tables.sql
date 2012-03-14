IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[hotspot_sentence]') AND type in (N'U'))
	drop TABLE  $(db_schema).hotspot_sentence
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[hotspot_instance]') AND type in (N'U'))
	DROP TABLE $(db_schema).[hotspot_instance]
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[hotspot]') AND type in (N'U'))
	drop TABLE  $(db_schema).hotspot
;


IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[feature_parchd]') AND type in (N'U'))
	drop TABLE  $(db_schema).feature_parchd
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[feature_rank]') AND type in (N'U'))
	drop TABLE  $(db_schema).feature_rank
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[tfidf_doclength]') AND type in (N'U'))
	drop TABLE  $(db_schema).tfidf_doclength
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[feature_eval]') AND type in (N'U'))
	drop TABLE  $(db_schema).feature_eval
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[classifier_eval_ir]') AND type in (N'U'))
DROP TABLE $(db_schema).[classifier_eval_ir]
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[classifier_eval_svm]') AND type in (N'U'))
DROP TABLE $(db_schema).[classifier_eval_svm]
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[classifier_eval_semil]') AND type in (N'U'))
DROP TABLE $(db_schema).[classifier_eval_semil]
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[classifier_instance_eval_prob]') AND type in (N'U'))
DROP TABLE $(db_schema).[classifier_instance_eval_prob]
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[classifier_instance_eval]') AND type in (N'U'))
DROP TABLE $(db_schema).[classifier_instance_eval]
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[classifier_eval]') AND type in (N'U'))
DROP TABLE $(db_schema).[classifier_eval]
;


IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[cv_fold_instance]') AND type in (N'U'))
DROP TABLE $(db_schema).[cv_fold_instance]
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[cv_fold]') AND type in (N'U'))
DROP TABLE $(db_schema).[cv_fold]
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[cv_best_svm]') AND type in (N'U'))
DROP TABLE $(db_schema).[cv_best_svm]
;


IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[kernel_eval_instance]') AND type in (N'U'))
DROP TABLE $(db_schema).[kernel_eval_instance]
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[kernel_eval]') AND type in (N'U'))
DROP TABLE $(db_schema).[kernel_eval]
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[v_corpus_group_class]') AND type in (N'V'))
drop VIEW $(db_schema).[v_corpus_group_class]
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[corpus_label]') AND type in (N'U'))
DROP TABLE $(db_schema).[corpus_label]
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).[corpus_doc]') AND type in (N'U'))
DROP TABLE $(db_schema).[corpus_doc]
;




