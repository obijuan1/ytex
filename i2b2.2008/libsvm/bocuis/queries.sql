/*
 * Queries to view features with high mutual information
 */

select label,
  count(*) total,
  sum(case when e.name = 'i2b2.2008-cui' then 1 else 0 end) cui,
  sum(case when e.name = 'i2b2.2008-ncuiword' then 1 else 0 end) ncuiword
from feature_eval e inner join feature_rank r on e.feature_eval_id = r.feature_eval_id
where e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui')
and r.evaluation > 0.03
group by label;

select label,
  count(*) total,
  sum(case when e.name = 'i2b2.2008-cui' then 1 else 0 end) cui,
  sum(case when e.name = 'i2b2.2008-ncuiword' then 1 else 0 end) ncuiword
from feature_eval e inner join feature_rank r on e.feature_eval_id = r.feature_eval_id
where e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui')
and r.evaluation > 0.05
group by label;

select label,
  count(*) total,
  sum(case when e.name = 'i2b2.2008-cui' then 1 else 0 end) cui,
  sum(case when e.name = 'i2b2.2008-ncuiword' then 1 else 0 end) ncuiword
from feature_eval e inner join feature_rank r on e.feature_eval_id = r.feature_eval_id
where e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui')
and r.evaluation > 0.10
group by label;

select * 
from feature_eval e 
inner join feature_rank r on e.feature_eval_id = r.feature_eval_id
where e.label = 'Asthma' 
and r.evaluation > 0.10 
and e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui');

-- features without concepts 
select *
from
(
select feature_name, sum(case when sab in ('SNOMEDCT', 'RXNORM') then 1 else 0 end) sabc, count(*) allsab
from
(
select feature_name
from feature_eval e
inner join feature_rank r on e.feature_eval_id = r.feature_eval_id
where r.evaluation > 0.06
and e.name in ('i2b2.2008-ncuiword')
) feat
inner join umls_fword_lookup f on f.fword = feat.feature_name
inner join umls.mrconso mrc on f.cui = mrc.cui
group by feature_name
) s
where sabc = 0 and allsab = 0;


select d.document_id, k.uid, aw.span_begin, aw.span_end, substring(doc.docText, ase.span_begin, 100)
from document d
inner join anno_base ak on ak.document_id = d.document_id
inner join anno_dockey k on k.anno_base_id = ak.anno_base_id
inner join anno_base aw on aw.document_id = d.document_id
inner join anno_word_token w on w.anno_base_id = aw.anno_base_id and w.canonical_form = 'oxycodone'
inner join anno_contain ac on ac.child_anno_base_id = aw.anno_base_id and ac.parent_uima_type_id = 9
inner join anno_base ase on ac.parent_anno_base_id = ase.anno_base_id
inner join i2b2_2008_doc doc on doc.docId = k.uid
left join namedentity_to_word ntw on aw.anno_base_id = ntw.word_id
where ntw.named_entity_id is null
and d.analysis_batch = 'i2b2.2008'
and d.document_id = 21949;

-- why is oxycodone not annotated in following sentence:
-- She continues to complain of 8/10 pain even on her home regimen of oxycontin/oxycodone