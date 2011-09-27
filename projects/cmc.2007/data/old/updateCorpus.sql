delete from corpus_term
where corpus_id in (select corpus_id from corpus where corpus_name = 'cmc-ctakes');

delete from corpus
where corpus_name = 'cmc-ctakes';

insert into corpus (corpus_name)
values ('cmc-ctakes');

insert into corpus_term (corpus_id, concept_id, frequency)
select corpus_id, code, count(*)
from corpus,
anno_ontology_concept o
inner join anno_base b on b.anno_base_id = o.anno_base_id
inner join document d on d.document_id = b.document_id
where d.analysis_batch in ('cmc')
and corpus.corpus_name = 'cmc-ctakes'
group by corpus_id, code
;

-- mysql can't handle subqueries on the table that will be updated
-- use temp table to store frequency
create temporary table total_freq (total int);
insert into total_freq (total)
select sum(frequency) from corpus_term
;

update corpus_term
set frequency = frequency / (select total from total_freq);

drop table total_freq;
