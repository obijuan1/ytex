create sequence feature_eval_sequence;
create sequence feature_rank_sequence;
create sequence feature_parchd_sequence;

create table feature_eval (
  feature_eval_id int not null primary key,
  corpus_name varchar2(50) not null ,
  featureset_name varchar2(50) default ' ' not null ,
  label varchar2(50) default ' '  not null ,
  cv_fold_id int default 0  not null ,
  param1 DOUBLE PRECISION default 0 not null ,
  param2 varchar2(50) default ' ' not null ,
  type varchar2(50) not null
);
create unique index nk_feature_eval on feature_eval(corpus_name, featureset_name, label, cv_fold_id, param1, param2, type);
create index ix_feature_eval on feature_eval (corpus_name, cv_fold_id, type);

create table  feature_rank (
  feature_rank_id int not null primary key,
  feature_eval_id int not null ,
  feature_name varchar2(50) not null ,
  evaluation DOUBLE PRECISION default 0 not null ,
  rank int default 0 not null ,
  foreign key (feature_eval_id) references feature_eval (feature_eval_id) ON DELETE CASCADE
) ;
create unique index nk_feature_name on  feature_rank(feature_eval_id, feature_name);
create index ix_feature_rank  on  feature_rank(feature_eval_id, rank);
create index ix_feature_evaluation  on  feature_rank(feature_eval_id, evaluation);


CREATE TABLE feature_parchd (
  feature_parchd_id int NOT NULL primary key,
  par_feature_rank_id int NOT NULL ,
  chd_feature_rank_id int NOT NULL
);
create UNIQUE index NK_feature_parent on feature_parchd(par_feature_rank_id,chd_feature_rank_id);
