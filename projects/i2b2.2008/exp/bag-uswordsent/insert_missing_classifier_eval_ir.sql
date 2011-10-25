/**
 * for some hotspot cutoffs + folds, we may be missing entire classes
 * add the missing truth tables
 */
drop table if exists tmp_missing_ir;
create temporary table tmp_missing_ir
as
select fc.*, e.classifier_eval_id, e.param1
from i2b2_2008_fold_class fc
inner join cv_fold f 
    on f.label = fc.disease 
    and f.cv_fold_id = fc.cv_fold_id
    and f.corpus_name = 'i2b2.2008'
inner join classifier_eval e
    on e.name = 'i2b2.2008'
    and e.experiment = 'bag-uswordsent'
    and e.label = fc.disease_id
    and e.run = f.run
    and e.fold = f.fold
/* filter out the class ids for which we have the ir metrics */
left join classifier_eval_ir ir 
    on ir.classifier_eval_id = e.classifier_eval_id 
    and ir.ir_class_id = fc.judgement_id
where ir.classifier_eval_ir_id is null
;

create unique index NK_tmp_missing_ir on tmp_missing_ir (cv_fold_id, disease, disease_id, judgement_id, classifier_eval_id, param1);

insert into classifier_eval_ir (classifier_eval_id, ir_class_id, tp, tn, fn, fp)
select fc.classifier_eval_id, fc.judgement_id, 
    0 tp, 
    sum(j.judgement_id <> fc.judgement_id) tn,
    sum(j.judgement_id = fc.judgement_id) fn,
    0 fp
from
/* get fold, classifier evaluation, and missing class ids */
tmp_missing_ir fc 
/* get the test instances for this fold */
inner join cv_fold_instance fi 
    on fi.cv_fold_id = fc.cv_fold_id 
    and fi.train = 0
/* get the judgement for these instances */
inner join i2b2_2008_anno a 
    on a.docId = fi.instance_id 
    and a.source = 'intuitive' 
    and a.disease = fc.disease
inner join i2b2_2008_judgement j 
    on j.judgement = a.judgement
inner join hotspot_instance i 
    on i.experiment = 'bag-uswordsent' 
    and i.label = fc.disease 
    and i.instance_id = fi.instance_id
/* filter out zero vectors */
left join hotspot_zero_vector zv 
    on zv.hotspot_instance_id = i.hotspot_instance_id 
    and zv.cutoff = fc.param1
where zv.hotspot_zero_vector_id is null
group by fc.classifier_eval_id, fc.judgement_id
;
