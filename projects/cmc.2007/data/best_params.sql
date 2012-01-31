delete from cv_best_svm 
where experiment = 'kern-ctakes-ident'
and corpus_name = 'cmc.2007'
;

/* 
 * get the macro average f1 for each parameter combination. 
 * we leave the Q class out for the macro average when selecting the optimal params
 * because there are so few examples of this class that it skews results.
 */
drop table if exists tmp_param_f1;
create temporary table tmp_param_f1
as
select label, cost, avg(f1) f1, stddev(f1) stddevf1, avg(f1) - stddev(f1) f1s
from
(
  select label, cost, run, fold, avg(f1) f1
    from classifier_eval_ir t
    inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
    inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
    where name = 'cmc.2007'
    and experiment = 'kern-ctakes-ident'
    and ir_class_id = 1
    group by label, cost, run, fold
) s group by label, cost;

create unique index IX_paramf1 on tmp_param_f1(label, cost);

/* get the max f1 for each label */
insert into cv_best_svm (corpus_name, label, experiment, f1)
select 'cmc.2007', label, 'kern-ctakes-ident', f1
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
	where name = 'cmc.2007'
    and experiment = 'kern-ctakes-ident'
    )
where corpus_name = 'cmc.2007'
    and experiment = 'kern-ctakes-ident'
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
    and c.experiment = 'kern-ctakes-ident'
    and c.corpus_name = 'cmc.2007'
    and s.f1 >= c.f1
group by s.label;

update cv_best_svm b inner join cv_cost p on b.label = p.label set b.cost = p.cost
where b.experiment = 'kern-ctakes-ident' and corpus_name = 'cmc.2007'
;