drop table $(db_schema).cv_fold;
drop table $(db_schema).cv_fold_instance;
create table $(db_schema).cv_fold (
  cv_fold_id int identity not null primary key,
  name varchar(50) not null,
  label varchar(50) not null,
  run int null,
  fold int null
);
create unique index nk_cv_fold on $(db_schema).cv_fold(name, label, run, fold);

create table $(db_schema).cv_fold_instance (
  cv_fold_instance_id int identity not null primary key,
  cv_fold_id int not null,
  instance_id int not null,
  train bit not null default 0
);
create unique index nk_cv_fold_instance on $(db_schema).cv_fold_instance(cv_fold_id, instance_id, train)


create table $(db_schema).kernel_eval (
  kernel_eval_id int identity not null primary key,
  name varchar(255) not null default '',
  instance_id1 int not null,
  instance_id2 int not null,
  similarity float not null
);

CREATE UNIQUE INDEX NK_kernel_eval ON $(db_schema).kernel_eval
(
	name, instance_id1, instance_id2
);

create index IX_kernel_eval1 on $(db_schema).kernel_eval(name, instance_id1);
create index IX_kernel_eval2 on $(db_schema).kernel_eval(name, instance_id2);




CREATE TABLE $(db_schema).weka_results (
  weka_result_id int identity NOT NULL PRIMARY KEY ,
  experiment varchar(200) DEFAULT NULL,
  label varchar(200) DEFAULT NULL,
  cost float DEFAULT '0',
  Key_Dataset varchar(8000) DEFAULT NULL,
  Key_Run varchar(8000) DEFAULT NULL,
  Key_Fold varchar(8000) DEFAULT NULL,
  Key_Scheme varchar(8000) DEFAULT NULL,
  Key_Scheme_options varchar(8000) DEFAULT NULL,
  Key_Scheme_version_ID varchar(8000) DEFAULT NULL,
  Date_time float DEFAULT NULL,
  Date_time2 float DEFAULT NULL,
  Number_of_training_instances float DEFAULT NULL,
  Number_of_testing_instances float DEFAULT NULL,
  Number_correct float DEFAULT NULL,
  Number_incorrect float DEFAULT NULL,
  Number_unclassified float DEFAULT NULL,
  Percent_correct float DEFAULT NULL,
  Percent_incorrect float DEFAULT NULL,
  Percent_unclassified float DEFAULT NULL,
  Kappa_statistic float DEFAULT NULL,
  Mean_absolute_error float DEFAULT NULL,
  Root_mean_squared_error float DEFAULT NULL,
  Relative_absolute_error float DEFAULT NULL,
  Root_relative_squared_error float DEFAULT NULL,
  SF_prior_entropy float DEFAULT NULL,
  SF_scheme_entropy float DEFAULT NULL,
  SF_entropy_gain float DEFAULT NULL,
  SF_mean_prior_entropy float DEFAULT NULL,
  SF_mean_scheme_entropy float DEFAULT NULL,
  SF_mean_entropy_gain float DEFAULT NULL,
  KB_information float DEFAULT NULL,
  KB_mean_information float DEFAULT NULL,
  KB_relative_information float DEFAULT NULL,
  True_positive_rate float DEFAULT NULL,
  Num_true_positives float DEFAULT NULL,
  False_positive_rate float DEFAULT NULL,
  Num_false_positives float DEFAULT NULL,
  True_negative_rate float DEFAULT NULL,
  Num_true_negatives float DEFAULT NULL,
  False_negative_rate float DEFAULT NULL,
  Num_false_negatives float DEFAULT NULL,
  IR_precision float DEFAULT NULL,
  IR_recall float DEFAULT NULL,
  F_measure float DEFAULT NULL,
  Area_under_ROC float DEFAULT NULL,
  Weighted_avg_true_positive_rate float DEFAULT NULL,
  Weighted_avg_false_positive_rate float DEFAULT NULL,
  Weighted_avg_true_negative_rate float DEFAULT NULL,
  Weighted_avg_false_negative_rate float DEFAULT NULL,
  Weighted_avg_IR_precision float DEFAULT NULL,
  Weighted_avg_IR_recall float DEFAULT NULL,
  Weighted_avg_F_measure float DEFAULT NULL,
  Weighted_avg_area_under_ROC float DEFAULT NULL,
  Elapsed_Time_training float DEFAULT NULL,
  Elapsed_Time_testing float DEFAULT NULL,
  UserCPU_Time_training float DEFAULT NULL,
  UserCPU_Time_testing float DEFAULT NULL,
  Serialized_Model_Size float DEFAULT NULL,
  Serialized_Train_Set_Size float DEFAULT NULL,
  Serialized_Test_Set_Size float DEFAULT NULL,
  Summary varchar(200) DEFAULT NULL,
  measureNumSupportVectors float DEFAULT NULL,
  scutTP int DEFAULT NULL,
  scutFP int DEFAULT NULL,
  scutFN int DEFAULT NULL,
  scutTN int DEFAULT NULL,
  scutPrecision float DEFAULT NULL,
  scutRecall float DEFAULT NULL,
  scutFMeasure float DEFAULT NULL,
  weight int DEFAULT '0',
  scutThreshold float DEFAULT NULL,
  degree int DEFAULT '0',
  gamma float DEFAULT '0',
  kernel int DEFAULT NULL,
);
create index IX_explabel on $(db_schema).weka_results(experiment,label);
create index IX_kernel on $(db_schema).weka_results(experiment,label,kernel,cost,degree,gamma);

CREATE TABLE  $(db_schema).stopword (
  stopword varchar(50) NOT NULL primary key
);



drop table hmls.feature_eval;
create table hmls.feature_eval (
  feature_eval_id int identity not null primary key,
  name varchar(50) not null,
  label varchar(50) not null,
  cv_fold_id int null,
  type varchar(50) not null,
);
create unique index nk_feature_eval on hmls.feature_eval(name, label, cv_fold_id, type);
create index ix_feature_eval on hmls.feature_eval(name, cv_fold_id, type);



drop table hmls.feature_rank;
create table hmls.feature_rank (
  feature_rank_id int identity not null primary key,
  feature_eval_id int not null foreign key references hmls.feature_eval(feature_eval_id) on delete cascade,
  feature_name varchar(50) not null,
  infogain float not null,
  rank int not null
);

create unique index nk_feature_rank on hmls.feature_rank(feature_eval_id, feature_name);
create index fk_feature_eval on hmls.feature_rank(feature_eval_id);
