drop table if exists cv_best ;
create temporary table cv_best (
    label varchar(50) primary key,
    f1 double,
    kernel int,
    cost double,
    weight varchar(50),
    param1 double,
    param2 varchar(50)
) engine = myisam;

/*
* get the best f1-score within 2 decimal places
*/
insert into cv_best (label, f1)
select label, truncate(max(f1),2) f1
from
(
	/*
	 * best f1 score by experiment (hotspot cutoff) and svm parameters
	 */
	select label, kernel, cost, gamma, weight, param1, param2, avg(f1) f1
	from classifier_eval_irzv t
	inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
	inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
	where name = 'i2b2.2008'
  and experiment = 'bag-word'
	group by label, kernel, cost, gamma, weight, param1, param2
) s
group by label
order by cast(label as decimal(2,0));

/*
* get the best hotspot cutoff (param1)
*/
drop table if exists cv_param1;
create temporary table cv_param1 (label varchar(50), param1 double);
insert into cv_param1
select c.label, max(s.param1)
from
(
	select label, experiment, kernel, cost, gamma, weight, param1, param2, avg(f1) f1
	from classifier_eval_irzv t
	inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
	inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
	where name = 'i2b2.2008'
  and experiment = 'bag-word'
	group by label, experiment, kernel, cost, gamma, weight, param1, param2
) s inner join cv_best c on c.label = s.label and s.f1 >= c.f1
group by c.label
;
update cv_best b inner join cv_param1 p on b.label = p.label set b.param1 = p.param1;


/*
* get the best cost
*/

drop table if exists cv_cost;
create temporary table cv_cost (label varchar(50), cost double);
insert into cv_cost
select c.label, min(s.cost)
from
(
	select label, experiment, kernel, cost, gamma, weight, param1, param2, avg(f1) f1
	from classifier_eval_irzv t
	inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
	inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
	where name = 'i2b2.2008'
  and experiment = 'bag-word'
	group by label, experiment, kernel, cost, gamma, weight, param1, param2
) s 
inner join cv_best c 
    on c.label = s.label 
    and c.param1 = s.param1
    and s.f1 >= c.f1 
group by c.label
;

update cv_best b inner join cv_cost p on b.label = p.label set b.cost = p.cost;
select * from cv_best;

select cast(concat('label.', label, '.cv.costs=', cost)as char(200))
from cv_best
;
select cast(concat('label.', label, '.kernel.param1=', param1)as char(200))
from cv_best
;


