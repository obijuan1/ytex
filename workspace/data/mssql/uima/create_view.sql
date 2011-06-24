create view $(db_schema).[v_annotation]
AS
SELECT anno.*, ur.uima_type_name, substring(doc.doc_text, anno.span_begin+1, anno.span_end-anno.span_begin) anno_text, doc.analysis_batch
FROM $(db_schema).anno_base AS anno 
INNER JOIN $(db_schema).document AS doc ON doc.document_id = anno.document_id
INNER JOIN $(db_schema).REF_UIMA_TYPE AS ur on ur.uima_type_id = anno.uima_type_id
;
GO

create view $(db_schema).v_document_cui_sent
as
SELECT da.document_id, 
ne.certainty, 
o.code, 
substring(d.doc_text, da.span_begin+1, da.span_end-da.span_begin) cui_text, 
substring(d.doc_text, sentence.span_begin+1, sentence.span_end-sentence.span_begin) sentence_text,
d.analysis_batch
FROM 
$(db_schema).anno_base AS da 
INNER JOIN $(db_schema).anno_named_entity AS ne ON da.anno_base_id = ne.anno_base_id 
INNER JOIN $(db_schema).anno_ontology_concept AS o ON o.anno_base_id = ne.anno_base_id 
INNER join $(db_schema).anno_base as sentence on da.document_id = sentence.document_id
INNER JOIN $(db_schema).document AS d on da.document_id = d.document_id
where 
sentence.span_begin <= da.span_begin 
and sentence.span_end >= da.span_end
and sentence.uima_type_id in (select uima_type_id from $(db_schema).ref_uima_type t where t.uima_type_name = 'edu.mayo.bmi.uima.core.sentence.type.Sentence')
;
go

CREATE VIEW $(db_schema).[v_document_ontoanno]
AS
SELECT d.document_id, da.span_begin, da.span_end, ne.certainty, o.coding_scheme, o.code, d.analysis_batch
FROM $(db_schema).document AS d INNER JOIN
$(db_schema).anno_base AS da ON d.document_id = da.document_id INNER JOIN
$(db_schema).anno_named_entity AS ne ON da.anno_base_id = ne.anno_base_id INNER JOIN
$(db_schema).anno_ontology_concept AS o ON o.anno_base_id = ne.anno_base_id
;
GO


create view $(db_schema).v_anno_segment
as
select da2.anno_base_id, s.segment_id
from $(db_schema).anno_base da 
inner join $(db_schema).anno_segment s on da.anno_base_id = s.anno_base_id
inner join $(db_schema).anno_base da2 on da2.span_begin >= da.span_begin and da2.span_end <= da.span_end and da2.document_id = da.document_id
inner join $(db_schema).ref_uima_type t on da2.uima_type_id = t.uima_type_id
where t.uima_type_name <> 'edu.mayo.bmi.uima.core.ae.type.Segment'
;
go
