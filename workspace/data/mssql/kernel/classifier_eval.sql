IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[HMLS].[classifier_eval_ir]') AND type in (N'U'))
DROP TABLE [HMLS].[classifier_eval_ir]
GO

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[HMLS].[classifier_eval_libsvm]') AND type in (N'U'))
DROP TABLE [HMLS].[classifier_eval_libsvm]
GO

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[HMLS].[classifier_instance_eval_prob]') AND type in (N'U'))
DROP TABLE [HMLS].[classifier_instance_eval_prob]
GO

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[HMLS].[classifier_instance_eval]') AND type in (N'U'))
DROP TABLE [HMLS].[classifier_instance_eval]
GO

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[HMLS].[classifier_eval]') AND type in (N'U'))
DROP TABLE [HMLS].[classifier_eval]
GO


CREATE TABLE [HMLS].[classifier_eval](
	[classifier_eval_id] [int] IDENTITY(1,1) NOT NULL,
	[name] [varchar](50) NOT NULL,
	[experiment] [varchar](50) NULL,
	[fold] [int] NULL,
	[run] [int] NULL,
	[algorithm] [varchar](50) NULL,
	[label] [varchar](50) NULL,
	[options] [varchar](1000) NULL,
	[model] [varbinary](max) NULL,
	[param1] [float] NULL,
	[param2] [varchar](50) NULL,
PRIMARY KEY CLUSTERED 
(
	[classifier_eval_id] ASC
)
)
;
go


CREATE TABLE [HMLS].[classifier_eval_ir](
	[classifier_eval_ir_id] [int] IDENTITY(1,1) NOT NULL,
	[classifier_eval_id] [int] NOT NULL foreign key references hmls.classifier_eval ([classifier_eval_id]) on delete cascade,
	[ir_class_id] [int] NOT NULL,
	[tp] [int] NOT NULL,
	[tn] [int] NOT NULL,
	[fp] [int] NOT NULL,
	[fn] [int] NOT NULL,
	[ppv] [float] NOT NULL,
	[npv] [float] NOT NULL,
	[sens] [float] NOT NULL,
	[spec] [float] NOT NULL,
	[f1] [float] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[classifier_eval_ir_id] ASC
)
)
;
GO

ALTER TABLE [HMLS].[classifier_eval_ir] ADD  DEFAULT ((0)) FOR [tp]
GO

ALTER TABLE [HMLS].[classifier_eval_ir] ADD  DEFAULT ((0)) FOR [tn]
GO

ALTER TABLE [HMLS].[classifier_eval_ir] ADD  DEFAULT ((0)) FOR [fp]
GO

ALTER TABLE [HMLS].[classifier_eval_ir] ADD  DEFAULT ((0)) FOR [fn]
GO

ALTER TABLE [HMLS].[classifier_eval_ir] ADD  DEFAULT ((0)) FOR [ppv]
GO

ALTER TABLE [HMLS].[classifier_eval_ir] ADD  DEFAULT ((0)) FOR [npv]
GO

ALTER TABLE [HMLS].[classifier_eval_ir] ADD  DEFAULT ((0)) FOR [sens]
GO

ALTER TABLE [HMLS].[classifier_eval_ir] ADD  DEFAULT ((0)) FOR [spec]
GO

ALTER TABLE [HMLS].[classifier_eval_ir] ADD  DEFAULT ((0)) FOR [f1]
GO

CREATE TABLE [HMLS].[classifier_eval_libsvm](
	[classifier_eval_id] [int] NOT NULL primary key foreign key references hmls.classifier_eval ([classifier_eval_id]) on delete cascade,
	[cost] [float] NULL,
	[weight] [int] NULL,
	[degree] [int] NULL,
	[gamma] [float] NULL,
	[kernel] [int] NULL,
	[supportVectors] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[classifier_eval_id] ASC
)
)

GO

CREATE TABLE [HMLS].[classifier_instance_eval](
	[classifier_instance_eval_id] [int] IDENTITY(1,1) NOT NULL primary key ,
	[classifier_eval_id] [int] NOT NULL foreign key references hmls.classifier_eval ([classifier_eval_id]) on delete cascade,
	[instance_id] [int] NOT NULL,
	[pred_class_id] [int] NOT NULL,
	[target_class_id] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[classifier_instance_eval_id] ASC
)
) 

GO

CREATE TABLE [HMLS].[classifier_instance_eval_prob](
	[classifier_eval_result_prob_id] [int] IDENTITY(1,1) NOT NULL,
	[classifier_instance_eval_id] [int] NOT NULL foreign key references hmls.classifier_instance_eval ([classifier_instance_eval_id]) on delete cascade,
	[class_id] [int] NOT NULL,
	[probability] [float] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[classifier_eval_result_prob_id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

GO





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

create table $(db_schema).classifier_eval (
	classifier_eval_id int identity not null primary key,
	name varchar(50) not null,
	experiment varchar(50) null,
	fold varchar(50) null,
	algorithm varchar(50) null,
	label varchar(50) null,
	options varchar(1000) null,
	model varBinary(MAX) null
);

create table $(db_schema).classifier_eval_libsvm (
	classifier_eval_id int primary key,
	cost float DEFAULT 0,
  	weight int DEFAULT 0,
	degree int DEFAULT 0,
	gamma float DEFAULT 0,
	kernel int,
	supportVectors int
);

alter table $(db_schema).classifier_eval_libsvm
add foreign key (classifier_eval_id) references $(db_schema).classifier_eval(classifier_eval_id) on delete cascade;

create table $(db_schema).classifier_instance_eval (
	classifier_instance_eval_id int not null identity primary key,
	classifier_eval_id int not null,
	instance_id int not null,
	pred_class_id int not null,
	target_class_id int null
);

alter table $(db_schema).classifier_instance_eval
add foreign key (classifier_eval_id) references $(db_schema).classifier_eval(classifier_eval_id) on delete cascade;

create unique index NK_classifier_instance_eval on $(db_schema).classifier_instance_eval(classifier_eval_id, instance_id);

create table $(db_schema).classifier_instance_eval_prob (
	classifier_eval_result_prob_id int not null identity primary key,
	classifier_instance_eval_id int not null,
	class_id int not null,
	probability float not null
);

alter table $(db_schema).classifier_instance_eval_prob
add foreign key (classifier_instance_eval_id)
references $(db_schema).classifier_instance_eval(classifier_instance_eval_id) 
on delete cascade;

create unique index nk_result_prob 
on $(db_schema).classifier_instance_eval_prob(classifier_instance_eval_id, class_id);


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
group by cls.classifier_eval_id, ir_class_id
) s
) s
;