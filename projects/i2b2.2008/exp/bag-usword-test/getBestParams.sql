
delete from i2b2_2008_cv_best where experiment = 'bag-usword';
/*
* get the best f1-score
*/
insert into i2b2_2008_cv_best (label, experiment, f1)
select label, experiment, truncate(max(f1),3) f1
from
(
	/*
	 * best f1 score by experiment (hotspot cutoff) and svm parameters
	 */
	select label, experiment, kernel, cost, gamma, weight, param1, param2, avg(f1) f1
	from classifier_eval_irzv t
	inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
	inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
	where name = 'i2b2.2008'
  and experiment in ('bag-usword')
	group by label, experiment, kernel, cost, gamma, weight, param1, param2
) s
group by label, experiment
order by cast(label as decimal(2,0));

/*
* get the best hotspot cutoff (param1)
*/
drop table if exists cv_param1;
create temporary table cv_param1 (label varchar(50), param1 double)
as
select c.label, max(s.param1) param1
from
(
	select label, experiment, kernel, cost, gamma, weight, param1, param2, avg(f1) f1
	from classifier_eval_irzv t
	inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
	inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
	where name = 'i2b2.2008'
  and experiment in ('bag-usword')
	group by label, experiment, kernel, cost, gamma, weight, param1, param2
) s inner join i2b2_2008_cv_best c on c.experiment = s.experiment and c.label = s.label and s.f1 >= c.f1
group by c.label
;

update i2b2_2008_cv_best b inner join cv_param1 p on b.label = p.label set b.param1 = p.param1
where b.experiment = 'bag-usword'
;

/*
* get the best cost
*/
drop table if exists cv_cost;
create temporary table cv_cost (label varchar(50), cost double)
as
select c.label, min(s.cost) cost
from
(
	select label, experiment, kernel, cost, gamma, weight, param1, param2, avg(f1) f1
	from classifier_eval_irzv t
	inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
	inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
	where name = 'i2b2.2008'
  and experiment in ('bag-usword')
	group by label, experiment, kernel, cost, gamma, weight, param1, param2
) s inner join i2b2_2008_cv_best c on c.experiment = s.experiment and c.label = s.label and s.f1 >= c.f1 and s.param1 = c.param1
group by c.label
;

update i2b2_2008_cv_best b inner join cv_cost p on b.label = p.label set b.cost = p.cost
where b.experiment = 'bag-usword'
;




/*
* get the best weight
*/
drop table if exists cv_weight;
create temporary table cv_weight (label varchar(50), weight varchar(50))
as
select c.label, min(coalesce(s.weight, '')) weight
from
(
	select label, experiment, kernel, cost, gamma, weight, param1, param2, avg(f1) f1
	from classifier_eval_irzv t
	inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
	inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
	where name = 'i2b2.2008'
  and experiment in ('bag-usword')
	group by label, experiment, kernel, cost, gamma, weight, param1, param2
) s inner join i2b2_2008_cv_best c on c.experiment = s.experiment and c.label = s.label and s.f1 >= c.f1 and s.param1 = c.param1 and s.cost = c.cost
group by c.label
;

update i2b2_2008_cv_best b inner join cv_weight p on b.label = p.label set b.weight = p.weight
where b.experiment = 'bag-usword'
;
