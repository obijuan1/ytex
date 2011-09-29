/*
 * get the positive class fraction, export as properties.
 * the positive class fraction varies based on the zero vector cutoff.
 * parameterized by export.cutoff and kernel.hzv.experiment.
 */
select cast(concat('kernel.classrel.',disease_id, '=', truncate(yc/tot, 2)) as char(100))
from
(
    select dis.disease_id, 
        count(*) tot, 
        sum(cast(a.judgement = 'Y' as decimal(1))) yc
    from hotspot_instance i
    inner join i2b2_2008_anno a on a.docId = i.instance_id and a.source = 'intuitive' and a.disease = i.label
    inner join i2b2_2008_disease dis on dis.disease = a.disease
    inner join i2b2_2008_doc d on d.docId = i.instance_id and d.documentSet = 'train'
    left join hotspot_zero_vector zv 
    	on zv.hotspot_instance_id = i.hotspot_instance_id 
    	and zv.cutoff = @export.cutoff@
    where i.experiment = '@kernel.hzv.experiment@'
    and zv.hotspot_zero_vector_id is null
    group by dis.disease_id
) s;