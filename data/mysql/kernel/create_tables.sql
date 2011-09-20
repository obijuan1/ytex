
CREATE TABLE  kernel_eval (
	kernel_eval_id int(11) NOT NULL AUTO_INCREMENT,
	name varchar(50) NOT NULL DEFAULT '' comment 'corpus name',
	experiment varchar(50) not null comment 'experiment - type of kernel',
	label varchar(50) not NULL default '' comment 'class label',
	cv_fold_id int not null default 0 comment 'fk cv_fold',
	param1 double not null default 0,
	param2 varchar(50) not null default '',
	PRIMARY KEY (kernel_eval_id),
	UNIQUE KEY NK_kernel_eval (name, experiment, label, cv_fold_id, param1, param2)
) ENGINE=MyISAM comment 'set of all kernel evaluations';

create table kernel_eval_instance (
	kernel_eval_instance int not null auto_increment primary key,
	kernel_eval_id int not null comment 'fk kernel_eval',
	instance_id1 int(11) NOT NULL,
	instance_id2 int(11) NOT NULL,
	similarity double NOT NULL,
	KEY IX_kernel_eval1 (kernel_eval_id, instance_id1),
	KEY IX_kernel_eval2 (kernel_eval_id, instance_id2),
	UNIQUE KEY NK_kernel_eval (kernel_eval_id, instance_id1, instance_id2)
) ENGINE=MyISAM comment 'kernel instance evaluation';


create table tfidf_doclength (
  tfidf_doclength_id int auto_increment not null primary key,
  name varchar(255) not null default '',
  instance_id int not null,
  length int not null default 0,
  unique key nk_instance_id (name, instance_id)
) ENGINE=MyISAM comment 'doc length for calculating tf-idf';

create table tfidf_docfreq (
  tfidf_docfreq_id int auto_increment not null primary key,
  name varchar(255) not null default '',
  term varchar(50) not null,
  numdocs int not null default 0,
  unique key nk_docfreq (name, term)
) ENGINE=MyISAM comment 'num docs term occurs for calculating tf-idf';

create table tfidf_termfreq (
  tfidf_termfreq_id int auto_increment primary key,
  name varchar(50) not null,
  instance_id int not null,
  term varchar(50) not null,
  freq int not null,
  unique index NK_tfidif_termfreq (name, instance_id, term),
  index IX_instance(name, instance_id)
) ENGINE=MyISAM comment 'per-doc term count';

/*
DROP TABLE IF EXISTS `weka_results`;
CREATE TABLE  `weka_results` (
  `weka_result_id` int(11) NOT NULL AUTO_INCREMENT,
  `experiment` varchar(200) DEFAULT NULL,
  `label` varchar(200) DEFAULT NULL,
  `cost` double DEFAULT '0',
  `Key_Dataset` varchar(8000) DEFAULT NULL,
  `Key_Run` varchar(8000) DEFAULT NULL,
  `Key_Fold` varchar(8000) DEFAULT NULL,
  `Key_Scheme` varchar(8000) DEFAULT NULL,
  `Key_Scheme_options` varchar(8000) DEFAULT NULL,
  `Key_Scheme_version_ID` varchar(8000) DEFAULT NULL,
  `Date_time` double DEFAULT NULL,
  `Date_time2` double DEFAULT NULL,
  `Number_of_training_instances` double DEFAULT NULL,
  `Number_of_testing_instances` double DEFAULT NULL,
  `Number_correct` double DEFAULT NULL,
  `Number_incorrect` double DEFAULT NULL,
  `Number_unclassified` double DEFAULT NULL,
  `Percent_correct` double DEFAULT NULL,
  `Percent_incorrect` double DEFAULT NULL,
  `Percent_unclassified` double DEFAULT NULL,
  `Kappa_statistic` double DEFAULT NULL,
  `Mean_absolute_error` double DEFAULT NULL,
  `Root_mean_squared_error` double DEFAULT NULL,
  `Relative_absolute_error` double DEFAULT NULL,
  `Root_relative_squared_error` double DEFAULT NULL,
  `SF_prior_entropy` double DEFAULT NULL,
  `SF_scheme_entropy` double DEFAULT NULL,
  `SF_entropy_gain` double DEFAULT NULL,
  `SF_mean_prior_entropy` double DEFAULT NULL,
  `SF_mean_scheme_entropy` double DEFAULT NULL,
  `SF_mean_entropy_gain` double DEFAULT NULL,
  `KB_information` double DEFAULT NULL,
  `KB_mean_information` double DEFAULT NULL,
  `KB_relative_information` double DEFAULT NULL,
  `True_positive_rate` double DEFAULT NULL,
  `Num_true_positives` double DEFAULT NULL,
  `False_positive_rate` double DEFAULT NULL,
  `Num_false_positives` double DEFAULT NULL,
  `True_negative_rate` double DEFAULT NULL,
  `Num_true_negatives` double DEFAULT NULL,
  `False_negative_rate` double DEFAULT NULL,
  `Num_false_negatives` double DEFAULT NULL,
  `IR_precision` double DEFAULT NULL,
  `IR_recall` double DEFAULT NULL,
  `F_measure` double DEFAULT NULL,
  `Area_under_ROC` double DEFAULT NULL,
  `Weighted_avg_true_positive_rate` double DEFAULT NULL,
  `Weighted_avg_false_positive_rate` double DEFAULT NULL,
  `Weighted_avg_true_negative_rate` double DEFAULT NULL,
  `Weighted_avg_false_negative_rate` double DEFAULT NULL,
  `Weighted_avg_IR_precision` double DEFAULT NULL,
  `Weighted_avg_IR_recall` double DEFAULT NULL,
  `Weighted_avg_F_measure` double DEFAULT NULL,
  `Weighted_avg_area_under_ROC` double DEFAULT NULL,
  `Elapsed_Time_training` double DEFAULT NULL,
  `Elapsed_Time_testing` double DEFAULT NULL,
  `UserCPU_Time_training` double DEFAULT NULL,
  `UserCPU_Time_testing` double DEFAULT NULL,
  `Serialized_Model_Size` double DEFAULT NULL,
  `Serialized_Train_Set_Size` double DEFAULT NULL,
  `Serialized_Test_Set_Size` double DEFAULT NULL,
  `Summary` varchar(200) DEFAULT NULL,
  `measureNumSupportVectors` double DEFAULT NULL,
  `scutTP` int(11) DEFAULT NULL,
  `scutFP` int(11) DEFAULT NULL,
  `scutFN` int(11) DEFAULT NULL,
  `scutTN` int(11) DEFAULT NULL,
  `scutPrecision` double DEFAULT NULL,
  `scutRecall` double DEFAULT NULL,
  `scutFMeasure` double DEFAULT NULL,
  `weight` int(11) DEFAULT '0',
  `scutThreshold` double DEFAULT NULL,
  `degree` int(11) DEFAULT '0',
  `gamma` double DEFAULT '0',
  `kernel` int(11) DEFAULT NULL,
  PRIMARY KEY (`weka_result_id`),
  KEY `explabel` (`experiment`,`label`),
  KEY `kernel` (`experiment`,`label`,`kernel`,`cost`,`degree`,`gamma`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
*/

CREATE TABLE  `stopword` (
  `stopword` varchar(50) NOT NULL,
  PRIMARY KEY (`stopword`)
) ENGINE=MyISAM DEFAULT CHARSET=utf-8;

create table classifier_eval (
	classifier_eval_id int AUTO_INCREMENT not null primary key,
	name varchar(50) not null,
	experiment varchar(50) null default "",
	fold int null,
	run int null,
	algorithm varchar(50) null default "",
	label varchar(50) null default "",
	options varchar(1000) null default "",
	model longblob null,
	param1 double NULL,
	param2 varchar(50) NULL
) engine=myisam comment 'evaluation of a classifier on a dataset';

create table classifier_eval_svm (
	classifier_eval_id int not null comment 'fk classifier_eval' primary key,
	cost double DEFAULT '0',
  	weight varchar(50),
	degree int DEFAULT '0',
	gamma double DEFAULT '0',
	kernel int DEFAULT NULL,
	supportVectors int default null,
	vcdim double null
) engine=myisam comment 'evaluation of a libsvm classifier on a dataset';

create table classifier_eval_semil (
	classifier_eval_id int not null comment 'fk classifier_eval' primary key,
	distance varchar(50),
	degree int not null default 0,
	gamma double not null default 0,
	soft_label bit not null default 0,
	norm_laplace bit not null default 0,
	mu double not null default 0,
	lambda double not null default 0,
	pct_labeled double not null default 0
) engine=myisam comment 'evaluation of a semil classifier on a dataset';

create table classifier_eval_ir (
	classifier_eval_ir_id int not null auto_increment primary key,
	classifier_eval_id int not null comment 'fk classifier_eval',
	ir_class_id int not null comment 'class id for ir stats',
	tp int not null,
	tn int not null,
	fp int not null,
	fn int not null,
	ppv double not null,
	npv double not null,
	sens double not null,
	spec double not null,
	f1 double not null,
	unique key NK_classifier_eval_ir (classifier_eval_id, ir_class_id),
	key IX_classifier_eval_id (classifier_eval_id)
) engine=myisam comment 'ir statistics of a classifier on a dataset';

create table classifier_eval_irzv (
	classifier_eval_irzv_id int not null auto_increment primary key,
	classifier_eval_id int not null comment 'fk classifier_eval',
	ir_class_id int not null comment 'class id for ir stats',
	tp int not null,
	tn int not null,
	fp int not null,
	fn int not null,
	ppv double not null,
	npv double not null,
	sens double not null,
	spec double not null,
	f1 double not null,
	unique key NK_classifier_eval_ir (classifier_eval_id, ir_class_id),
	key IX_classifier_eval_id (classifier_eval_id)
) engine=myisam comment 'ir statistics of a classifier on a dataset with zero vectors added';

create table classifier_instance_eval (
	classifier_instance_eval_id int not null auto_increment primary key,
	classifier_eval_id int not null comment 'fk classifier_eval',
	instance_id bigint not null,
	pred_class_id int not null,
	target_class_id int null,
	unique key nk_result (classifier_eval_id, instance_id)
) engine=myisam comment 'instance classification result';

create table classifier_instance_eval_prob (
	classifier_eval_result_prob_id int not null auto_increment primary key,
	classifier_instance_eval_id int comment 'fk classifier_instance_eval',
	class_id int not null,
	probability double not null,
	unique key nk_result_prob (classifier_instance_eval_id, class_id)
) engine=myisam comment 'probability of belonging to respective class';


create table cv_fold (
  cv_fold_id int auto_increment not null primary key,
  corpus_name varchar(50) not null comment 'corpus name',
  split_name varchar(50) not null default '' comment 'split/subset name',
  label varchar(50) not null default '' ,
  run int not null default 0,
  fold int not null default 0,
  unique index nk_cv_fold (corpus_name, split_name, label, run, fold)
)engine=myisam ;

create table cv_fold_instance (
  cv_fold_instance_id int auto_increment not null primary key,
  cv_fold_id int not null,
  instance_id int not null,
  train bit not null default 0,
  unique index nk_cv_fold_instance (cv_fold_id, instance_id, train)
) engine=myisam ;


create table feature_eval (
  feature_eval_id int auto_increment not null primary key,
  corpus_name varchar(50) not null comment 'corpus name',
  featureset_name varchar(50) not null default '' comment 'feature set name',
  label varchar(50) not null default ''  comment 'label wrt features evaluated',
  cv_fold_id int not null default 0 comment 'fold wrt features evaluated',
  param1 varchar(50) not null default '' comment 'meta-parameter for feature evaluation',
  type varchar(50) not null comment 'metric used to evaluate features',
  unique index nk_feature_eval(corpus_name, featureset_name, label, cv_fold_id, param1, type),
  index ix_feature_eval(corpus_name, cv_fold_id, type)
) engine=myisam comment 'evaluation of a set of features in a corpus';

create table feature_rank (
  feature_rank_id int auto_increment not null primary key,
  feature_eval_id int not null comment 'fk feature_eval',
  feature_name varchar(50) not null comment 'name of feature',
  evaluation double not null comment 'measurement of feature worth',
  rank int not null comment 'rank among all features',
  unique index nk_feature_name(feature_eval_id, feature_name),
  unique index ix_feature_rank(feature_eval_id, rank),
  index fk_feature_eval(feature_eval_id)
) engine=myisam comment 'evaluation of a feature in a corpus';

create table hotspot (
  hotspot_id int auto_increment not null primary key,
  instance_id int not null comment 'fk cv_fold_instance',
  anno_base_id int not null comment 'fk anno_base_id',
  feature_rank_id int not null comment 'fk feature_rank',
  unique index NK_hotspot (instance_id, anno_base_id, feature_rank_id)
) engine=myisam ;
ALTER TABLE `hotspot` ADD INDEX `ix_instance_id`(`instance_id`),
 ADD INDEX `ix_anno_base_id`(`anno_base_id`),
 ADD INDEX `ix_feature_rank_id`(`feature_rank_id`);

create table hotspot_zero_vector (
  hotspot_zero_vector_id int auto_increment not null primary key,
  corpus_name varchar(50) not null comment 'corpus/feature set name',
  experiment varchar(50) not null default '',
  label varchar(50) not null default '' comment 'class label',
  instance_id int not null comment 'instance id',
  cutoff double not null comment 'hotspot infogain cutoff',
  unique index nk_zero_vector (corpus_name, experiment, label, instance_id, cutoff),
  index ix_instance_id (corpus_name, experiment, label, instance_id)
) engine = myisam comment 'zero vectors for given cutoff';

create table hotspot_instance (
    hotspot_instance_id int auto_increment primary key,
    corpus_name varchar(50) not null,
    experiment varchar(50) not null default '',
    label varchar(50) not null default '',
    instance_id int not null,
    unique index NK_hotspot_instance (corpus_name, experiment, label, instance_id)
) engine=myisam comment 'hotspot features for an instance';

create table hotspot_sentence (
    hotspot_sentence_id int auto_increment not null primary key,
    hotspot_instance_id int not null comment 'fk hotspot_instance',
    anno_base_id int not null comment 'fk anno_sentence',
    evaluation double not null comment 'max eval from hotspot',
    section varchar(50) not null default '' comment 'section containing this sentence',
    unique index NK_hotspot_sentence (hotspot_instance_id, anno_base_id),
    index FK_hotspot_instance_id (hotspot_instance_id),
    index FK_anno_base_id (anno_base_id),
    INDEX IX_evaluation (hotspot_instance_id, evaluation)
) engine = myisam comment 'sentences that contain hotspots at specified threshold';

create table hotspot_feature_eval (
    hotspot_feature_eval_id int auto_increment primary key,
    hotspot_instance_id int not null comment 'fk hotspot_instance',
    feature_type varchar(10) not null default '' comment 'type of feature, e.g. word/cui',
    feature_name varchar(50) not null,
    evaluation double not null comment 'max eval that caused this feature to be included',
    unique index NK_hotspot_feature_eval (hotspot_instance_id, feature_type, feature_name),
    index IX_evaluation (hotspot_instance_id, evaluation),
    index fk_hotspot_instance (hotspot_instance_id)
) engine=myisam comment 'hotspot features for an instance';


create table hotspot_zero_vector_tt
(
  hotspot_zero_vector_tt int auto_increment not null primary key,
  name varchar(50) not null comment 'corpus name',
  experiment varchar(50) not null comment 'classifier_eval.experiment',
  label varchar(50) not null comment 'classifier_eval.label',
  run int not null default 0 comment 'classifier_eval.run',
  fold int not null default 0 comment 'classifier_eval.fold',
  ir_class_id int not null comment 'truth table wrt this class',
  cutoff double not null comment 'zero vector cutoff',
  tp int not null,
  tn int not null,
  fp int not null,
  fn int not null,
  unique key NK_hotspot_zero_vector_tt (name, experiment, label, run, fold, ir_class_id, cutoff)
) engine=myisam comment 'truth table for hotspot zero vectors'
;

/*
create view v_classifier_eval_ir_classes
as
select distinct ce.classifier_eval_id, target_class_id ir_class_id
from classifier_eval ce
inner join classifier_instance_eval ci
on ce.classifier_eval_id = ci.classifier_eval_id
;

create view v_classifier_eval_ir_tt
as
select cls.classifier_eval_id, ir_class_id,
  sum(case
    when ir_class_id = target_class_id and ir_class_id = pred_class_id then 1
    else 0
  end) tp,
  sum(case
    when ir_class_id <> target_class_id and ir_class_id <> pred_class_id then 1
    else 0
  end) tn,
  sum(case
    when ir_class_id <> target_class_id and ir_class_id = pred_class_id then 1
    else 0
  end) fp,
  sum(case
    when ir_class_id = target_class_id and ir_class_id <> pred_class_id then 1
    else 0
  end) fn
from v_classifier_eval_ir_classes cls
inner join classifier_instance_eval ci on cls.classifier_eval_id = ci.classifier_eval_id
group by classifier_eval_id, ir_class_id
;

create view v_classifier_eval_ir
as
select *,
  case when tp+fp > 0 then tp/(tp+fp) else 0 end prec,
  case when tp+fn > 0 then tp/(tp+fn) else 0 end sens,
  case when fp+tn > 0 then tn/(fp+tn) else 0 end spec,
  case when fn+tn > 0 then tn/(fn+tn) else 0 end npv,
  case when (tp+fp) > 0 and (tp+fn) > 0 then 2*(tp/(tp+fp))*(tp/(tp+fn))/(tp/(tp+fn) + tp/(tp+fp)) else 0 end f1
from v_classifier_eval_ir_tt
;


*/