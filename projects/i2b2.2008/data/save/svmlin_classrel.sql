/*
 * get the positive class fraction, export as properties.
 * the positive class fraction varies based on the zero vector cutoff.
 * parameterized by export.cutoff and kernel.hzv.experiment.
 * 
 * this is confusing, because the svmlin classes are incremented by 1 (0 = unlabeled)
 */
select cast(
    concat(
    'label.', dis.disease_id, '.class.1=', truncate(sum(j.judgement_id = 0)/count(*),3), 
    char(10),
    'label.', dis.disease_id, '.class.2=', truncate(sum(j.judgement_id = 1)/count(*),3), 
    char(10),
    'label.', dis.disease_id, '.class.3=', truncate(sum(j.judgement_id = 2)/count(*),3) 
) as char(200))
from hotspot_instance i
inner join i2b2_2008_doc d 
    on d.docId = i.instance_id 
    and d.documentSet = 'train'
inner join i2b2_2008_anno a 
    on a.docId = d.docId 
    and a.source = 'intuitive' 
    and i.label = a.disease
inner join i2b2_2008_judgement j on j.judgement = a.judgement
inner join i2b2_2008_disease dis on dis.disease = a.disease
left join hotspot_zero_vector zv 
    on zv.hotspot_instance_id = i.hotspot_instance_id 
    and zv.cutoff = @export.cutoff@
where zv.hotspot_zero_vector_id is null
and i.experiment = '@kernel.hzv.experiment@'
group by dis.disease_id
;