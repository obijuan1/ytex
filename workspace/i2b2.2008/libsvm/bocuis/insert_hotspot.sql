/* clean up any existing hotspots for the feature sets */
delete hotspot
from hotspot
inner join feature_rank r on hotspot.feature_rank_id = r.feature_rank_id
inner join feature_eval e on e.feature_eval_id = r.feature_eval_id and e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui')
;

/* word hotspots */
insert into hotspot (instance_id, anno_base_id, feature_rank_id)
select a.docId, w.anno_base_id, r.feature_rank_id
from i2b2_2008_doc d /* documents */
/* document annotations - only intuitive */
inner join i2b2_2008_anno a
  on a.docId = d.docId
  and a.source = 'intuitive'
/* feature eval - fold 0 and name i2b2.2008-train - infogain on entire training set */
inner join feature_eval e
  on e.cv_fold_id = 0
  and e.label = a.disease
  and e.name  = 'i2b2.2008-ncuiword'
/* feature rank */
inner join feature_rank r on r.feature_eval_id = e.feature_eval_id
/* join to word token via dockey - anno base - document - anno base - word token */
inner join anno_dockey k on k.uid = d.docId
inner join anno_base kb on kb.anno_base_id = k.anno_base_id
inner join document doc on doc.document_id = kb.document_id and doc.analysis_batch = 'i2b2.2008'
inner join anno_base wb on wb.document_id = kb.document_id
inner join anno_word_token w on w.anno_base_id = wb.anno_base_id and w.canonical_form = r.feature_name
/* don't use words that are contained within named entities */
left join anno_contain ac on ac.child_anno_base_id = wb.anno_base_id and ac.parent_uima_type_id = 8
/* limit to top features */
where r.evaluation > 0.03
and ac.parent_anno_base_id is null
;

/* cui hotspots */
insert into hotspot (instance_id, anno_base_id, feature_rank_id)
/* cuis are duplicated, so use distinct */
select distinct a.docId, w.anno_base_id, r.feature_rank_id
from i2b2_2008_doc d /* documents */
/* document annotations - only intuitive */
inner join i2b2_2008_anno a
  on a.docId = d.docId
  and a.source = 'intuitive'
/* feature eval - fold 0 and name i2b2.2008-train - infogain on entire training set */
inner join feature_eval e
  on e.cv_fold_id = 0
  and e.label = a.disease
  and e.name  = 'i2b2.2008-cui'
/* feature rank */
inner join feature_rank r on r.feature_eval_id = e.feature_eval_id
/* join to word token via dockey - anno base - document - anno base - word token */
inner join anno_dockey k on k.uid = d.docId
inner join anno_base kb on kb.anno_base_id = k.anno_base_id
inner join document doc on doc.document_id = kb.document_id and doc.analysis_batch = 'i2b2.2008'
inner join anno_base wb on wb.document_id = kb.document_id
inner join anno_ontology_concept w on w.anno_base_id = wb.anno_base_id and w.code = r.feature_name
/* limit to top 1000 features */
where r.evaluation > 0.03
;

delete from hotspot_feature_eval where name = 'i2b2.2008-cui';

insert into hotspot_feature_eval (name, label, instance_id, feature_name, evaluation)
select 'i2b2.2008-cui', s.*
from
(
select e.label, h.instance_id, w.canonical_form, max(r.evaluation) evaluation
from hotspot h
/* limit to the feature sets we're working on */
inner join feature_rank r on r.feature_rank_id = h.feature_rank_id
inner join feature_eval e on e.feature_eval_id = r.feature_eval_id and e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui')
/* get sentence for hotspot */
inner join anno_contain ac on ac.child_anno_base_id = h.anno_base_id and ac.parent_uima_type_id = 9
/* get words in sentence */
inner join anno_contain acw on acw.parent_anno_base_id = ac.parent_anno_base_id and ac.child_uima_type_id = 25
/* get stemmed word */
inner join anno_word_token w on w.anno_base_id = acw.child_anno_base_id and w.canonical_form is not null
/* don't use words within a named entity */
left join anno_contain acn on acn.child_anno_base_id = acw.child_anno_base_id and acn.parent_uima_type_id = 8
where acn.parent_anno_base_id is null
/* test
and h.instance_id = 1
*/
/* for each word get the best evaluation that causes it to be included */
group by e.label, h.instance_id, w.canonical_form
) s
;

insert into hotspot_feature_eval (name, label, instance_id, feature_name, evaluation)
select 'i2b2.2008-cui', s.*
from
(
select e.label, h.instance_id, w.code, max(r.evaluation) evaluation
from hotspot h
/* limit to the feature sets we're working on */
inner join feature_rank r on r.feature_rank_id = h.feature_rank_id
inner join feature_eval e on e.feature_eval_id = r.feature_eval_id and e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui')
/* get sentence for hotspot */
inner join anno_contain ac on ac.child_anno_base_id = h.anno_base_id and ac.parent_uima_type_id = 9
/* get named entities in sentence */
inner join anno_contain acw on acw.parent_anno_base_id = ac.parent_anno_base_id and ac.child_uima_type_id = 8
/* get umls cuis */
inner join anno_ontology_concept w on w.anno_base_id = acw.child_anno_base_id
/* test
where h.instance_id = 1
*/
/* for each word get the best evaluation that causes it to be included */
group by e.label, h.instance_id, w.code
) s
;


