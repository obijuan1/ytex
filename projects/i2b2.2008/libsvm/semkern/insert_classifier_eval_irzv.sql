delete classifier_eval_irzv
from classifier_eval_irzv
inner join classifier_eval e on classifier_eval_irzv.classifier_eval_id = e.classifier_eval_id
where e.experiment like 'semkern%' or e.experiment like 'linkern%' or e.experiment like 'lchkern%';


insert into classifier_eval_irzv (classifier_eval_id, ir_class_id, tp, tn, fp, fn, ppv, npv, sens, spec, f1)
select s.*,
  case when ppv + sens > 0 then 2*ppv*sens/(ppv+sens) else 0 end f1
from
(
select s.*,
  case when tp+fp <> 0 then tp/(tp+fp) else 0 end ppv,
  case when tn+fn <> 0 then tn/(tn+fn) else 0 end npv,
  case when tp+fn <> 0 then tp/(tp+fn) else 0 end sens,
  case when tn+fp <> 0 then tn/(tn+fp) else 0 end spec
from
(
select
  e.classifier_eval_id,
  ir.ir_class_id,
  ir.tp + coalesce(z.tp, 0) tp,
  ir.tn + coalesce(z.tn, 0) tn,
  ir.fp + coalesce(z.fp, 0) fp,
  ir.fn + coalesce(z.fn, 0) fn
from classifier_eval_ir ir
inner join classifier_eval e on ir.classifier_eval_id = e.classifier_eval_id
left join hotspot_zero_vector_tt z
  on (z.name, substring(z.experiment, 7), z.label, z.run, z.fold, z.ir_class_id) = (e.name, substring(e.experiment,8), e.label, e.run, e.fold, ir.ir_class_id)
where (e.experiment like 'semkern%' or e.experiment like 'linkern%' or e.experiment like 'lchkern%')
and z.experiment like 'bocuis%'
) s
) s
;