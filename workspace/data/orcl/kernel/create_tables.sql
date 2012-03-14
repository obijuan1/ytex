create sequence feature_eval_id_sequence;
create sequence feature_rank_id_sequence;
create sequence feature_parchd_id_sequence;

create table feature_eval (
  feature_eval_id int identity not null primary key,
  corpus_name varchar2(50) not null ,
  featureset_name varchar2(50) not null default ' ' ,
  label varchar2(50) not null default ' '  ,
  cv_fold_id int not null default 0 ,
  param1 float not null default 0 ,
  param2 varchar2(50) not null default ' ' ,
  type varchar2(50) not null,
);
create unique index nk_feature_eval on feature_eval(corpus_name, featureset_name, label, cv_fold_id, param1, param2, type);
create index ix_feature_eval on feature_eval (corpus_name, cv_fold_id, type);
-- insert triggers to generate primary keys from sequence
create trigger trg_feature_eval before insert on feature_eval
for each row
when (new.feature_eval_id is null)
begin
 select feature_eval_id_sequence.nextval into :new.feature_eval_id from dual;
end;
/

create table  feature_rank (
  feature_rank_id int identity not null primary key,
  feature_eval_id int not null ,
  feature_name varchar2(50) not null ,
  evaluation float not null default 0 ,
  rank int not null default 0,
  foreign key (feature_eval_id) references feature_eval (feature_eval_id) ON DELETE CASCADE
) ;
create unique index nk_feature_name on  feature_rank(feature_eval_id, feature_name);
create index ix_feature_rank  on  feature_rank(feature_eval_id, rank);
create index ix_feature_evaluation  on  feature_rank(feature_eval_id, evaluation);

create trigger trg_feature_rank before insert on feature_rank
for each row
when (new.note_id is null)
begin
 select demo_note_id_sequence.nextval into :new.note_id from dual;
end;
/

CREATE TABLE feature_parchd (
  feature_parchd_id int identity NOT NULL primary key,
  par_feature_rank_id int NOT NULL ,
  chd_feature_rank_id int NOT NULL
);
create UNIQUE index NK_feature_parent on feature_parchd(par_feature_rank_id,chd_feature_rank_id);

create trigger trg_feature_parchd before insert on feature_parchd
for each row
when (new.note_id is null)
begin
 select demo_note_id_sequence.nextval into :new.note_id from dual;
end;
/
