/* 
 * generate parameters.properties
 * first query gets optimal -W and -U parameters (cost, lambda)
 * 2nd query gets -R parameter (positive class fraction)
 */
select cast(concat('label', label, '_code.kernel.evalLines=-A 3 -W ', cost, ' -U ', lambda) as char(200)) param
from best_svmlin

union

select cast(
    concat(
    'label', dis.disease_id, '_code1_label.param.R=', truncate(sum(j.judgement_id = 0)/count(*),4), 
    char(10),
    'label', dis.disease_id, '_code2_label.param.R=', truncate(sum(j.judgement_id = 1)/count(*),4), 
    char(10),
    'label', dis.disease_id, '_code3_label.param.R=', truncate(sum(j.judgement_id = 2)/count(*),4) 
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
inner join best_svmlin b on b.label = dis.disease_id
left join hotspot_zero_vector zv 
    on zv.hotspot_instance_id = i.hotspot_instance_id 
    and zv.cutoff = b.cutoff
where zv.hotspot_zero_vector_id is null
and i.experiment = 'bag-word'
group by dis.disease_id
;
