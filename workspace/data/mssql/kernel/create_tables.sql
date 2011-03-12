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

create table $(db_schema).tfidf_doclength (
  tfidf_doclength_id int identity not null primary key,
  name varchar(255) not null default '',
  instance_id int not null,
  length int not null default 0
);
create unique index NK_doclength on $(db_schema).tfidf_doclength (name, instance_id);

create table $(db_schema).tfidf_docfreq (
  tfidf_docfreq_id int identity not null primary key,
  name varchar(255) not null default '',
  term varchar(50) not null,
  numdocs int not null default 0
)
create unique index NK_docfreq on $(db_schema).tfidf_docfreq (name, term);


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

create table $(db_schema).classifier_eval (
	classifier_eval_id int AUTO_INCREMENT not null primary key,
	name varchar(50) not null,
	fold varchar(50) null,
	algorithm varchar(50) null,
	label varchar(50) null,
	options varchar(200) null,
	model longblob null
) comment 'evaluation of a classifier on a dataset';

create table $(db_schema).classifier_eval_libsvm (
	classifier_eval_id int not null comment 'fk classifier_eval' primary key,
	cost double DEFAULT '0',
  	weight int DEFAULT '0',
	degree int DEFAULT '0',
	gamma double DEFAULT '0',
	kernel int DEFAULT NULL,
	supportVectors int default null
) comment 'evaluation of a libsvm classifier on a dataset';

create table $(db_schema).classifier_instance_eval (
	classifier_instance_eval_id int not null auto_increment primary key,
	classifier_eval_id int not null comment 'fk classifier_eval',
	instance_id int not null,
	class_id int not null,
	unique key nk_result (classifier_eval_id, instance_id)
) comment 'instance classification result';

create table $(db_schema).classifier_instance_eval_prob (
	classifier_eval_result_prob_id int not null auto_increment primary key,
	classifier_instance_eval_id int comment 'fk classifier_instance_eval',
	class_id int not null,
	probability double not null,
	unique key nk_result_prob (classifier_instance_eval_id, class_id)
) comment 'probability of belonging to respective class';

create view $(db_schema).v_classifier_eval_ir
as
select *,
  case when sens+prec > 0 then 2*sens*prec/(sens+prec) else 0 end f1
from
(
select *,
  case when tp+fp > 0 then tp/(tp+fp) else 0 end prec,
  case when tp+fn > 0 then tp/(tp+fn) else 0 end sens,
  case when fp+tn > 0 then tn/(fp+tn) else 0 end spec
from
(
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
from
(
select distinct ce.classifier_eval_id, target_class_id ir_class_id
from $(db_schema).classifier_eval ce
inner join $(db_schema).classifier_instance_eval ci
on ce.classifier_eval_id = ci.classifier_eval_id
) cls
inner join $(db_schema).classifier_instance_eval ci on cls.classifier_eval_id = ci.classifier_eval_id
group by classifier_eval_id, ir_class_id
) s
) s
;

create table hmls.classifier_eval (
	classifier_eval_id int identity not null primary key,
	name varchar(50) not null,
	experiment varchar(50) null,
	fold varchar(50) null,
	algorithm varchar(50) null,
	label varchar(50) null,
	options varchar(1000) null,
	model varBinary(MAX) null
);

create table hmls.classifier_eval_libsvm (
	classifier_eval_id int primary key,
	cost float DEFAULT 0,
  	weight int DEFAULT 0,
	degree int DEFAULT 0,
	gamma float DEFAULT 0,
	kernel int,
	supportVectors int
);

alter table hmls.classifier_eval_libsvm
add foreign key (classifier_eval_id) references hmls.classifier_eval(classifier_eval_id) on delete cascade;

create table hmls.classifier_instance_eval (
	classifier_instance_eval_id int not null identity primary key,
	classifier_eval_id int not null,
	instance_id int not null,
	pred_class_id int not null,
	target_class_id int null
);

alter table hmls.classifier_instance_eval
add foreign key (classifier_eval_id) references hmls.classifier_eval(classifier_eval_id) on delete cascade;

create unique index NK_classifier_instance_eval on hmls.classifier_instance_eval(classifier_eval_id, instance_id);

create table hmls.classifier_instance_eval_prob (
	classifier_eval_result_prob_id int not null identity primary key,
	classifier_instance_eval_id int not null,
	class_id int not null,
	probability float not null
);

alter table hmls.classifier_instance_eval_prob
add foreign key (classifier_instance_eval_id)
references hmls.classifier_instance_eval(classifier_instance_eval_id) 
on delete cascade;

create unique index nk_result_prob 
on hmls.classifier_instance_eval_prob(classifier_instance_eval_id, class_id);


create view hmls.v_classifier_eval_ir
as
select *,
  case when sens+prec > 0 then 2*sens*prec/(sens+prec) else 0 end f1
from
(
select *,
  case when tp+fp > 0 then tp/(tp+fp) else 0 end prec,
  case when tp+fn > 0 then tp/(tp+fn) else 0 end sens,
  case when fp+tn > 0 then tn/(fp+tn) else 0 end spec
from
(
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
from
(
select distinct ce.classifier_eval_id, target_class_id ir_class_id
from $(db_schema).classifier_eval ce
inner join $(db_schema).classifier_instance_eval ci
on ce.classifier_eval_id = ci.classifier_eval_id
) cls
inner join $(db_schema).classifier_instance_eval ci on cls.classifier_eval_id = ci.classifier_eval_id
group by cls.classifier_eval_id, ir_class_id
) s
) s
;