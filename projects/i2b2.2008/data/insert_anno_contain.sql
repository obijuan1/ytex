insert into anno_contain (parent_anno_base_id, parent_uima_type_id, child_anno_base_id, child_uima_type_id)
select p.anno_base_id, p.uima_type_id, c.anno_base_id, c.uima_type_id
from document d 
inner join anno_base p
    on p.document_id = d.document_id 
    and d.analysis_batch = 'i2b2.2008'
inner join anno_base c
    on c.document_id = d.document_id 
    and p.span_begin <= c.span_begin
    and p.span_end >= c.span_end
    and p.anno_base_id <> c.anno_base_id
    and not (p.span_begin = c.span_begin and p.span_end = c.span_end and p.uima_type_id = c.uima_type_id)
;