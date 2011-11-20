delete ho
from hotspot ho
inner join feature_rank r 
	on r.feature_rank_id = ho.feature_rank_id
inner join feature_eval e
	on e.feature_eval_id = r.feature_eval_id
	and e.corpus_name = 'i2b2.2008'
	and e.type = 'infogain-imputed-filt' 
	and e.featureset_name = 'ctakes'
	and e.param1 = 'rbpar'
	and e.cv_fold_id = 0
;

/*
 * get top concepts for each label.
 * use nth propagated IG's evaluation as the cutoff.
 */
drop table if exists hot_concepts;
create temporary table hot_concepts
as
select distinct e.label, r.feature_name, r.feature_rank_id
from 
/* feature eval - imputed infogain */
feature_eval e
/* feature eval - propagated infogain */
inner join feature_eval ep
    on e.corpus_name = ep.corpus_name
    and e.label = ep.label
    and e.param2 = ep.param2
    and e.featureset_name = ep.featureset_name
    and ep.type = 'infogain-propagated'
/* pick the nth propagated concept */
inner join feature_rank rp
    on rp.feature_eval_id = ep.feature_eval_id
    and rp.rank = 10
/* get corresponding imputed concepts */
inner join feature_rank r 
    on r.feature_eval_id = e.feature_eval_id
    and r.evaluation >= rp.evaluation
where e.corpus_name = 'i2b2.2008'
	and e.type = 'infogain-imputed-filt' 
	and e.featureset_name = 'ctakes'
	and e.param2 = 'rbpar'
;
create unique index NK_hc on hot_concepts(label, feature_name, feature_rank_id);

/* find hotspots corresponding to concepts */
insert into hotspot (instance_id, anno_base_id, feature_rank_id)
select distinct a.instance_id, c.anno_base_id, hc.feature_rank_id
from corpus_doc d 
/* document labels */
inner join corpus_label a
  on a.instance_id = d.instance_id
  and a.corpus_name = d.corpus_name
/* join to concept token via document - anno base - concept token */
inner join document doc 
    on doc.uid = d.instance_id 
    and doc.analysis_batch = 'i2b2.2008'
inner join anno_base wb 
    on wb.document_id = doc.document_id
inner join anno_ontology_concept c 
    on c.anno_base_id = wb.anno_base_id 
/* limit to hot concepts */
inner join hot_concepts hc
    on hc.feature_name = c.code
    and hc.label = a.label
where d.corpus_name = 'i2b2.2008'
;

/* delete hotspot instances for this experiment */
delete i,s 
from hotspot_instance i left join hotspot_sentence s on s.hotspot_instance_id = i.hotspot_instance_id
where experiment = 'imputed'
and corpus_name = 'i2b2.2008'
;


/*
* populate hotspot_instance
*/
insert into hotspot_instance (instance_id, label, corpus_name, experiment)
select instance_id, label, corpus_name, 'imputed'
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
 	and e.corpus_name = 'i2b2.2008'
	and e.type = 'infogain-imputed-filt' 
	and e.featureset_name = 'ctakes'
	and e.param2 = 'rbpar'
	and e.label = h.label /* must match label */
/* get annotation for hotspot */
inner join anno_base ab on ab.anno_base_id = ho.anno_base_id
/* get sentences +- 100 characters */
inner join anno_base bctx
  on bctx.document_id = ab.document_id
  and bctx.uima_type_id in (9)
  and
  (
    (bctx.span_begin <= ab.span_end + 100 and bctx.span_end > ab.span_end)
    or
    (bctx.span_end >= ab.span_begin - 100 and bctx.span_begin < ab.span_begin)
  )
where h.experiment = 'imputed'
and h.corpus_name = 'i2b2.2008'
/* testing 
and h.instance_id = 2
and h.label = 'Asthma'
*/
group by h.hotspot_instance_id, bctx.anno_base_id;

/* force all the 'best' sentences from word to be included, regardless of what the cui evaluation is */
update
/* join imputed and word sentences */
hotspot_sentence hs
inner join hotspot_instance hi 
	on hi.hotspot_instance_id = hs.hotspot_instance_id
	and hi.experiment = 'imputed'
	and hi.corpus_name = 'i2b2.2008'
inner join hotspot_instance hi2
	on hi2.instance_id = hi.instance_id
	and hi2.experiment = 'word'
	and hi2.corpus_name = hi.corpus_name
	and hi2.label = hi.label
inner join hotspot_sentence hs2
	on hs2.anno_base_id = hs.anno_base_id
	and hs2.hotspot_instance_id = hi2.hotspot_instance_id
/* filter to only the 'best' sentences */
inner join cv_best_svm b
	on b.label = hi.label
    and b.corpus_name = hi.corpus_name
    and b.experiment = 'word'
	and hs2.rank <= b.param1
set hs.evaluation = 1, hs.rank = 0
;


/* add sentences we missed using just cuis */
insert into hotspot_sentence(hotspot_instance_id, anno_base_id, evaluation, rank)
select hi.hotspot_instance_id, hs2.anno_base_id, 1, 1
from hotspot_sentence hs2
inner join hotspot_instance hi2
	on hs2.hotspot_instance_id = hi2.hotspot_instance_id
    and hi2.experiment = 'word'
	and hi2.corpus_name = 'i2b2.2008'    
/* filter to only the 'best' sentences */
inner join cv_best_svm b
	on b.label = hi2.label
	and b.corpus_name = hi2.corpus_name 
    and b.experiment = 'word'
	and hs2.rank <= b.param1
/* throw out sentences we already have */
inner join hotspot_instance hi 
	on hi2.instance_id = hi.instance_id
	and hi.experiment = 'imputed'
	and hi.corpus_name = hi2.corpus_name 
	and hi2.label = hi.label
left join hotspot_sentence hs
	on hs.anno_base_id = hs2.anno_base_id
	and hi.hotspot_instance_id = hs.hotspot_instance_id
where hs.anno_base_id is null;


/*
 * get the best evaluation and rank for each hotspot_instance
 */
update hotspot_instance hi
left join (
	select s.hotspot_instance_id, max(evaluation) evaluation, min(rank) rank
	from hotspot_sentence s
	inner join hotspot_instance i 
		on i.hotspot_instance_id = s.hotspot_instance_id
		and i.experiment = 'imputed'
		and i.corpus_name = 'i2b2.2008'
	group by hotspot_instance_id
) m on hi.hotspot_instance_id = m.hotspot_instance_id
set hi.max_evaluation = coalesce(evaluation, 0),
hi.min_rank = coalesce(rank, 100000)
where hi.experiment = 'imputed'
	and hi.corpus_name = 'i2b2.2008'
;