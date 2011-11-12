/* delete hotspot instances for this experiment */
delete i,s 
from hotspot_instance i left join hotspot_sentence s on s.hotspot_instance_id = i.hotspot_instance_id
where experiment = 'bag-impcuiword-hch'
and corpus_name = 'i2b2.2008'
;


/*
* populate hotspot_instance
*/
insert into hotspot_instance (instance_id, label, corpus_name, experiment)
select d.docId, a.disease, 'i2b2.2008', 'bag-impcuiword-hch'
from i2b2_2008_doc d
inner join i2b2_2008_anno a 
    on a.docId = d.docId 
    and a.source = 'intuitive'
    and a.disease = 'Hypercholesterolemia'
;

/*
 * get annotations within hotspot window.
 * get named entities, words, and numbers in the window.
 * use cui and usword hotspots.
 */
insert into hotspot_sentence (hotspot_instance_id, anno_base_id, evaluation, rank)
select h.hotspot_instance_id, bctx.anno_base_id, max(r.evaluation), min(r.rank)
from hotspot_instance h
/* join hotspot_instance to hotspot via feature_eval and instance_id */
inner join hotspot ho on h.instance_id = ho.instance_id
inner join feature_rank r on r.feature_rank_id = ho.feature_rank_id
/* get word and concept hotspots */
inner join feature_eval e 
    on e.feature_eval_id = r.feature_eval_id 
 	and e.corpus_name = 'i2b2.2008'
	and e.cv_fold_id = 0    
	and e.label = h.label /* must match label */
	and e.type = 'infogain-imputed' 
	and e.featureset_name = 'ctakes' 
	and e.param1 = 'rbpar'
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
where h.experiment = 'bag-impcuiword-hch'
and h.corpus_name = 'i2b2.2008'
/* testing
and h.instance_id = 2
and h.label = 'Asthma'
*/
group by h.hotspot_instance_id, bctx.anno_base_id;

/* force all the 'best' sentences from bag-uswordsent100 to be included, regardless of what the cui evaluation is */
update
/* join impcuiword and uswordsent100 sentences */
hotspot_sentence hs
inner join hotspot_instance hi 
	on hi.hotspot_instance_id = hs.hotspot_instance_id
	and hi.experiment = 'bag-impcuiword-hch'
	and hi.corpus_name = 'i2b2.2008'
inner join hotspot_instance hi2
	on hi2.instance_id = hi.instance_id
	and hi2.experiment = 'bag-uswordsent100'
	and hi2.corpus_name = hi.corpus_name
	and hi2.label = hi.label
inner join hotspot_sentence hs2
	on hs2.anno_base_id = hs.anno_base_id
	and hs2.hotspot_instance_id = hi2.hotspot_instance_id
	and hs2.evaluation >= 0.07
set hs.evaluation = 1
;

/* add sentences we missed using just cuis */
insert into hotspot_sentence(hotspot_instance_id, anno_base_id, evaluation, rank)
select hi.hotspot_instance_id, hs2.anno_base_id, 1, 1
from hotspot_sentence hs2
inner join hotspot_instance hi2
	on hs2.hotspot_instance_id = hi2.hotspot_instance_id
    and hi2.experiment = 'bag-uswordsent100'
	and hi2.corpus_name = 'i2b2.2008'    
/* throw out sentences we already have */
inner join hotspot_instance hi 
	on hi2.instance_id = hi.instance_id
	and hi.experiment = 'bag-impcuiword-hch'
	and hi.corpus_name = 'i2b2.2008'
	and hi2.label = hi.label
left join hotspot_sentence hs
	on hs.anno_base_id = hs2.anno_base_id
	and hi.hotspot_instance_id = hs.hotspot_instance_id
where hs.anno_base_id is null 
and hs2.evaluation >= 0.07
;