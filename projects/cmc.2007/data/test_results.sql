-- get micro-averaged f1 across all labels
select 'micro', round(f1, 6) f1, round(ppv, 6) ppv, round(sens, 6) sens
from
(
    select *, if(ppv+sens > 0, 2*ppv*sens/(ppv+sens), 0) f1
    from
    (
        select *, if(tp+fp > 0, tp/(tp+fp), 0) ppv, if(tp+fn >0, tp/(tp+fn), 0) sens
        from
        (
            select sum(tp) tp, sum(fp) fp, sum(tn) tn, sum(fn) fn
            from classifier_eval e
            inner join classifier_eval_ir i on e.classifier_eval_id = i.classifier_eval_id
			where experiment = '@kernel.experiment@-test'
			and name = '@kernel.name@'
            and ir_class_id = '1'
        ) s 
    ) s
) s;

-- get macro-averaged f1 across all labels
select 'macro', round(avg(f1),6) f1, round(avg(ppv),6) ppv, round(avg(sens),6) sens
from classifier_eval e
inner join classifier_eval_ir i on e.classifier_eval_id = i.classifier_eval_id
where experiment = '@kernel.experiment@-test'
and name = '@kernel.name@'
and ir_class_id = '1'
;
