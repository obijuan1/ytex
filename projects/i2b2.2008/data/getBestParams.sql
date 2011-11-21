delete from cv_best_svm 
where experiment = '@kernel.experiment@'
and corpus_name = '@kernel.name@'
;

/* 
 * get the macro average f1 for each parameter combination. 
 * we leave the Q class out for the macro average when selecting the optimal params
 * because there are so few examples of this class that it skews results.
 */
drop table if exists tmp_param_f1;
create temporary table tmp_param_f1
as
select label, cost, param1, param2, avg(f1) f1, stddev(f1) stddevf1, avg(f1) - stddev(f1) f1s
from
(
  select label, cost, param1, param2, run, fold, avg(f1) f1
    from classifier_eval_ir t
    inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
    inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
    where name = '@kernel.name@'
    and experiment = '@kernel.experiment@'
    and t.ir_type = 'zv'
    and t.ir_class <> 'Q'
    group by label, cost, param1, run, fold
) s group by label, cost, param1;

create unique index IX_paramf1 on tmp_param_f1(label, cost, param1, param2);

/* get the max f1 for each label */
insert into cv_best_svm (corpus_name, label, experiment, f1)
select '@kernel.name@', label, '@kernel.experiment@', f1
from
(
	/*
	 * best f1 score by experiment (hotspot cutoff) and svm parameters
	 */
	select label, truncate(max(f1),3) f1
	from tmp_param_f1
	group by label
) s
;

/*
 * set the kernel type - assume only 1 kernel type per experiment
 */
update cv_best_svm 
set kernel = 
	(
	select distinct kernel 
	from classifier_eval e
    inner join classifier_eval_svm s on e.classifier_eval_id = s.classifier_eval_id
	where name = '@kernel.name@'
    and experiment = '@kernel.experiment@'
    )
where corpus_name = '@kernel.name@'
    and experiment = '@kernel.experiment@'
;

/*
* get the best hotspot cutoff (param1)
*/
drop table if exists cv_param1;
create temporary table cv_param1 (label varchar(50), param1 double)
as
select c.label, max(s.param1) param1
from tmp_param_f1 s 
inner join cv_best_svm c 
    on c.label = s.label
    and c.corpus_name = '@kernel.name@'
    and s.f1 >= c.f1
    and c.experiment = '@kernel.experiment@'
group by c.label
;

update cv_best_svm b inner join cv_param1 p on b.label = p.label set b.param1 = p.param1
where b.experiment = '@kernel.experiment@' and corpus_name = '@kernel.name@'
;

/*
* get the best lcs cutoff
* only applicable for superlin
* for others, nothing will be done here
*/
drop table if exists cv_param2;
create temporary table cv_param2 (label varchar(50), param2 char(5))
as
select c.label, max(cast(s.param2 as decimal(2,0))) param2
from tmp_param_f1 s 
inner join cv_best_svm c 
    on c.label = s.label 
    and c.corpus_name = '@kernel.name@'
    and c.experiment = '@kernel.experiment@'
    and s.f1 >= c.f1
where s.param2 <> ''
group by c.label
;

update cv_best_svm b inner join cv_param2 p on b.label = p.label set b.param2 = p.param2
where b.experiment = '@kernel.experiment@' and corpus_name = '@kernel.name@'
;

/*
* get the lowest cost for the best f1 
*/
drop table if exists cv_cost;
create temporary table cv_cost (label varchar(50), cost double)
select s.label, min(s.cost) cost
from tmp_param_f1 s 
inner join cv_best_svm c 
    on c.label = s.label 
    and s.param1 = c.param1
    and s.param2 = coalesce(c.param2, '')
    and c.experiment = '@kernel.experiment@'
    and c.corpus_name = '@kernel.name@'
    and s.f1 >= c.f1
group by s.label;

update cv_best_svm b inner join cv_cost p on b.label = p.label set b.cost = p.cost
where b.experiment = '@kernel.experiment@' and corpus_name = '@kernel.name@'
;
