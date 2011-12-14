use umls;
select distinct cui, str, r.rel
from mrconso m
inner join
(
select distinct cui1, r.REL
from mrrel r
where r.cui2 = 'C0678181' and rel in ('RN', 'CHD')
) r on m.cui = r.cui1
order by cui
;


/*
* zocor = C0678181
* lipitor = C0593906
* gemfibrozil = C0017245
* statins = C0360714
*/
/*
parents of zocor:
C0003277	Anticholesteremics
C0003367	antihyperlipemics
C0014432	Enzyme inhibitor
C0360714	Statin
*/

/*
* parents of gemfibrozil:
* C0003367	antihyperlipemics
* C0086440	Antilipemics
* C1516000	Antilipidemic Agent
*/

/*
* parents of zocor & gemfibrozil = C0003367	antihyperlipemics
*/
select distinct cui, str
from mrconso m
inner join
(
select distinct cui1
from mrrel r
where r.cui2 = 'C0678181' and rel in ('RN', 'CHD')
) z on m.CUI = z.cui1
inner join
(
select distinct cui1
from mrrel r
where r.cui2 = 'C0017245' and rel in ('RN', 'CHD')
) g on z.cui1 = g.cui1
;

create temporary table antilipid (cui char(8));
-- 119 concepts children of antihyperlipemics
insert into antilipid
select distinct cui2
from mrrel
where CUI1 = 'C0003367' and rel in ('RN', 'CHD');


create temporary table statin (cui char(8));
-- 25 concepts children of statins
insert into statin
select distinct cui2
from mrrel
where CUI1 = 'C0360714' and rel in ('RN', 'CHD');

use ytex;

select count(*) from ytex.document;

select * from i2b2_2008_disease;

select judgement, sum(case when cui = 0 then 1 else 0 end) present, sum(case when cui = 1 then 1 else 0 end) absent
from
(
select a.judgement, case when l.document_id is null then 0 else 1 end cui
from i2b2_2008_doc d
inner join i2b2_2008_anno a
  on a.docId = d.docId
  and a.source = 'intuitive'
  and a.disease = 'Hypercholesterolemia'
inner join anno_dockey k on a.docId = k.uid
inner join anno_base ak on ak.anno_base_id = k.anno_base_id
left join (
  select distinct document_id
  from anno_base ao
  inner join anno_ontology_concept c on c.anno_base_id = ao.anno_base_id
  inner join umls.antilipid l on l.cui = c.code
) l on l.document_id = ak.document_id
) s
group by judgement
;


select judgement, sum(case when cui = 0 then 1 else 0 end) present, sum(case when cui = 1 then 1 else 0 end) absent
from
(
select a.judgement, case when l.document_id is null then 0 else 1 end cui
from i2b2_2008_doc d
inner join i2b2_2008_anno a
  on a.docId = d.docId
  and a.source = 'intuitive'
  and a.disease = 'Hypercholesterolemia'
inner join anno_dockey k on a.docId = k.uid
inner join anno_base ak on ak.anno_base_id = k.anno_base_id
left join (
  select distinct document_id
  from anno_base ao
  inner join anno_ontology_concept c on c.anno_base_id = ao.anno_base_id
  inner join umls.statin l on l.cui = c.code
) l on l.document_id = ak.document_id
) s
group by judgement;

select judgement, sum(case when cui = 0 then 1 else 0 end) present, sum(case when cui = 1 then 1 else 0 end) absent
from
(
select a.judgement, case when l.document_id is null then 0 else 1 end cui
from i2b2_2008_doc d
inner join i2b2_2008_anno a
  on a.docId = d.docId
  and a.source = 'intuitive'
  and a.disease = 'Hypertriglyceridemia'
inner join anno_dockey k on a.docId = k.uid
inner join anno_base ak on ak.anno_base_id = k.anno_base_id
left join (
  select distinct document_id
  from anno_base ao
  inner join anno_ontology_concept c on c.anno_base_id = ao.anno_base_id
  inner join umls.statin l on l.cui = c.code
) l on l.document_id = ak.document_id
) s
group by judgement
;

select judgement, sum(case when cui = 0 then 1 else 0 end) present, sum(case when cui = 1 then 1 else 0 end) absent
from
(
select a.judgement, case when l.document_id is null then 0 else 1 end cui
from i2b2_2008_doc d
inner join i2b2_2008_anno a
  on a.docId = d.docId
  and a.source = 'intuitive'
  and a.disease = 'Hypertriglyceridemia'
inner join anno_dockey k on a.docId = k.uid
inner join anno_base ak on ak.anno_base_id = k.anno_base_id
left join (
  select distinct document_id
  from anno_base ao
  inner join anno_ontology_concept c on c.anno_base_id = ao.anno_base_id
  inner join umls.antilipid l on l.cui = c.code
) l on l.document_id = ak.document_id
) s
group by judgement
;




select judgement, sum(case when cui = 0 then 1 else 0 end) present, sum(case when cui = 1 then 1 else 0 end) absent
from
(
select a.judgement, case when l.document_id is null then 0 else 1 end cui
from i2b2_2008_doc d
inner join i2b2_2008_anno a
  on a.docId = d.docId
  and a.source = 'intuitive'
  and a.disease = 'Hypertriglyceridemia'
inner join anno_dockey k on a.docId = k.uid
inner join anno_base ak on ak.anno_base_id = k.anno_base_id
left join (
  select distinct document_id
  from anno_base ao
  inner join anno_ontology_concept c on c.anno_base_id = ao.anno_base_id
  where c.code ='C0678181'
) l on l.document_id = ak.document_id
) s
group by judgement
