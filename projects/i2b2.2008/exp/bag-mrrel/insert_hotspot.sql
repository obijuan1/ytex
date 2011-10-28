delete ho
from hotspot ho
inner join feature_rank r on r.feature_rank_id = ho.feature_rank_id
inner join feature_eval e
	on e.feature_eval_id = r.feature_eval_id
	and e.corpus_name = 'i2b2.2008'
	and e.type = 'mrrel'
	and e.cv_fold_id = 0
;

insert into hotspot (instance_id, anno_base_id, feature_rank_id)
select distinct a.docId, c.anno_base_id, r.feature_rank_id
from i2b2_2008_doc d /* documents */
/* document annotations - only intuitive */
inner join i2b2_2008_anno a
  on a.docId = d.docId
  and a.source = 'intuitive'
/* feature eval - mrrel */
inner join feature_eval e
 	on e.corpus_name = 'i2b2.2008'
	and e.cv_fold_id = 0
	and e.label = a.disease
	and e.type = 'mrrel'
/* feature rank */
inner join feature_rank r on r.feature_eval_id = e.feature_eval_id
/* join to concept token via document - anno base - concept token */
inner join document doc on doc.uid = d.docId and doc.analysis_batch = 'i2b2.2008'
inner join anno_base wb on wb.document_id = doc.document_id
inner join anno_ontology_concept c 
	on c.anno_base_id = wb.anno_base_id 
	and c.code = r.feature_name
;

/* delete hotspot instances and sentences for this experiment */
delete hi,s
from hotspot_instance hi
left join hotspot_sentence s on s.hotspot_instance_id = hi.hotspot_instance_id
where experiment = 'bag-mrrel'
and corpus_name = 'i2b2.2008'
;

/*
* populate hotspot_instance
*/
insert into hotspot_instance (instance_id, label, corpus_name, experiment)
select docId, disease, 'i2b2.2008', 'bag-mrrel'
from i2b2_2008_anno a 
where a.source = 'intuitive'
;

drop table if exists tmp_hotspot;
create table tmp_hotspot
as
/* get only word hotspots that are above the best cutoff threshold */ 
select hotspot_instance_id, anno_base_id, evaluation, rank
from hotspot_instance h
inner join i2b2_2008_disease d 
	on d.disease = h.label
inner join i2b2_2008_cv_best b 
	on b.label = d.disease_id 
	and b.experiment = 'bag-usword'
inner join hotspot ho 
	on h.instance_id = ho.instance_id
inner join feature_rank r 
	on r.feature_rank_id = ho.feature_rank_id
	and r.evaluation >= b.param1
inner join feature_eval e 
    on e.feature_eval_id = r.feature_eval_id 
 	and e.corpus_name = 'i2b2.2008'
	and e.type = 'InfoGainAttributeEval' 
	and e.featureset_name in ('usword')
	and e.cv_fold_id = 0    
	and e.label = h.label /* must match label */
where h.experiment = 'bag-mrrel'	

union

/* get all mrrel hotspots */
select hotspot_instance_id, anno_base_id, evaluation, rank
from hotspot_instance h
inner join hotspot ho 
	on h.instance_id = ho.instance_id
inner join feature_rank r 
	on r.feature_rank_id = ho.feature_rank_id
inner join feature_eval e 
    on e.feature_eval_id = r.feature_eval_id 
 	and e.corpus_name = 'i2b2.2008'
	and e.type = 'mrrel' 
	and e.cv_fold_id = 0    
	and e.label = h.label /* must match label */
where h.experiment = 'bag-mrrel'
;

create index IX_tmp_hotspot on tmp_hotspot(hotspot_instance_id, anno_base_id, evaluation, rank);


/*
 * get annotations within hotspot window.
 * get named entities, words, and numbers in the window.
 * use cui and usword hotspots.
 */
insert into hotspot_sentence (hotspot_instance_id, anno_base_id, evaluation, rank)
select h.hotspot_instance_id, bctx.anno_base_id, max(th.evaluation), min(th.rank)
from hotspot_instance h
/* join hotspot_instance to hotspot via feature_eval and instance_id */
inner join tmp_hotspot th on th.hotspot_instance_id = h.hotspot_instance_id
/* get annotation for hotspot */
inner join anno_base ab on ab.anno_base_id = th.anno_base_id
/* get words and number tokens and named entities +- 100 characters */
inner join anno_base bctx
  on bctx.document_id = ab.document_id
  and bctx.uima_type_id in (8, 22, 25, 26)
  and (bctx.span_end >= ab.span_begin - 100 and bctx.span_begin <= ab.span_end + 100)
  and (bctx.uima_type_id = 8 or bctx.covered_text is not null)
where h.experiment = 'bag-mrrel'
and h.corpus_name = 'i2b2.2008'
/* testing
and h.instance_id = 2
and h.label = 'Asthma'
*/
group by h.hotspot_instance_id, bctx.anno_base_id;

