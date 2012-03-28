-- get the positive class fraction, export as properties.
-- the positive class fraction varies based on the zero vector cutoff.
-- parameterized by export.cutoff and kernel.hzv.experiment.
select cast(
    concat(
    'label', a.label, '_classY=', truncate(sum(a.class = 'Y')/count(*),3), 
    char(10),
    'label', a.label, '_classN=', truncate(sum(a.class = 'N')/count(*),3), 
    char(10),
    'label', a.label, '_classQ=', truncate(sum(a.class = 'Q')/count(*),3) 
) as char(200))
from corpus_doc d
inner join corpus_label a 
    on d.corpus_name = a.corpus_name
    and d.instance_id = a.instance_id
inner join hotspot_instance hi 
   	on hi.instance_id = a.instance_id
   	and hi.label = a.label
	and hi.corpus_name = a.corpus_name
	and hi.experiment = '@kernel.hzv.experiment@'	
    and hi.min_rank <= @export.cutoff@
where d.corpus_name = 'i2b2.2008'
    and d.doc_group = 'train'
group by a.label
;
