/* 
 * get the labels with > 2 training examples. 
 */
select *
from
(
select label_id, sum(case when class = 1 then 1 else 0 end) lc
from corpus_doc d
inner join corpus_doc_anno a on d.doc_id = a.doc_id and a.corpus_name = d.corpus_name
inner join corpus_label l on l.corpus_name = d.corpus_name and a.label = l.label
group by label_id
) s where lc > 2
order by cast(label_id as decimal(5))

/*
 * get the best parameter for svmlin from cross-validation
 * 
 */

drop table if exists best_f1;
create temporary table best_f1 as
select label, truncate(max(f1a), 3) f1
from
(
    select label, cost, coalesce(weight, '') weight, avg(f1) f1a
    from classifier_eval e
    inner join classifier_eval_ir i on e.classifier_eval_id = i.classifier_eval_id and i.ir_class_id = 1
    inner join classifier_eval_svm s on e.classifier_eval_id = s.classifier_eval_id
    where experiment in ('kpca-svmlin')
    group by label, cost, gamma
) s
group by label
;

drop table if exists best_cost_svmlin;
create temporary table best_cost_svmlin as
select s.label, f1, min(cost) cost
from
(
    select label, cost, gamma, avg(f1) f1a
    from classifier_eval e
    inner join classifier_eval_ir i on e.classifier_eval_id = i.classifier_eval_id and i.ir_class_id = 1
    inner join classifier_eval_svm s on e.classifier_eval_id = s.classifier_eval_id
    where experiment in ('kpca-svmlin')
    group by label, cost, gamma
) s inner join best_f1 f1 on f1.label = s.label and f1.f1 <= s.f1a
group by s.label;

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
        inner join best_cost_svmlin w on w.label = e.label and w.cost = s.cost 
        where experiment in ('kpca-svmlin')
    ) s 
) s;


select s.label, s.f1, l.best_f1
from best_cost_svmlin s 
inner join best_weight l on s.label = l.label;