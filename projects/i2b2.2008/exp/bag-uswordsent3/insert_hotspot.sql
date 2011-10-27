/*
 * relies on bag-uswordsent/insert_hotspot.sql being executed
 */

delete i, s
from hotspot_instance i 
left join hotspot_sentence s on s.hotspot_instance_id = i.hotspot_instance_id
where i.experiment = 'bag-uswordsent3'
and i.corpus_name = 'i2b2.2008';


/*
* populate hotspot_instance
*/
insert into hotspot_instance (instance_id, label, corpus_name, experiment)
select d.docId, a.disease, 'i2b2.2008', 'bag-uswordsent3'
from i2b2_2008_doc d
inner join i2b2_2008_anno a 
    on a.docId = d.docId 
    and a.source = 'intuitive'
;

/*
* create temp table with the sentence numbers per hotspot instance and their evaluation
*/
drop table if exists tmp_sent;
create temporary table tmp_sent
as
select i3.hotspot_instance_id, b.document_id, sn.sentence_number, evaluation, rank
from hotspot_instance i0
inner join hotspot_instance i3 
    on i0.label = i3.label 
    and i0.instance_id = i3.instance_id
    and i3.experiment = 'bag-uswordsent3'
    and i3.corpus_name = 'i2b2.2008'
inner join hotspot_sentence s
    on s.hotspot_instance_id = i0.hotspot_instance_id
inner join anno_base b
    on b.anno_base_id = s.anno_base_id
inner join anno_sentence sn
    on sn.anno_base_id = s.anno_base_id
;

create unique index NK_tmp_sent on tmp_sent(hotspot_instance_id, document_id, sentence_number);


/*
* we want the bordering sentences - give the bordering sentences the max evaluation of the sentence that contains the hotspot
*/
insert into hotspot_sentence (hotspot_instance_id, anno_base_id, evaluation, rank)
select hotspot_instance_id, sn.anno_base_id, max(evaluation), min(rank)
from tmp_sent s
inner join anno_base b on b.document_id = s.document_id
inner join anno_sentence sn 
    on sn.anno_base_id = b.anno_base_id 
    and sn.sentence_number in (s.sentence_number - 1, s.sentence_number, s.sentence_number+1)
group by hotspot_instance_id, sn.anno_base_id
;