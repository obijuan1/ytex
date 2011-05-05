/* duplicate corpus for umls rb + par concept hierarchy */
delete from corpus_term
where corpus_id in (select corpus_id from corpus where corpus_name = 'i2b2.2008-rb');

delete from corpus
where corpus_name = 'i2b2.2008-rb';

insert into corpus (corpus_name)
values ('i2b2.2008-rb');

insert into corpus_term (corpus_id, concept_id, frequency)
select corpus_id, code, count(*)
from corpus,
anno_ontology_concept o
inner join anno_base b on b.anno_base_id = o.anno_base_id
inner join document d on d.document_id = b.document_id
where d.analysis_batch in ('i2b2.2008')
and corpus.corpus_name = 'i2b2.2008-rb'
group by corpus_id, code
;

-- mysql can't handle subqueries on the table that will be updated
-- use temp table to store frequency
drop table if exists total_freq;
create temporary table total_freq (total int);

insert into total_freq (total)
select sum(frequency)
from corpus_term t
inner join corpus c on t.corpus_id = c.corpus_id
where corpus_name = 'i2b2.2008-rb'
;

update corpus_term t
inner join corpus c on t.corpus_id = c.corpus_id
set frequency = frequency / (select total from total_freq)
where corpus_name = 'i2b2.2008-rb'
;
