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
select a.instance_id, t.anno_base_id, r.feature_rank_id
from corpus_doc d /* documents */
/* document annotations - only intuitive */
inner join corpus_label a
  on a.instance_id = d.instance_id
  and a.corpus_name = d.corpus_name
/* feature eval - infogain on entire training set */
inner join feature_eval e
  on e.cv_fold_id = 0
  and e.label = a.label
  and e.corpus_name = a.corpus_name
  and e.featureset_name = 'usword'
  and e.type = 'InfoGainAttributeEval'
/* feature rank */
inner join feature_rank r on r.feature_eval_id = e.feature_eval_id
/* join to word token via dockey - anno base - document - anno base - word token */
inner join document doc 
	on doc.instance_id = d.instance_id 
	and doc.analysis_batch = d.corpus_name
inner join anno_base ab 
	on ab.document_id = doc.document_id
inner join anno_token t
	on t.coveredtext = r.feature_name
    and t.anno_base_id = ab.anno_base_id
/* limit to top features */
where r.evaluation >= 0.008
	and d.corpus_name = 'i2b2.2008'
;


/* delete hotspot instances and sentences for this experiment */
delete hi,s
from hotspot_instance hi
left join hotspot_sentence s on s.hotspot_instance_id = hi.hotspot_instance_id
where experiment = 'word'
and corpus_name = 'i2b2.2008'
;

/*
* populate hotspot_instance
*/
insert into hotspot_instance (instance_id, label, corpus_name, experiment)
select instance_id, label, corpus_name, 'word'
from corpus_label a 
where a.corpus_name = 'i2b2.2008'
;

/*
 * for every sentence get the maximum evaluation that would cause it to be included.
 * look at a 100-character window on either side of a hotspot - 
 * any sentence that overlaps this window is included.
 */
insert into hotspot_sentence (hotspot_instance_id, anno_base_id, evaluation, rank)
select h.hotspot_instance_id, bctx.anno_base_id, max(r.evaluation), min(r.rank)
from hotspot_instance h
/* join hotspot_instance to hotspot via feature_eval and instance_id */
inner join hotspot ho on h.instance_id = ho.instance_id
inner join feature_rank r on r.feature_rank_id = ho.feature_rank_id
inner join feature_eval e 
    on e.feature_eval_id = r.feature_eval_id 
 	and e.corpus_name = h.corpus_name
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
where h.experiment = 'word'
and h.corpus_name = 'i2b2.2008'
/* testing
and h.instance_id = 2
and h.label = 'Asthma'
*/
group by h.hotspot_instance_id, bctx.anno_base_id;

/*
 * get the best evaluation and rank for each hotspot_instance
 */
update hotspot_instance hi
left join (
	select s.hotspot_instance_id, max(evaluation) evaluation, min(rank) rank
	from hotspot_sentence s
	inner join hotspot_instance i 
		on i.hotspot_instance_id = s.hotspot_instance_id
		and  i.experiment = 'word'
		and i.corpus_name = 'i2b2.2008'
	group by hotspot_instance_id
) m on hi.hotspot_instance_id = m.hotspot_instance_id
set hi.max_evaluation = coalesce(evaluation, 0),
hi.min_rank = coalesce(rank, 100000)
where hi.experiment = 'word'
	and hi.corpus_name = 'i2b2.2008'
;