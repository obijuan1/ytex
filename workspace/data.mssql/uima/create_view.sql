create view $(db_schema).[V_DOCUMENT_ANNOTATION]
AS
SELECT anno.*, ur.uima_type_name, substring(doc.doc_text, anno.span_begin+1, anno.span_end-anno.span_begin) anno_text, doc.analysis_batch
FROM $(db_schema).document_annotation AS anno 
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
$(db_schema).document_annotation AS da 
INNER JOIN $(db_schema).named_entity_annotation AS ne ON da.document_annotation_id = ne.document_annotation_id 
INNER JOIN $(db_schema).ontology_concept_annotation AS o ON o.document_annotation_id = ne.document_annotation_id 
INNER join $(db_schema).document_annotation as sentence on da.document_id = sentence.document_id
INNER JOIN $(db_schema).document AS d on da.document_id = d.document_id
where 
sentence.span_begin <= da.span_begin 
and sentence.span_end >= da.span_end
and sentence.uima_type_id in (select uima_type_id from $(db_schema).ref_uima_type t where t.uima_type_name = 'edu.mayo.bmi.uima.core.sentence.type.Sentence')
;
go

create view $(db_schema).[V_SNOMED_FWORD_LOOKUP]
as
select fword, cui, text
from $(db_schema).umls_ms_2009
where 
(
	tui in 
	(
	'T021','T022','T023','T024','T025','T026','T029','T030','T031',
	'T059','T060','T061',
	'T019','T020','T037','T046','T047','T048','T049','T050','T190','T191',
	'T033','T034','T040','T041','T042','T043','T044','T045','T046','T056','T057','T184'
	)
	and sourcetype = 'SNOMEDCT'
) 
;
GO

CREATE VIEW $(db_schema).[V_DOCUMENT_ONTOANNO]
AS
SELECT d.document_id, da.span_begin, da.span_end, ne.certainty, o.coding_scheme, o.code, d.analysis_batch
FROM $(db_schema).document AS d INNER JOIN
$(db_schema).document_annotation AS da ON d.document_id = da.document_id INNER JOIN
$(db_schema).named_entity_annotation AS ne ON da.document_annotation_id = ne.document_annotation_id INNER JOIN
$(db_schema).ontology_concept_annotation AS o ON o.document_annotation_id = ne.document_annotation_id
;
GO


create view $(db_schema).v_anno_segment
as
select da2.document_annotation_id, s.segment_id
from $(db_schema).document_annotation da 
inner join $(db_schema).segment_annotation s on da.document_annotation_id = s.document_annotation_id
inner join $(db_schema).document_annotation da2 on da2.span_begin >= da.span_begin and da2.span_end <= da.span_end and da2.document_id = da.document_id
inner join $(db_schema).ref_uima_type t on da2.uima_type_id = t.uima_type_id
where t.uima_type_name <> 'edu.mayo.bmi.uima.core.ae.type.Segment'
;
go
