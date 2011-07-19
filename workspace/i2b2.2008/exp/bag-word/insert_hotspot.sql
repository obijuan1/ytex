delete ho
from hotspot ho
inner join feature_rank r on r.feature_rank_id = ho.feature_rank_id
inner join feature_eval e
	on e.feature_eval_id = r.feature_eval_id
	and e.corpus_name = 'i2b2.2008'
	and e.type = 'InfoGainAttributeEval'
	and e.featureset_name = 'word'
	and e.cv_fold_id = 0
;


insert into hotspot (instance_id, anno_base_id, feature_rank_id)
select a.docId, w.anno_base_id, r.feature_rank_id
from i2b2_2008_doc d /* documents */
/* document annotations - only intuitive */
inner join i2b2_2008_anno a
  on a.docId = d.docId
  and a.source = 'intuitive'
/* feature eval - fold 0 and name i2b2.2008 - infogain on entire training set */
inner join feature_eval e
 	on e.corpus_name = 'i2b2.2008'
	and e.type = 'InfoGainAttributeEval'
	and e.featureset_name = 'word'
	and e.cv_fold_id = 0
	and e.label = a.disease
/* feature rank */
inner join feature_rank r on r.feature_eval_id = e.feature_eval_id
/* join to word token via document - anno base - word token */
inner join document doc on doc.uid = d.docId and doc.analysis_batch = 'i2b2.2008'
inner join anno_base wb on wb.document_id = doc.document_id
inner join anno_word_token w on w.anno_base_id = wb.anno_base_id and w.canonical_form = r.feature_name
/* limit to top features. for ambert mean was 0.08 - 0.008 should be generous */
where r.evaluation >= 0.008
;


/* delete hotspot instances for this experiment */
delete from hotspot_instance 
where experiment = 'bag-word'
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
select d.docId, a.disease, 'i2b2.2008', 'bag-word'
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
 	and e.corpus_name = 'i2b2.2008'
	and e.type = 'InfoGainAttributeEval'
	and e.featureset_name = 'word'
	and e.cv_fold_id = 0    
	and e.label = h.label /* must match label */
/* get sentence for hotspot */
inner join anno_contain ac on ac.child_anno_base_id = ho.anno_base_id and ac.parent_uima_type_id = 9
where h.experiment = 'bag-word'
and h.corpus_name = 'i2b2.2008'
/* testing
and h.instance_id = 2
and h.label = 'Asthma'
*/
group by h.hotspot_instance_id, ac.parent_anno_base_id;

