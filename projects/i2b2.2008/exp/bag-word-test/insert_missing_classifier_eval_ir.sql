/**
 * for some disease and hotspot cutoffs, we may be missing entire classes
 * add the missing truth tables
 */
drop table if exists tmp_missing_ir;
create temporary table tmp_missing_ir
as
select e.classifier_eval_id, e.param1, d.disease_id, d.disease, j.judgement_id
from classifier_eval e
inner join i2b2_2008_disease d on e.label = d.disease_id
inner join i2b2_2008_test_judgement j on j.disease = d.disease
/* filter out the class ids for which we have the ir metrics */
left join classifier_eval_ir ir 
    on ir.classifier_eval_id = e.classifier_eval_id 
    and ir.ir_class_id = j.judgement_id
where ir.classifier_eval_ir_id is null
and e.name = 'i2b2.2008'
and e.experiment = 'bag-word-test'
;

create unique index NK_tmp_missing_ir on tmp_missing_ir (classifier_eval_id, param1, disease, disease_id, judgement_id);

insert into classifier_eval_ir (classifier_eval_id, ir_class_id, tp, tn, fn, fp)
select fc.classifier_eval_id, fc.judgement_id, 
    0 tp, 
    sum(j.judgement_id <> fc.judgement_id) tn,
    sum(j.judgement_id = fc.judgement_id) fn,
    0 fp
from
/* classifier evaluation, and missing class ids */
tmp_missing_ir fc 
/* get the judgement for these instances */
inner join i2b2_2008_anno a 
    on a.source = 'intuitive' 
    and a.disease = fc.disease
/* limit to test documents */
inner join i2b2_2008_doc d
    on a.docId = d.docId
    and d.documentSet = 'test'
inner join i2b2_2008_judgement j 
    on j.judgement = a.judgement
inner join hotspot_instance i 
    on i.experiment = 'bag-word' 
    and i.label = fc.disease 
    and i.instance_id = a.docId
/* filter out zero vectors */
left join hotspot_zero_vector zv 
    on zv.hotspot_instance_id = i.hotspot_instance_id 
    and zv.cutoff = fc.param1
where zv.hotspot_zero_vector_id is null
group by fc.classifier_eval_id, fc.judgement_id
;
