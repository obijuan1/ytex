-- get the words for which we have the matching umls strings

insert into nlm_wsd_word
select s.word
from
(
select c.word, count(distinct c.choice_code) wc
from nlm_wsd_cui c
inner join
    (
    select distinct word, choice_code
    from nlm_wsd
    ) n on c.word = n.word and c.choice_code = n.choice_code
inner join @UMLS_SCHEMA@.MRCONSO mc on mc.cui = c.cui and mc.str = c.word
group by c.word
) s
inner join
(
select c.word, count(distinct c.choice_code) wc
from nlm_wsd_cui c
inner join
    (
    select distinct word, choice_code
    from nlm_wsd
    ) n on c.word = n.word and c.choice_code = n.choice_code
group by c.word
) c on s.word = c.word and s.wc = c.wc
order by c.word
;

insert into nlm_wsd_word (word) values ('blood_pressure');