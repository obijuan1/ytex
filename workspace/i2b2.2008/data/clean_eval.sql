delete from classifier_eval where experiment in ('kern-cuiword-lin', 'kern-cuiword-filteredlin');

delete classifier_eval_svm
from classifier_eval_svm
left join classifier_eval
        on classifier_eval.classifier_eval_id = classifier_eval_svm.classifier_eval_id
where classifier_eval.classifier_eval_id is null
;

delete classifier_eval_ir
from classifier_eval_ir
left join classifier_eval
        on classifier_eval.classifier_eval_id = classifier_eval_ir.classifier_eval_id
where classifier_eval.classifier_eval_id is null
;

delete classifier_eval_irzv
from classifier_eval_irzv
left join classifier_eval
        on classifier_eval.classifier_eval_id = classifier_eval_irzv.classifier_eval_id
where classifier_eval.classifier_eval_id is null
;

delete classifier_instance_eval
from classifier_instance_eval
left join classifier_eval
        on classifier_eval.classifier_eval_id = classifier_instance_eval.classifier_eval_id
where classifier_eval.classifier_eval_id is null
;

delete classifier_instance_eval_prob
from classifier_instance_eval
left join classifier_instance_eval_prob
        on classifier_instance_eval_prob.classifier_instance_eval_id = classifier_instance_eval.classifier_instance_eval_id
where classifier_instance_eval.classifier_instance_eval_id is null
;
