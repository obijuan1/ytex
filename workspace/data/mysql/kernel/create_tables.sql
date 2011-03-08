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