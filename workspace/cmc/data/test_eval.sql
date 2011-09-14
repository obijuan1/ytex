select *, 2*ppv*sens/(ppv+sens)
from
(
	select tp/(tp+fp) ppv, tp/(tp+fn) sens
	from
	(
		select sum(tp) tp, sum(fp) fp, sum(tn) tn, sum(fn) fn
		from classifier_eval e
		inner join classifier_eval_ir i on e.classifier_eval_id = i.classifier_eval_id and i.ir_class_id = 1
		where experiment = 'sujeevan-test'
	) s
)s
;