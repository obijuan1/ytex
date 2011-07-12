-- evaluation without zero vectors
/*
create table tmp_classifier_ir
select * from v_classifier_eval_ir;

select avg(f1)
from
(
	select label, max(f1) f1
	from
	(
		select experiment, label, kernel, cost, gamma, weight, avg(f1) f1
		from tmp_classifier_ir t
		inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
		inner join classifier_eval_libsvm l on e.classifier_eval_id = l.classifier_eval_id
		group by experiment, label, kernel, cost, gamma, weight
	) s
	group by label
) s
;
*/

-- ******************************************************************
-- evaluation with zero vectors
-- put all classifications in a new table
DROP TABLE IF EXISTS `classifier_instance_eval_zv`;
CREATE TABLE `classifier_instance_eval_zv` (
  `classifier_instance_eval_id` int(11) NOT NULL DEFAULT '0',
  `classifier_eval_id` int(11) NOT NULL COMMENT 'fk classifier_eval',
  `instance_id` int(11) NOT NULL,
  `pred_class_id` int(11) NOT NULL,
  `target_class_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`classifier_eval_id`,`instance_id`),
  KEY `fk` (`classifier_eval_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

insert into classifier_instance_eval_zv
select * from classifier_instance_eval;

-- add classifications for zero vectors
drop table if exists  tmp_classifier_eval;
create table tmp_classifier_eval (
  classifier_eval_id int not null primary key,
  cv_fold_id int not null,
  label varchar(50) not null,
  cutoff int not null,
  index ix_label_cutoff (label, cutoff),
  index ix_cv_fold (cv_fold_id)
);

insert into tmp_classifier_eval
select e.classifier_eval_id, f.cv_fold_id, d.disease, substring(experiment, 7)
from classifier_eval e
/* convert the label into a disease name */
inner join i2b2_2008_disease d on d.disease_id = e.label
/* join with fold */
inner join cv_fold f
  on e.name = f.name
  and d.disease = f.label
  and e.run = f.run
  and e.fold = f.fold
;

/* add predictions for zero vectors */
insert into classifier_instance_eval_zv (classifier_eval_id, instance_id, pred_class_id, target_class_id)
select e.classifier_eval_id, i.instance_id, jauto.judgement_id, jgold.judgement_id
from tmp_classifier_eval e
/* join with test instances */
inner join cv_fold_instance i
  on i.cv_fold_id = e.cv_fold_id
  and i.train = 0
/* join with zero vectors */
inner join hotspot_zero_vector hzv
  on hzv.label = e.label
  and hzv.cutoff = e.cutoff
  and hzv.instance_id = i.instance_id
/* join with gold class */
inner join i2b2_2008_anno a
  on a.disease = e.label
  and a.docId = i.instance_id
  and a.source = 'intuitive'
/* convert into class id */
inner join i2b2_2008_judgement jgold on jgold.judgement = a.judgement
/* get default class for zero vector */
inner join hotspot_zv_default hzvd
  on hzvd.label = e.label
/* convert into class id */
inner join i2b2_2008_judgement jauto on jauto.judgement = hzvd.class_name
;

drop table if exists tmp_classifier_ir_zv;
/* compute f-score with zero-vectors */
create table tmp_classifier_ir_zv
select *,
  case when tp+fp > 0 then tp/(tp+fp) else 0 end prec,
  case when tp+fn > 0 then tp/(tp+fn) else 0 end sens,
  case when fp+tn > 0 then tn/(fp+tn) else 0 end spec,
  case when fn+tn > 0 then tn/(fn+tn) else 0 end npv,
  case when (tp+fp) > 0 and (tp+fn) > 0 then 2*(tp/(tp+fp))*(tp/(tp+fn))/(tp/(tp+fn) + tp/(tp+fp)) else 0 end f1
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
	from classifier_eval ce
	inner join classifier_instance_eval_zv ci
	on ce.classifier_eval_id = ci.classifier_eval_id
) cls
inner join classifier_instance_eval_zv ci on cls.classifier_eval_id = ci.classifier_eval_id
group by classifier_eval_id, ir_class_id
) s
;

/**
 * macro f1 of the best classifier from cross validation
 */
select avg(f1)
from
(
	/*
	 * best f1 score per label
	 */
	select label, max(f1) f1
	from
	(
		/*
		 * best f1 score by experiment (hotspot cutoff) and svm parameters
		 */
		select experiment, label, kernel, cost, gamma, weight, avg(f1) f1
		from tmp_classifier_ir_zv t
		inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
		inner join classifier_eval_libsvm l on e.classifier_eval_id = l.classifier_eval_id
		group by experiment, label, kernel, cost, gamma, weight
	) s
	group by label
) s
;

/*
 * sanity check - # predicted instances should = # instances in test fold
 * this query should return 0
 */
select z.cv_fold_id, z.classifier_eval_id, zic-fic
from
(
	/*
	 * get the number predicted instances per fold
	 */
	select e.cv_fold_id, e.classifier_eval_id, count(*) zic
	from classifier_instance_eval_zv zv
	inner join tmp_classifier_eval e on zv.classifier_eval_id = e.classifier_eval_id
	group by e.cv_fold_id, e.classifier_eval_id
) z
inner join
(
	/*
	 * get the number test instances per fold
	 */
	select f.cv_fold_id, count(*) fic
	from cv_fold f
	inner join cv_fold_instance i on f.cv_fold_id = i.cv_fold_id and i.train = 0
	group by f.cv_fold_id
) f
on z.cv_fold_id = f.cv_fold_id
/*
 * get the ones that differ
 */
where zic-fic <> 0
;
