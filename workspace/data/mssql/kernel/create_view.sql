create view $(db_schema).v_classifier_eval_ir
as
select *,
  case when sens+prec > 0 then 2*sens*prec/(sens+prec) else 0 end f1
from
(
select *,
  case when tp+fp > 0 then tp/(tp+fp) else 0 end prec,
  case when tp+fn > 0 then tp/(tp+fn) else 0 end sens,
  case when fp+tn > 0 then tn/(fp+tn) else 0 end spec
from
(
select cls.classifier_eval_id, ir_class_id,
  sum(case
    when ir_class_id = target_class_id and ir_class_id = pred_class_id then 1
    else 0
  end) tp,
  sum(case
    when ir_class_id <> target_class_id and ir_class_id <> pred_class_id then 1
    else 0
  end) tn,
  sum(case
    when ir_class_id <> target_class_id and ir_class_id = pred_class_id then 1
    else 0
  end) fp,
  sum(case
    when ir_class_id = target_class_id and ir_class_id <> pred_class_id then 1
    else 0
  end) fn
from
(
select distinct ce.classifier_eval_id, target_class_id ir_class_id
from $(db_schema).classifier_eval ce
inner join $(db_schema).classifier_instance_eval ci
on ce.classifier_eval_id = ci.classifier_eval_id
) cls
inner join $(db_schema).classifier_instance_eval ci on cls.classifier_eval_id = ci.classifier_eval_id
group by cls.classifier_eval_id, ir_class_id
) s
) s
;

