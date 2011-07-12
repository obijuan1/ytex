delete from corpus_term
where corpus_id in (select corpus_id from corpus where corpus_name = 'cmc-suj');

delete from corpus
where corpus_name = 'cmc-suj';

insert into corpus (corpus_name)
values ('cmc-suj');

insert into corpus_term (corpus_id, concept_id, frequency)
select corpus_id, cui, c.cc
from corpus,
(
select cui, count(*) cc 
from suj_concept
group by cui
) c
where corpus.corpus_name = 'cmc-suj';

create temporary table total_freq (total int);
insert into total_freq (total)
select sum(frequency) 
from corpus_term t 
inner join corpus c on t.corpus_id = c.corpus_id
where c.corpus_name = 'cmc-suj'
;

update corpus_term t inner join corpus c on t.corpus_id = c.corpus_id
set frequency = frequency / (select total from total_freq)
where c.corpus_name = 'cmc-suj'
;

drop table total_freq;

