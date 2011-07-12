drop table if exists tmp_libsvm;

/* macro averaged f1 for each cross-validation run */
create temporary table tmp_libsvm
select cast(substring(experiment, 7) as decimal(3,2)) cutoff, label, kernel, cost, gamma, weight, f1
from
(
	/*
	 * best f1 score by experiment (hotspot cutoff) and svm parameters
	 */
	select experiment, label, kernel, cost, gamma, weight, avg(f1) f1
	from classifier_eval_irzv t
	inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
	inner join classifier_eval_libsvm l on e.classifier_eval_id = l.classifier_eval_id
    where experiment like 'bocuis%'
	group by experiment, label, kernel, cost, gamma, weight
) s
;



delete from i2b2_best_libsvm where experiment = 'bocuis';

/* get best f1 for each label */
insert into i2b2_best_libsvm (experiment, label, best_f1)
select 'bocuis', label, max(f1)
from tmp_libsvm
group by label
;

/* get best cutoff for each label */
update i2b2_best_libsvm b
inner join
(
select t.label, max(t.cutoff) cutoff
from tmp_libsvm t
inner join i2b2_best_libsvm b 
	on t.label = b.label 
	and t.f1 > (b.best_f1 - 0.02)
where b.experiment = 'bocuis'
group by t.label
) c on b.label = c.label and b.experiment = 'bocuis'
set b.cutoff = c.cutoff
;

/* get best kernel for each label */
update i2b2_best_libsvm b
inner join
(
select t.label, min(t.kernel) kernel
from tmp_libsvm t
inner join i2b2_best_libsvm b
  on t.label = b.label
  and t.cutoff = b.cutoff
  and t.f1 > (b.best_f1 - 0.02)
where b.experiment = 'bocuis'
group by t.label
) c on b.label = c.label and b.experiment = 'bocuis'
set b.kernel = c.kernel
;
-- best kernel linear across the board, don't worry about gamma

/* get best cost for each label */
update i2b2_best_libsvm b
inner join
(
select t.label, min(t.cost) cost
from tmp_libsvm t
inner join i2b2_best_libsvm b
  on t.label = b.label
  and t.cutoff = b.cutoff
  and t.kernel = b.kernel
  and t.f1 > (b.best_f1 - 0.02)
where b.experiment = 'bocuis'
group by t.label
) c on b.label = c.label and b.experiment = 'bocuis'
set b.cost = c.cost
;
