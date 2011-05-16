
CREATE TABLE  hmls.hotspot (
  hotspot_id int NOT NULL identity primary key,
  instance_id int NOT NULL ,
  anno_base_id int NOT NULL ,
  feature_rank_id int NOT NULL
) ;
create UNIQUE index NK_hotspot on hmls.hotspot (instance_id,anno_base_id,feature_rank_id);
create index  ix_instance_id on hmls.hotspot (instance_id);
create index  ix_anno_base_id on hmls.hotspot (anno_base_id);
create index  ix_feature_rank_id on hmls.hotspot (feature_rank_id);


CREATE TABLE  hmls.hotspot_feature_eval (
  hotspot_feature_eval_id int NOT NULL identity primary key,
  name varchar(50) NOT NULL,
  label varchar(50) NOT NULL,
  instance_id int NOT NULL,
  feature_name varchar(50) NOT NULL,
  evaluation float NOT NULL
) ;
create UNIQUE index nk_hotspot_feature on hmls.hotspot_feature_eval(name,label,instance_id,feature_name);
create index ix_rank on hmls.hotspot_feature_eval(name,label,instance_id,evaluation);


CREATE TABLE  hmls.hotspot_sentence (
  hotspot_sentence_id int NOT NULL identity primary key,
  name varchar(50) NOT NULL,
  label varchar(50) NOT NULL default '',
  instance_id int NOT NULL,
  anno_base_id int NOT NULL foreign key references hmls.anno_sentence(anno_base_id),
  evaluation float DEFAULT NULL,
  section varchar(50) NOT NULL DEFAULT 'OTHER'
) ;
create UNIQUE index NK_hotspot_sentence on hmls.hotspot_sentence (name,label,anno_base_id);
create index IX_instance on hmls.hotspot_sentence (name,label,instance_id);

CREATE TABLE  hmls.hotspot_zero_vector (
  hotspot_zero_vector_id int NOT NULL identity primary key,
  label varchar(50) NOT NULL default '',
  instance_id int NOT NULL,
  cutoff float NOT NULL,
  name varchar(50) NOT NULL
) ;
create UNIQUE index nk_zero_vector on hmls.hotspot_zero_vector(label,instance_id,cutoff,name);
create index ix_instance_id on hmls.hotspot_zero_vector(label,instance_id);


CREATE TABLE  hmls.hotspot_zero_vector_tt (
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
create  UNIQUE index NK_hotspot_zero_vector_tt on hmls.hotspot_zero_vector_tt (name,experiment,label,run,fold,ir_class_id);
