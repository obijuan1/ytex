select cast(
    concat(
    'class.weight.',dis.disease_id, '= ,' , 
    ' -w0 ', truncate((count(*)-sum(j.judgement_id = 0))/count(*),4), 
    ' -w1 ', truncate((count(*)-sum(j.judgement_id = 1))/count(*),4), 
    ' -w2 ', truncate((count(*)-sum(j.judgement_id = 2))/count(*),4)
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
and i.experiment = '@kernel.experiment@'
group by dis.disease_id;