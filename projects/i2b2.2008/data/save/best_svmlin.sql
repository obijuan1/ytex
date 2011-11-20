/*
 * get the best svmlin parameters
 * start with multiclass
 * then move on to binary classes
 */
drop table if exists best_f1;
create temporary table best_f1 as
select label, truncate(max(f1),3) f1
from
(
	/*
	 * best f1 score by experiment (hotspot cutoff) and svm parameters
	 */
	select label, kernel, cost, gamma, weight, param1, param2, avg(f1) f1
	from classifier_eval_irzv t
	inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
	inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
	where experiment = 'bag-word-svmlinmc'
	and name = 'i2b2.2008'
	and run > 0
	and fold > 0
	group by label, kernel, cost, gamma, weight, param1, param2
) s
group by label
;

drop table if exists best_cutoff;
create temporary table best_cutoff as
select b.label, b.f1, min(param1) cutoff
from
(
	/*
	 * best f1 score by experiment (hotspot cutoff) and svm parameters
	 */
	select label, kernel, cost, gamma, weight, param1, param2, avg(f1) f1
	from classifier_eval_irzv t
	inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
	inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
	where experiment = 'bag-word-svmlinmc'
	and name = 'i2b2.2008'
	and run > 0
	and fold > 0
	group by label, kernel, cost, gamma, weight, param1, param2
) s inner join best_f1 b on b.label = s.label and s.f1 >= b.f1
group by b.label, b.f1
;
select * from best_cutoff;


drop table if exists best_cost;
create temporary table best_cost as
select b.label, b.f1, b.cutoff, min(cost) cost
from
(
	/*
	 * best f1 score by experiment (hotspot cutoff) and svm parameters
	 */
	select label, kernel, cost, gamma, weight, param1, param2, avg(f1) f1
	from classifier_eval_irzv t
	inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
	inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
	where experiment = 'bag-word-svmlinmc'
	and name = 'i2b2.2008'
	and run > 0
	and fold > 0
	group by label, kernel, cost, gamma, weight, param1, param2
) s inner join best_cutoff b on b.label = s.label and s.f1 >= b.f1 and b.cutoff = s.param1
group by b.label, b.f1, b.cutoff
;

drop table if exists best_svmlin;
create table best_svmlin as
select b.label, b.f1, b.cutoff, b.cost, max(s.gamma) lambda
from
(
	/*
	 * best f1 score by experiment (hotspot cutoff) and svm parameters
	 */
	select label, kernel, cost, gamma, weight, param1, param2, avg(f1) f1
	from classifier_eval_irzv t
	inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
	inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
	where experiment = 'bag-word-svmlinmc'
	and name = 'i2b2.2008'
	and run > 0
	and fold > 0
	group by label, kernel, cost, gamma, weight, param1, param2
) s inner join best_cost b on b.label = s.label and s.f1 >= b.f1 and b.cutoff = s.param1 and b.cost = s.cost
group by b.label, b.f1, b.cutoff, b.cost
;

/*
 * repeat for single-class experiments
 */
drop table if exists best_f1;
create temporary table best_f1 as
select label, truncate(max(f1),3) f1
from
(
	/*
	 * best f1 score by experiment (hotspot cutoff) and svm parameters
	 */
	select label, kernel, cost, gamma, weight, param1, param2, avg(f1) f1
	from classifier_eval_irzv t
	inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
	inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
	where experiment = 'bag-word-svmlin'
	and name = 'i2b2.2008'
	and run > 0
	and fold > 0
	and label not in (select label from best_svmlin)
	group by label, kernel, cost, gamma, weight, param1, param2
) s
group by label
;

create temporary table best_f1 as
select label, truncate(max(f1),3) f1
from
(
	/*
	 * best f1 score by experiment (hotspot cutoff) and svm parameters
	 */
	select label, kernel, cost, gamma, weight, param1, param2, avg(f1) f1
	from classifier_eval_irzv t
	inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
	inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
	where experiment = 'bag-word-svmlin'
	and name = 'i2b2.2008'
	and run > 0
	and fold > 0
  and label not in (select label from best_svmlin)
	group by label, kernel, cost, gamma, weight, param1, param2
) s
group by label
;

drop table if exists best_cutoff;
create temporary table best_cutoff as
select b.label, b.f1, min(param1) cutoff
from
(
	/*
	 * best f1 score by experiment (hotspot cutoff) and svm parameters
	 */
	select label, kernel, cost, gamma, weight, param1, param2, avg(f1) f1
	from classifier_eval_irzv t
	inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
	inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
	where experiment = 'bag-word-svmlin'
	and name = 'i2b2.2008'
	and run > 0
	and fold > 0
	group by label, kernel, cost, gamma, weight, param1, param2
) s inner join best_f1 b on b.label = s.label and s.f1 >= b.f1
group by b.label, b.f1
;
select * from best_cutoff;


drop table if exists best_cost;
create temporary table best_cost as
select b.label, b.f1, b.cutoff, min(cost) cost
from
(
	/*
	 * best f1 score by experiment (hotspot cutoff) and svm parameters
	 */
	select label, kernel, cost, gamma, weight, param1, param2, avg(f1) f1
	from classifier_eval_irzv t
	inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
	inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
	where experiment = 'bag-word-svmlin'
	and name = 'i2b2.2008'
	and run > 0
	and fold > 0
	group by label, kernel, cost, gamma, weight, param1, param2
) s inner join best_cutoff b on b.label = s.label and s.f1 >= b.f1 and b.cutoff = s.param1
group by b.label, b.f1, b.cutoff
;
select * from best_cost;

insert into best_svmlin
select b.label, b.f1, b.cutoff, b.cost, max(s.gamma) lambda
from
(
	/*
	 * best f1 score by experiment (hotspot cutoff) and svm parameters
	 */
	select label, kernel, cost, gamma, weight, param1, param2, avg(f1) f1
	from classifier_eval_irzv t
	inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
	inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
	where experiment = 'bag-word-svmlin'
	and name = 'i2b2.2008'
	and run > 0
	and fold > 0
	group by label, kernel, cost, gamma, weight, param1, param2
) s inner join best_cost b on b.label = s.label and s.f1 >= b.f1 and b.cutoff = s.param1 and b.cost = s.cost
group by b.label, b.f1, b.cutoff, b.cost
;