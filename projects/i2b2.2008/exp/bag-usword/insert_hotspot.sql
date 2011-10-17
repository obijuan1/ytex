delete ho
from hotspot ho
inner join feature_rank r on r.feature_rank_id = ho.feature_rank_id
inner join feature_eval e
	on e.feature_eval_id = r.feature_eval_id
	and e.corpus_name = 'i2b2.2008'
	and e.type = 'InfoGainAttributeEval'
	and e.featureset_name = 'usword'
	and e.cv_fold_id = 0
;

insert into hotspot (instance_id, anno_base_id, feature_rank_id)
select a.docId,  wb.anno_base_id, r.feature_rank_id
from i2b2_2008_doc d /* documents */
/* document annotations - only intuitive */
inner join i2b2_2008_anno a
  on a.docId = d.docId
  and a.source = 'intuitive'
/* feature eval - infogain on entire training set */
inner join feature_eval e
  on e.cv_fold_id = 0
  and e.label = a.disease
  and e.corpus_name = 'i2b2.2008'
  and e.featureset_name = 'usword'
  and e.type = 'InfoGainAttributeEval'
/* feature rank */
inner join feature_rank r on r.feature_eval_id = e.feature_eval_id
/* join to word token via dockey - anno base - document - anno base - word token */
inner join document doc on doc.uid = d.docId and doc.analysis_batch = 'i2b2.2008'
inner join anno_base wb 
	on wb.document_id = doc.document_id 
	and wb.uima_type_id = 26 
	and wb.covered_text = r.feature_name
/* limit to top features */
where r.evaluation >= 0.008
;


/* delete hotspot instances and sentences for this experiment */
delete hi,s
from hotspot_instance hi
left join hotspot_sentence s on s.hotspot_instance_id = hi.hotspot_instance_id
where experiment = 'bag-usword'
and corpus_name = 'i2b2.2008'
;

/*
* populate hotspot_instance
*/
insert into hotspot_instance (instance_id, label, corpus_name, experiment)
select docId, disease, 'i2b2.2008', 'bag-usword'
from i2b2_2008_anno a 
where a.source = 'intuitive'
;

/*
 * for every word and number get the maximum evaluation that would cause it to be included.
 * look at a 100-character window on either side.
 * hotspot_sentence is supposed to be for sentences; we are abusing it here 
 * for words
 */
insert into hotspot_sentence (hotspot_instance_id, anno_base_id, evaluation)
select h.hotspot_instance_id, bctx.anno_base_id, max(r.evaluation)
from hotspot_instance h
/* join hotspot_instance to hotspot via feature_eval and instance_id */
inner join hotspot ho on h.instance_id = ho.instance_id
inner join feature_rank r on r.feature_rank_id = ho.feature_rank_id
inner join feature_eval e 
    on e.feature_eval_id = r.feature_eval_id 
 	and e.corpus_name = 'i2b2.2008'
	and e.type = 'InfoGainAttributeEval'
	and e.featureset_name = 'usword'
	and e.cv_fold_id = 0    
	and e.label = h.label /* must match label */
/* get annotation for hotspot */
inner join anno_base ab on ab.anno_base_id = ho.anno_base_id
/* get words and number tokens +- 100 characters */
inner join anno_base bctx
  on bctx.document_id = ab.document_id
  and bctx.uima_type_id in (22, 26)
  and (bctx.span_end >= ab.span_begin - 100 and bctx.span_begin <= ab.span_end + 100)
  and bctx.covered_text is not null
where h.experiment = 'bag-usword'
and h.corpus_name = 'i2b2.2008'
/* testing
and h.instance_id = 2
and h.label = 'Asthma'
*/
group by h.hotspot_instance_id, bctx.anno_base_id;
