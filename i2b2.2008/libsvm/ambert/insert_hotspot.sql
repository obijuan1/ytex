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
  and e.name = 'i2b2.2008-train'
/* feature rank */
inner join feature_rank r on r.feature_eval_id = e.feature_eval_id
/* join to word token via dockey - anno base - document - anno base - word token */
inner join anno_dockey k on k.uid = d.docId
inner join anno_base kb on kb.anno_base_id = k.anno_base_id
inner join document doc on doc.document_id = kb.document_id and doc.analysis_batch = 'i2b2.2008'
inner join anno_base wb on wb.document_id = kb.document_id
inner join anno_word_token w on w.anno_base_id = wb.anno_base_id and w.canonical_form = r.feature_name
/* limit to top 1000 features */
where r.rank < 1000
;

insert into hotspot_feature (label, instance_id, feature_name, rank)
select e.label, h.instance_id, w.canonical_form, min(r.rank) rank
from hotspot h
/* get annotation for hotspot */
inner join anno_base ab on ab.anno_base_id = h.anno_base_id
/* get feature rank for hotspot */
inner join feature_rank r on r.feature_rank_id = h.feature_rank_id
inner join feature_eval e on r.feature_eval_id = e.feature_eval_id
/* get words +- 100 characters */
inner join anno_base bctx
  on bctx.document_id = ab.document_id
  and bctx.uima_type_id = 25
  and (bctx.span_end >= ab.span_begin - 100 and bctx.span_begin <= ab.span_end + 100)
inner join anno_word_token w
  on bctx.anno_base_id = w.anno_base_id
  and w.canonical_form is not null
/* for each word get the best rank that causes it to be included */
group by e.label, h.instance_id, w.canonical_form
;


insert into hotspot_zero_vector (label, instance_id, cutoff)
select a.disease, d.docId, 100
from i2b2_2008_doc d
inner join i2b2_2008_anno a on d.docId = a.docId and a.source = 'intuitive'
left join
(
select label, instance_id, count(*) fc
from hotspot_feature
where rank <= 100
group by label, instance_id
) h on d.docId = h.instance_id and a.disease = h.label
where fc is null
;

insert into hotspot_zero_vector (label, instance_id, cutoff)
select a.disease, d.docId, 75
from i2b2_2008_doc d
inner join i2b2_2008_anno a on d.docId = a.docId and a.source = 'intuitive'
left join
(
select label, instance_id, count(*) fc
from hotspot_feature
where rank <= 75
group by label, instance_id
) h on d.docId = h.instance_id and a.disease = h.label
where fc is null
;

insert into hotspot_zero_vector (label, instance_id, cutoff)
select a.disease, d.docId, 50
from i2b2_2008_doc d
inner join i2b2_2008_anno a on d.docId = a.docId and a.source = 'intuitive'
left join
(
select label, instance_id, count(*) fc
from hotspot_feature
where rank <= 50
group by label, instance_id
) h on d.docId = h.instance_id and a.disease = h.label
where fc is null
;

insert into hotspot_zero_vector (label, instance_id, cutoff)
select a.disease, d.docId, 25
from i2b2_2008_doc d
inner join i2b2_2008_anno a on d.docId = a.docId and a.source = 'intuitive'
left join
(
select label, instance_id, count(*) fc
from hotspot_feature
where rank <= 25
group by label, instance_id
) h on d.docId = h.instance_id and a.disease = h.label
where fc is null
;


insert into hotspot_zero_vector (label, instance_id, cutoff)
select a.disease, d.docId, 10
from i2b2_2008_doc d
inner join i2b2_2008_anno a on d.docId = a.docId and a.source = 'intuitive'
left join
(
select label, instance_id, count(*) fc
from hotspot_feature
where rank <= 10
group by label, instance_id
) h on d.docId = h.instance_id and a.disease = h.label
where fc is null
;


-- convert from rank into evaluation
insert into hotspot_feature_eval (name, label, instance_id, feature_name, evaluation)
select 'i2b2.2008-word', hf.label, hf.instance_id, hf.feature_name, r.evaluation
from hotspot_feature hf
inner join feature_eval e on hf.label = e.label and e.name = 'i2b2.2008-train' and e.cv_fold_id = 0
inner join feature_rank r on r.feature_eval_id = e.feature_eval_id and r.rank = hf.rank
;


insert into hotspot_zero_vector (label, instance_id, cutoff)
select a.disease, d.docId, 5
from i2b2_2008_doc d
inner join i2b2_2008_anno a on d.docId = a.docId and a.source = 'intuitive'
left join
(
select label, instance_id, count(*) fc
from hotspot_feature
where rank <= 5
group by label, instance_id
) h on d.docId = h.instance_id and a.disease = h.label
where fc is null
;





/*
takes way too long per fold.
generating hotspot vector per instance: 1.8 sec
doing this for 800 instances x 16 labels x 5 runs x 2 folds x 1.8 sec = 64 hours
doing this just for 1 fold (/10) = 6.4 hours

insert into hotspot (instance_id, anno_base_id, feature_rank_id)
select i.cv_fold_instance_id, w.anno_base_id, r.feature_rank_id
from cv_fold f
inner join cv_fold_instance i on f.cv_fold_id = i.cv_fold_id
inner join feature_eval e on e.cv_fold_id = f.cv_fold_id
inner join feature_rank r on r.feature_eval_id = e.feature_eval_id
inner join anno_dockey k on k.uid = i.instance_id
inner join anno_base kb on kb.anno_base_id = k.anno_base_id
inner join anno_base wb on wb.document_id = kb.document_id
inner join anno_word_token w on w.anno_base_id = wb.anno_base_id and w.canonical_form = r.feature_name
where r.evaluation > 0.04
;

create table hotspot_feature (
	hotspot_feature_id int not null auto_increment primary key,
	label varchar(50) not null,
	instance_id int not null,
	feature_name varchar(50),
	rank int
);

insert into hotspot_feature (label, instance_id, feature_name, evaluation)

select h.instance_id, w.canonical_form, max(r.evaluation) evaluation
from hotspot h
inner join cv_fold_instance i on h.instance_id = i.cv_fold_instance_id
inner join cv_fold f on f.cv_fold_id = i.cv_fold_id
inner join anno_base ab on ab.anno_base_id = h.anno_base_id
inner join feature_rank r on r.feature_rank_id = h.feature_rank_id
inner join anno_base bctx
  on bctx.document_id = ab.document_id
  and bctx.uima_type_id = 25
  and (bctx.span_end >= ab.span_begin - 100 or bctx.span_begin <= ab.span_end + 100)
inner join anno_word_token w
  on bctx.anno_base_id = w.anno_base_id
  and w.canonical_form is not null
where f.fold = 2 and f.run = 1 and f.label = 'Asthma'
*/