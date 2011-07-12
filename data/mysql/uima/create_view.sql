create view v_annotation
AS
SELECT anno.*, ur.uima_type_name, substring(doc.doc_text, anno.span_begin+1, anno.span_end-anno.span_begin) anno_text, doc.analysis_batch
FROM anno_base AS anno 
INNER JOIN document AS doc ON doc.document_id = anno.document_id
INNER JOIN ref_uima_type AS ur on ur.uima_type_id = anno.uima_type_id
;


create view v_document_cui_sent
as
SELECT da.document_id, 
ne.certainty, 
o.code, 
substring(d.doc_text, da.span_begin+1, da.span_end-da.span_begin) cui_text, 
substring(d.doc_text, sentence.span_begin+1, sentence.span_end-sentence.span_begin) sentence_text,
d.analysis_batch
FROM 
anno_base AS da 
INNER JOIN anno_named_entity AS ne ON da.anno_base_id = ne.anno_base_id 
INNER JOIN anno_ontology_concept AS o ON o.anno_base_id = ne.anno_base_id 
INNER join anno_base as sentence on da.document_id = sentence.document_id
INNER JOIN document AS d on da.document_id = d.document_id
where 
sentence.span_begin <= da.span_begin 
and sentence.span_end >= da.span_end
and sentence.uima_type_id in (select uima_type_id from ref_uima_type t where t.uima_type_name = 'edu.mayo.bmi.uima.core.sentence.type.Sentence')
;

/*
 * view performance on mysql is abominable, use a table instead
create view v_snomed_fword_lookup
as
select fword, cui, text
from umls_ms_2009
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

 */

CREATE VIEW v_document_ontoanno
AS
SELECT d.document_id, da.span_begin, da.span_end, ne.certainty, o.coding_scheme, o.code, d.analysis_batch
FROM document AS d INNER JOIN
anno_base AS da ON d.document_id = da.document_id INNER JOIN
anno_named_entity AS ne ON da.anno_base_id = ne.anno_base_id INNER JOIN
anno_ontology_concept AS o ON o.anno_base_id = ne.anno_base_id
;



create view v_anno_segment
as
select da2.anno_base_id, s.segment_id
from anno_base da 
inner join anno_segment s on da.anno_base_id = s.anno_base_id
inner join anno_base da2 on da2.span_begin >= da.span_begin and da2.span_end <= da.span_end and da2.document_id = da.document_id
inner join ref_uima_type t on da2.uima_type_id = t.uima_type_id
where t.uima_type_name <> 'edu.mayo.bmi.uima.core.ae.type.Segment'
;

