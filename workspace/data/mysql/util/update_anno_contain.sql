-- Fill in the anno_contain table with all containment relationships

-- clear out existing relationships
delete ac
from anno_contain ac
inner join anno_base ab on ac.parent_anno_base_id = ab.anno_base_id
inner join document d on d.document_id = ab.document_id
where d.analysis_batch = 'i2b2.2008'
;

-- insert new relationships
insert into anno_contain (parent_anno_base_id, parent_uima_type_id, child_anno_base_id, child_uima_type_id)
select p.anno_base_id, p.uima_type_id, c.anno_base_id, c.uima_type_id
from document d
inner join anno_base c on c.document_id = d.document_id
inner join anno_base p
	on p.document_id = d.document_id
	and p.span_begin <= c.span_begin
	and p.span_end >= c.span_end
where analysis_batch = 'i2b2.2008'
-- prevent trivial containment - same type, same span 
and p.span_begin <> c.span_begin
and p.span_end <> c.span_end
and p.uima_type_id <> c.uima_type_id
and p.span_begin is not null
and c.span_begin is not null
;