/*
 * get the best parameters
 * using just cost and weight
 */

drop table if exists best_f1;
create temporary table best_f1 (
    label varchar(10),
    best_f1 double
);

-- select * from weka_results where experiment = '@EXPERIMENT@' and kernel = 0;

insert into best_f1 (label, best_f1)
select label, truncate(max(f1a), 3)
from
(
    select label, cost, coalesce(weight, '') weight, avg(f1) f1a
    from classifier_eval e
    inner join classifier_eval_ir i on e.classifier_eval_id = i.classifier_eval_id and i.ir_class_id = 1
    inner join classifier_eval_svm s on e.classifier_eval_id = s.classifier_eval_id
    where experiment in ('kern-ctakes-flatne')
    group by label, cost, coalesce(weight, '') 
) s
group by label
;

drop table if exists best_cost;
create temporary table best_cost (
    label varchar(10),
    best_f1 double,
    best_cost double
);

insert into best_cost (label, best_f1, best_cost)
select s.label, best_f1, min(cost)
from
(
    select label, cost, coalesce(weight, '') weight, avg(f1) f1a
    from classifier_eval e
    inner join classifier_eval_ir i on e.classifier_eval_id = i.classifier_eval_id and i.ir_class_id = 1
    inner join classifier_eval_svm s on e.classifier_eval_id = s.classifier_eval_id
    where experiment in ('kern-ctakes-flatne')
    group by label, cost, coalesce(weight, '') 
) s inner join best_f1 f1 on f1.label = s.label and f1.best_f1 <= s.f1a
group by s.label;

drop table if exists best_weight;
create temporary table best_weight (
    label varchar(10),
    best_f1 double,
    best_cost double,
    best_weight varchar(100)
);

insert into best_weight (label, best_f1, best_cost, best_weight)
select s.label, best_f1, best_cost, min(weight)
from
(
    select label, cost, coalesce(weight, '') weight, avg(f1) f1a
    from classifier_eval e
    inner join classifier_eval_ir i on e.classifier_eval_id = i.classifier_eval_id and i.ir_class_id = 1
    inner join classifier_eval_svm s on e.classifier_eval_id = s.classifier_eval_id
    where experiment in ('kern-ctakes-flatne')
    group by label, cost, coalesce(weight, '')
) s inner join best_cost f1 on f1.label = s.label and f1.best_f1 <= s.f1a and s.cost = f1.best_cost
group by s.label;

-- macro
select avg(best_f1) from best_weight;

-- micro
select ppv, sens, 
    case 
    when (ppv+sens) > 0 then 2*ppv*sens/(ppv+sens)
    else 0
    end f1
from
(
    select tp/(tp+fp) ppv, tp/(tp+fn) sens
    from 
    (
        select sum(tp) tp, sum(tn) tn, sum(fp) fp, sum(fn) fn
        from classifier_eval e
        inner join classifier_eval_ir i on e.classifier_eval_id = i.classifier_eval_id and i.ir_class_id = 1
        inner join classifier_eval_svm s on e.classifier_eval_id = s.classifier_eval_id
        inner join best_weight w on w.label = e.label and w.best_cost = s.cost and w.best_weight = coalesce(s.weight, '')
        where experiment in ('kern-ctakes-flatne')
    ) s 
) s;

-- best cost
select cast(concat('label.',label, '.cv.costs=', best_cost) as char(100))
from best_weight
;
-- best weigths
select cast(concat('class.weight.',label, '=', best_weight) as char(100))
from best_weight
;