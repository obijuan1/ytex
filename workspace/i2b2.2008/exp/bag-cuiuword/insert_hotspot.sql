delete ho
from hotspot ho
inner join feature_rank r on r.feature_rank_id = ho.feature_rank_id
inner join feature_eval e
	on e.feature_eval_id = r.feature_eval_id
	and e.corpus_name = 'i2b2.2008'
	and e.type = 'infogain-imputed' 
	and e.param1 = 'rbpar'
	and e.cv_fold_id = 0
;


insert into hotspot (instance_id, anno_base_id, feature_rank_id)
select distinct a.docId, c.anno_base_id, r.feature_rank_id
from i2b2_2008_doc d /* documents */
/* document annotations - only intuitive */
inner join i2b2_2008_anno a
  on a.docId = d.docId
  and a.source = 'intuitive'
/* feature eval - fold 0 and name i2b2.2008 - infogain on entire training set */
inner join feature_eval e
 	on e.corpus_name = 'i2b2.2008'
	and e.cv_fold_id = 0
	and e.label = a.disease
	and e.type = 'infogain-imputed' 
	and e.param1 = 'rbpar'
/* feature rank */
inner join feature_rank r on r.feature_eval_id = e.feature_eval_id
/* join to concept token via document - anno base - concept token */
inner join document doc on doc.uid = d.docId and doc.analysis_batch = 'i2b2.2008'
inner join anno_base wb on wb.document_id = doc.document_id
inner join anno_ontology_concept c 
	on c.anno_base_id = wb.anno_base_id 
	and c.code = r.feature_name
/* limit to top features. for ambert mean was 0.08 - 0.008 should be generous */
where r.evaluation >= 0.008
;


/* delete hotspot instances for this experiment */
delete from hotspot_instance 
where experiment = 'bag-cuiuword'
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
select d.docId, a.disease, 'i2b2.2008', 'bag-cuiuword'
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
	and e.cv_fold_id = 0    
	and e.label = h.label
    and
	(
		(e.type = 'InfoGainAttributeEval' and e.featureset_name = 'word')
		or
		(e.type = 'infogain-imputed' and e.featureset_name = 'rbpar')
	)
/* get sentence for hotspot */
inner join anno_contain ac on ac.child_anno_base_id = ho.anno_base_id and ac.parent_uima_type_id = 9
where h.experiment = 'bag-cuiuword'
and h.corpus_name = 'i2b2.2008'
/* testing
and h.instance_id = 2
and h.label = 'Asthma'
*/
group by h.hotspot_instance_id, ac.parent_anno_base_id;

