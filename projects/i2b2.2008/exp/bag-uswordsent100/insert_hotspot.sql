/* delete hotspot instances and sentences for this experiment */
delete hi,s
from hotspot_instance hi
left join hotspot_sentence s on s.hotspot_instance_id = hi.hotspot_instance_id
where experiment = 'bag-uswordsent100'
and corpus_name = 'i2b2.2008'
;

/*
* populate hotspot_instance
*/
insert into hotspot_instance (instance_id, label, corpus_name, experiment)
select docId, disease, 'i2b2.2008', 'bag-uswordsent100'
from i2b2_2008_anno a 
where a.source = 'intuitive'
;

/*
 * for every sentence get the maximum evaluation that would cause it to be included.
 * look at a 100-character window on either side of a hotspot - any sentence that overlaps this window is included.
 */
insert into hotspot_sentence (hotspot_instance_id, anno_base_id, evaluation, rank)
select h.hotspot_instance_id, bctx.anno_base_id, max(r.evaluation), min(r.rank)
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
  and bctx.uima_type_id in (9)
  and
  (
    (bctx.span_begin <= ab.span_end + 100 and bctx.span_end > ab.span_end)
    or
    (bctx.span_end >= ab.span_begin - 100 and bctx.span_begin < ab.span_begin)
  )
where h.experiment = 'bag-uswordsent100'
and h.corpus_name = 'i2b2.2008'
/* testing
and h.instance_id = 2
and h.label = 'Asthma'
*/
group by h.hotspot_instance_id, bctx.anno_base_id;
