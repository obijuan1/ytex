delete e,r,dl
from feature_eval e
left join feature_rank r on e.feature_eval_id = r.feature_eval_id
left join tfidf_doclength dl on dl.feature_eval_id = r.feature_eval_id
where corpus_name = 'i2b2.2008'
and featureset_name = 'cui'
and type = 'tfidf'
;

insert into feature_eval (corpus_name, featureset_name, type)
values ('i2b2.2008', 'cui', 'tfidf')
;

insert into feature_rank (feature_eval_id, feature_name, evaluation)
select fe.feature_eval_id, s.code, s.tf
from
(
	select feature_eval_id 
	from feature_eval 
	where corpus_name = 'i2b2.2008'
	and featureset_name = 'cui'
	and type = 'tfidf'
) fe,
(
  	select c.code, count(distinct d.instance_id) tf
	from document d
	inner join anno_base an on an.document_id = d.document_id
	inner join anno_ontology_concept c on c.anno_base_id = an.anno_base_id
	where analysis_batch = 'i2b2.2008'
	group by c.code
) s
;

insert into tfidf_doclength (feature_eval_id, instance_id, length)
select fe.feature_eval_id, s.instance_id, s.length
from
(
	select feature_eval_id 
	from feature_eval 
	where corpus_name = 'i2b2.2008'
	and featureset_name = 'cui'
	and type = 'tfidf'
) fe,
(
	select d.instance_id, count(*) length
	from document d
	inner join anno_base an on an.document_id = d.document_id
	inner join anno_ontology_concept c on c.anno_base_id = an.anno_base_id
	where analysis_batch = 'i2b2.2008'
	group by d.instance_id
) s
;

/*
delete from tfidf_doclength where name = 'i2b2.2008-cui';
delete from tfidf_docfreq where name = 'i2b2.2008-cui';
delete from tfidf_termfreq where name = 'i2b2.2008-cui';

insert into tfidf_doclength (name, instance_id, length)
select 'i2b2.2008-cui', s.*
from
(
	select d.uid, count(*)
	from document d
	inner join anno_base an on an.document_id = d.document_id
	inner join anno_ontology_concept c on c.anno_base_id = an.anno_base_id
	where analysis_batch = 'i2b2.2008'
	group by d.uid
) s;


insert into tfidf_docfreq (name, term, numdocs)
select 'i2b2.2008-cui', s.*
from
(
	select code, count(*)
	from
	(
	  	select distinct c.code, d.uid
		from document d
		inner join anno_base an on an.document_id = d.document_id
		inner join anno_ontology_concept c on c.anno_base_id = an.anno_base_id
		where analysis_batch = 'i2b2.2008'
		group by c.code, d.uid
	) s
	group by code
) s
;


insert into tfidf_termfreq (name, instance_id, term, freq)
select 'i2b2.2008-cui', s.*
from
(
	select d.uid, c.code, count(*)
	from document d
	inner join anno_base an on an.document_id = d.document_id
	inner join anno_ontology_concept c on c.anno_base_id = an.anno_base_id
	where analysis_batch = 'i2b2.2008'
	group by d.uid, c.code
) s
;
*/
