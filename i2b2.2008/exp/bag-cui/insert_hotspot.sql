/* delete hotspot instances for this experiment */
delete from hotspot_instance 
where experiment = 'bag-cui'
and corpus_name = 'i2b2.2008'
;

/* delete hotspot_sentences for this experiment */
delete s 
from hotspot_sentence s 
left join hotspot_instance i 
on s.hotspot_instance_id = i.hotspot_instance_id
where i.hotspot_instance_id is null
;

/*
* populate hotspot_instance
*/
insert into hotspot_instance (instance_id, label, corpus_name, experiment)
select d.docId, a.disease, 'i2b2.2008', 'bag-cui'
from i2b2_2008_doc d
inner join i2b2_2008_anno a 
    on a.docId = d.docId 
    and a.source = 'intuitive'
;

/*
 * get the sentences that contain hotspots
 */
insert into hotspot_sentence (hotspot_instance_id, anno_base_id, evaluation)
select h.hotspot_instance_id, ac.parent_anno_base_id, max(r.evaluation)
from hotspot_instance h
/* join hotspot_instance to hotspot via feature_eval and instance_id */
inner join hotspot ho on h.instance_id = ho.instance_id
inner join feature_rank r on r.feature_rank_id = ho.feature_rank_id
inner join feature_eval e 
    on e.feature_eval_id = r.feature_eval_id 
    and e.corpus_name = h.corpus_name
    and e.label = h.label /* must match label */
    and 
    (
        (e.featureset_name = '' and e.type = 'mutualinfo-child' and param1 = 'rbpar')
    )
/* get sentence for hotspot */
inner join anno_contain ac on ac.child_anno_base_id = ho.anno_base_id and ac.parent_uima_type_id = 9
where h.experiment = 'bag-cui'
and h.corpus_name = 'i2b2.2008'
/* testing
and h.instance_id = 2
and h.label = 'Asthma'
*/
group by h.hotspot_instance_id, ac.parent_anno_base_id;