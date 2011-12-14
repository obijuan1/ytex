drop table if exists tmp_all_concepts;
create temporary table tmp_all_concepts
as 
	select distinct code
	from anno_ontology_concept o
	inner join anno_base b on b.anno_base_id = o.anno_base_id
	inner join document d on d.document_id = b.document_id
	where d.analysis_batch in ('i2b2.2008')
;
create unique index NK_concept on tmp_all_concepts(code);

drop table if exists tmp_all_diseasecuis;
create temporary table tmp_all_diseasecuis
as
select distinct d.disease, m.cui
from umls.MRCONSO m
inner join i2b2_2008_disease d on d.disease = m.str and d.disease not in ('CAD', 'PVD', 'OA')
;
create unique index IX_discui on tmp_all_diseasecuis(disease, cui);

insert into tmp_all_diseasecuis (disease, cui)
select distinct 'PVD', m.cui 
from umls.MRCONSO m
where str = 'Peripheral vascular disease'
;

insert into tmp_all_diseasecuis (disease, cui)
select distinct 'OA', m.cui 
from umls.MRCONSO m
where str = 'Osteoarthritis'


insert into tmp_all_diseasecuis (disease, cui)
select distinct 'CAD', m.cui 
from umls.MRCONSO m
where str = 'Coronary Artery Disease'
;

delete fe, r
from feature_eval fe 
left join feature_rank r on fe.feature_eval_id = r.feature_eval_id
where fe.type = 'mrrel' 
and fe.corpus_name = 'i2b2.2008'
;

insert into feature_eval (corpus_name, label, type)
select 'i2b2.2008', disease, 'mrrel'
from i2b2_2008_disease
;

insert into feature_rank (feature_eval_id, feature_name, evaluation, rank)
select *, 1, 1
from
(
    select distinct fe.feature_eval_id, r.cui1
    from umls.MRREL r
    inner join tmp_all_diseasecuis c on r.cui2 = c.cui
    /* limit to concepts that appear in the corpus */
    inner join tmp_all_concepts dc on dc.code = r.cui1
    inner join feature_eval fe 
        on fe.label = c.disease 
        and fe.type = 'mrrel' 
        and fe.corpus_name = 'i2b2.2008'
    where r.rela in ('may_be_prevented_by', 'may_be_treated_by', 'inverse_isa')
) s
;

/* see what concepts we pulled */
select label, feature_name, min(str) 
from feature_eval fe
inner join feature_rank r on fe.feature_eval_id = r.feature_eval_id
left join umls.MRCONSO c on c.cui = r.feature_name and c.tty in ('PT', 'PN') and lat = 'ENG'
where fe.type = 'mrrel' 
and fe.corpus_name = 'i2b2.2008'
group by label, feature_name
order by label, feature_name
;