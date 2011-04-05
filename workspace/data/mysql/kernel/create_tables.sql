DROP TABLE IF EXISTS `kernel_eval`;
CREATE TABLE  `kernel_eval` (
  `kernel_eval_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL DEFAULT '',
  `instance_id1` int(11) NOT NULL,
  `instance_id2` int(11) NOT NULL,
  `similarity` double NOT NULL,
  PRIMARY KEY (`kernel_eval_id`),
  UNIQUE KEY `NK_kernel_eval` (`name`,`instance_id1`,`instance_id2`),
  KEY `NK_kernel_eval1` (`name`,`instance_id1`),
  KEY `NK_kernel_eval2` (`name`,`instance_id2`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;


drop table if exists concept_graph;
create table concept_graph (
  concept_graph_id int auto_increment not null primary key,
  depthMax int not null,
  conceptMap longblob not null,
  sabs varchar(1000) not null default ''
);

drop table if exists concept_graph_root;
create table concept_graph_root (
	concept_graph_id int not null comment 'fk concept_graph',
	cui char(10) not null,
	primary key (concept_graph_id, cui)
);

drop table if exists corpus;
create table corpus (
  corpus_id int auto_increment not null primary key,
  corpus_name varchar(100) not null,
  unique key corpus_name (corpus_name)
);

drop table if exists corpus_term;
create table corpus_term (
  corpus_term_id int auto_increment not null primary key,
  corpus_id int not null comment 'fk corpus',
  concept_id varchar(10) not null,
  frequency double not null default 0,
  info_content double null,
  unique key nk_corpus_term (corpus_id, concept_id)
);

drop table if exists info_content;
create table info_content (
	info_content_id int auto_increment not null primary key,
	corpus_id int not null comment 'fk corpus',
	concept_graph_id int not null default 0 comment 'fk concept_graph',
	concept_id char(10) not null,
	frequency double not null default 0,
	info_content double not null default 0,
	unique key nk_info_content (corpus_id, concept_graph_id, concept_id)
);


drop table if exists tfidf_doclength; 
create table tfidf_doclength (
  tfidf_doclength_id int auto_increment not null primary key,
  name varchar(255) not null default '',
  instance_id int not null,
  length int not null default 0,
  unique key nk_instance_id (name, instance_id)
) comment 'doc length for calculating tf-idf';

drop table if exists tfidf_docfreq; 
create table tfidf_docfreq (
  tfidf_docfreq_id int auto_increment not null primary key,
  name varchar(255) not null default '',
  term varchar(50) not null,
  numdocs int not null default 0,
  unique key nk_docfreq (name, term)
) comment 'num docs term occurs for calculating tf-idf';

drop table tfidf_termfreq;
create table tfidf_termfreq (
  tfidf_termfreq_id int auto_increment primary key,
  name varchar(50) not null,
  instance_id int not null,
  term varchar(50) not null,
  freq int not null,
  unique index NK_tfidif_termfreq (name, instance_id, term),
  index IX_instance(name, instance_id)
) comment 'per-doc term count';

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


DROP TABLE IF EXISTS `stopword`;
CREATE TABLE  `stopword` (
  `stopword` varchar(50) NOT NULL,
  PRIMARY KEY (`stopword`)
) ENGINE=MyISAM DEFAULT CHARSET=utf-8;

create table classifier_eval (
	classifier_eval_id int AUTO_INCREMENT not null primary key,
	name varchar(50) not null,
	experiment varchar(50) null default "",
	fold varchar(50) null default "",
	algorithm varchar(50) null default "",
	label varchar(50) null default "",
	options varchar(1000) null default "",
	model longblob null
) comment 'evaluation of a classifier on a dataset';

create table classifier_eval_libsvm (
	classifier_eval_id int not null comment 'fk classifier_eval' primary key,
	cost double DEFAULT '0',
  	weight int DEFAULT '0',
	degree int DEFAULT '0',
	gamma double DEFAULT '0',
	kernel int DEFAULT NULL,
	supportVectors int default null
) comment 'evaluation of a libsvm classifier on a dataset';

create table classifier_instance_eval (
	classifier_instance_eval_id int not null auto_increment primary key,
	classifier_eval_id int not null comment 'fk classifier_eval',
	instance_id int not null,
	pred_class_id int not null,
	target_class_id int null,
	unique key nk_result (classifier_eval_id, instance_id)
) comment 'instance classification result';

create table classifier_instance_eval_prob (
	classifier_eval_result_prob_id int not null auto_increment primary key,
	classifier_instance_eval_id int comment 'fk classifier_instance_eval',
	class_id int not null,
	probability double not null,
	unique key nk_result_prob (classifier_instance_eval_id, class_id)
) comment 'probability of belonging to respective class';

drop table cv_fold;
drop table cv_fold_instance;
create table cv_fold (
  cv_fold_id int auto_increment not null primary key,
  name varchar(50) not null,
  label varchar(50) not null,
  run int null,
  fold int null,
  unique index nk_cv_fold (name, label, run, fold)
);

create table cv_fold_instance (
  cv_fold_instance_id int auto_increment not null primary key,
  cv_fold_id int not null,
  instance_id int not null,
  train bit not null default 0,
  unique index nk_cv_fold_instance (cv_fold_id, instance_id, train)
);
create table feature_infogain (
  feature_infogain_id int auto_increment not null primary key,
  name varchar(50) not null,
  label varchar(50) not null,
  cv_fold_id int null,
  feature_name varchar(50) not null,
  infogain double not null,
  rank int not null,
  unique index nk_feature_infogain(name, cv_fold_id, feature_name)
);


drop table feature_eval;
create table feature_eval (
  feature_eval_id int auto_increment not null primary key,
  name varchar(50) not null,
  label varchar(50) not null,
  cv_fold_id int null,
  type varchar(50) not null,
  unique index nk_feature_eval(name, label, cv_fold_id, type),
  index ix_feature_eval(name, cv_fold_id, type)
);

drop table feature_rank;
create table feature_rank (
  feature_rank_id int auto_increment not null primary key,
  feature_eval_id int not null comment 'fk feature_eval',
  feature_name varchar(50) not null,
  infogain double not null,
  rank int not null,
  unique index nk_feature_rank(feature_eval_id, feature_name),
  index fk_feature_eval(feature_eval_id)
);



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

