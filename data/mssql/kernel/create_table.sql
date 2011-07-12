/**
 * tfidf tables
 */
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
);
create unique index NK_docfreq on $(db_schema).tfidf_docfreq (name, term);

create table $(db_schema).tfidf_termfreq (
  tfidf_termfreq_id int identity primary key,
  name varchar(50) not null,
  instance_id int not null,
  term varchar(50) not null,
  freq int not null,
);
create unique index NK_tfidif_termfreq on $(db_schema).tfidf_termfreq (name, instance_id, term);
create index IX_instance on $(db_schema).tfidf_termfreq(name, instance_id);

CREATE TABLE  $(db_schema).stopword (
  stopword varchar(50) NOT NULL primary key
);

/**
 * cv_fold tables
 */

create table $(db_schema).cv_fold (
  cv_fold_id int identity not null primary key,
  corpus_name varchar(50) not null,
  split_name varchar(50) not null default '',
  label varchar(50) not null default '',
  run int not null default 0,
  fold int not null default 0
);
create unique index nk_cv_fold on $(db_schema).cv_fold(corpus_name, split_name, label, run, fold);

create table $(db_schema).cv_fold_instance (
  cv_fold_instance_id int identity not null primary key,
  cv_fold_id int not null,
  instance_id int not null,
  train bit not null default 0
);
create unique index nk_cv_fold_instance on $(db_schema).cv_fold_instance(cv_fold_id, instance_id, train)

/**
 * classifier_eval tables
 */

CREATE TABLE $(db_schema).[classifier_eval](
	[classifier_eval_id] [int] IDENTITY(1,1) NOT NULL primary key,
	[name] [varchar](50) NOT NULL,
	[experiment] [varchar](50) NULL,
	[fold] [int] NULL,
	[run] [int] NULL,
	[algorithm] [varchar](50) NULL,
	[label] [varchar](50) NULL,
	[options] [varchar](1000) NULL,
	[model] [varbinary](max) NULL,
	[param1] [float] NULL,
	[param2] [varchar](50) NULL
)
;
go


CREATE TABLE $(db_schema).[classifier_eval_ir](
	[classifier_eval_ir_id] [int] IDENTITY(1,1) NOT NULL primary key,
	[classifier_eval_id] [int] NOT NULL foreign key references $(db_schema).classifier_eval ([classifier_eval_id]) on delete cascade,
	[ir_class_id] [int] NOT NULL,
	[tp] [int] NOT NULL default 0,
	[tn] [int] NOT NULL default 0,
	[fp] [int] NOT NULL default 0,
	[fn] [int] NOT NULL default 0,
	[ppv] [float] NOT NULL default 0,
	[npv] [float] NOT NULL default 0,
	[sens] [float] NOT NULL default 0,
	[spec] [float] NOT NULL default 0,
	[f1] [float] NOT NULL default 0
)
;
GO

CREATE TABLE $(db_schema).[classifier_eval_svm](
	[classifier_eval_id] [int] NOT NULL primary key foreign key references $(db_schema).classifier_eval ([classifier_eval_id]) on delete cascade,
	[cost] [float] NULL,
	[weight] [int] NULL,
	[degree] [int] NULL,
	[gamma] [float] NULL,
	[kernel] [int] NULL,
	[supportVectors] [int] NULL,
	vcdim float null,
)
;
GO

CREATE TABLE $(db_schema).classifier_eval_semil (
  classifier_eval_id int NOT NULL primary key foreign key references $(db_schema).classifier_eval ([classifier_eval_id]) on delete cascade,
  distance varchar(50) DEFAULT NULL,
  degree int NOT NULL DEFAULT 0,
  gamma float NOT NULL DEFAULT 0,
  soft_label bit NOT NULL DEFAULT 0,
  norm_laplace bit NOT NULL DEFAULT 0,
  mu float NOT NULL DEFAULT 0,
  lambda float NOT NULL DEFAULT 0,
  pct_labeled float NOT NULL DEFAULT 0
)
;
GO

CREATE TABLE $(db_schema).[classifier_instance_eval](
	[classifier_instance_eval_id] [int] IDENTITY(1,1) NOT NULL primary key ,
	[classifier_eval_id] [int] NOT NULL foreign key references $(db_schema).classifier_eval ([classifier_eval_id]) on delete cascade,
	[instance_id] [int] NOT NULL,
	[pred_class_id] [int] NOT NULL,
	[target_class_id] [int] NULL
) 
;
GO

CREATE TABLE $(db_schema).[classifier_instance_eval_prob](
	[classifier_eval_result_prob_id] [int] IDENTITY(1,1) NOT NULL primary key,
	[classifier_instance_eval_id] [int] NOT NULL foreign key references $(db_schema).classifier_instance_eval ([classifier_instance_eval_id]) on delete cascade,
	[class_id] [int] NOT NULL,
	[probability] [float] NOT NULL,
)
;
GO

/**
 * feature_eval tables
 */
create table $(db_schema).feature_eval (
  feature_eval_id int identity not null primary key,
  corpus_name varchar(50) not null,
  featureset_name varchar(50) not null default '',
  label varchar(50) not null,
  cv_fold_id int not null default 0,
  type varchar(50) not null,
  param1 varchar(50) not null default ''
);
create unique index nk_feature_eval on $(db_schema).feature_eval(corpus_name, featureset_name, label, cv_fold_id, type, param1);
create index ix_feature_eval on $(db_schema).feature_eval(name, cv_fold_id, type);

create table $(db_schema).feature_rank (
  feature_rank_id int identity not null primary key,
  feature_eval_id int not null foreign key references $(db_schema).feature_eval(feature_eval_id) on delete cascade,
  feature_name varchar(50) not null,
  evaluation float not null,
  rank int not null
);
create unique index nk_feature_rank on $(db_schema).feature_rank(feature_eval_id, feature_name);
create unique index nk2_feature_rank on $(db_schema).feature_rank(feature_eval_id, rank);
create index fk_feature_eval on $(db_schema).feature_rank(feature_eval_id);

/**
 * autohp tables
 */
CREATE TABLE  $(db_schema).hotspot (
  hotspot_id int NOT NULL identity primary key,
  instance_id int NOT NULL ,
  anno_base_id int NOT NULL foreign key references $(db_schema).anno_base(anno_base_id) on delete cascade,
  feature_rank_id int NOT NULL foreign key references $(db_schema).feature_rank(feature_rank_id) on delete cascade
) ;
create UNIQUE index NK_hotspot on $(db_schema).hotspot (instance_id,anno_base_id,feature_rank_id);
create index  ix_instance_id on $(db_schema).hotspot (instance_id);
create index  ix_anno_base_id on $(db_schema).hotspot (anno_base_id);
create index  ix_feature_rank_id on $(db_schema).hotspot (feature_rank_id);


CREATE TABLE  $(db_schema).hotspot_feature_eval (
  hotspot_feature_eval_id int NOT NULL identity primary key,
  name varchar(50) NOT NULL,
  label varchar(50) NOT NULL,
  instance_id int NOT NULL,
  feature_name varchar(50) NOT NULL,
  evaluation float NOT NULL
) ;
create UNIQUE index nk_hotspot_feature on $(db_schema).hotspot_feature_eval(name,label,instance_id,feature_name);
create index ix_rank on $(db_schema).hotspot_feature_eval(name,label,instance_id,evaluation);


CREATE TABLE  $(db_schema).hotspot_sentence (
  hotspot_sentence_id int NOT NULL identity primary key,
  name varchar(50) NOT NULL,
  label varchar(50) NOT NULL default '',
  instance_id int NOT NULL,
  anno_base_id int NOT NULL foreign key references $(db_schema).anno_sentence(anno_base_id),
  evaluation float DEFAULT NULL,
  section varchar(50) NOT NULL DEFAULT 'OTHER'
) ;
create UNIQUE index NK_hotspot_sentence on $(db_schema).hotspot_sentence (name,label,anno_base_id);
create index IX_instance on $(db_schema).hotspot_sentence (name,label,instance_id);

CREATE TABLE  $(db_schema).hotspot_zero_vector (
  hotspot_zero_vector_id int NOT NULL identity primary key,
  label varchar(50) NOT NULL default '',
  instance_id int NOT NULL,
  cutoff float NOT NULL,
  name varchar(50) NOT NULL
) ;
create UNIQUE index nk_zero_vector on $(db_schema).hotspot_zero_vector(label,instance_id,cutoff,name);
create index ix_instance_id on $(db_schema).hotspot_zero_vector(label,instance_id);


CREATE TABLE  $(db_schema).hotspot_zero_vector_tt (
  hotspot_zero_vector_tt int NOT NULL identity primary key,
  name varchar(50) NOT NULL ,
  experiment varchar(50) NOT NULL ,
  label varchar(50) NOT NULL ,
  run int NOT NULL DEFAULT 0 ,
  fold int NOT NULL DEFAULT 0 ,
  ir_class_id int NOT NULL ,
  cutoff float NOT NULL ,
  tp int NOT NULL,
  tn int NOT NULL,
  fp int NOT NULL,
  fn int NOT NULL
) ;
create  UNIQUE index NK_hotspot_zero_vector_tt on $(db_schema).hotspot_zero_vector_tt (name,experiment,label,run,fold,ir_class_id);
