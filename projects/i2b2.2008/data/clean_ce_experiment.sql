delete e,s,i,z
from classifier_eval e
inner join classifier_eval_svm s on e.classifier_eval_id = s.classifier_eval_id
inner join classifier_eval_ir i on e.classifier_eval_id = i.classifier_eval_id
left join classifier_instance_eval z on e.classifier_eval_id = z.classifier_eval_id
where e.experiment = '@kernel.experiment@' and e.name = '@kernel.name@'
;