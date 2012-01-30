-- insert the labels that the documents haven't been assigned to
-- assign them the class '2' instead of '1' for the labels that the documents have been assigned to

create temporary table tmp_cmc_label as
select distinct label from corpus_label where corpus_name = 'cmc.2007';

insert into corpus_label (corpus_name, instance_id, label, class)
select d.corpus_name, d.instance_id, tl.label, '2'
from tmp_cmc_label tl
inner join corpus_doc d 
left join corpus_label l 
    on d.corpus_name = l.corpus_name 
    and d.instance_id = l.instance_id
    and tl.label = l.label
where l.label is null
and d.corpus_name = 'cmc.2007';


-- add the section boundaries
delete from ref_segment_regex where segment_id in ('CMC_IMPRESSION', 'CMC_HISTORY');

insert into ref_segment_regex (regex, segment_id, limit_to_regex)
values ('<text.*CLINICAL_HISTORY">(.*)<\/text>', 'CMC_HISTORY', 1)
;

insert into ref_segment_regex (regex, segment_id, limit_to_regex)
values ('<text.*IMPRESSION">(.*)<\/text>', 'CMC_IMPRESSION',1)
;
