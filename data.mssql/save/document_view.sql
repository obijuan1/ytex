drop view [ESLD].[v_all_documents]
;
GO

create view [ESLD].[v_all_documents]
as
select
	r.UID, 
	e.studyid, 
	(select document_type_id from esld.ref_document_type where document_type_name = 'PROGRESS_NOTE') document_type_id, 
	r.notes doc_text
from ALL_PROGNOTES_V r 
inner join esld_sample_v e on r.ssn = e.ssn
inner join esld.adjudication a on a.studyid = e.studyid
union
select
	r.UID, 
	e.studyid, 
	(select document_type_id from esld.ref_document_type where document_type_name = 'RADIOLOGY') document_type_id,
	r.note doc_text
from ALL_RADNOTES_V r 
inner join esld_sample_v e on r.ssn = e.ssn
inner join esld.adjudication a on a.studyid = e.studyid
union
select 
	r.UID, 
	e.studyid, 
	(select document_type_id from esld.ref_document_type where document_type_name = 'PATHOLOGY') document_type_id,
	r.note doc_text
from ALL_PATHNOTES_V r 
inner join esld_sample_v e on r.ssn = e.ssn
inner join esld.adjudication a on a.studyid = e.studyid
;
GO

drop view ESLD.V_DOCUMENT_CUI;
go

CREATE VIEW ESLD.V_DOCUMENT_CUI
AS
SELECT d.document_id, da.span_begin, da.span_end, ne.certainty, u.cui
FROM ESLD.document AS d INNER JOIN
ESLD.document_annotation AS da ON d.document_id = da.document_id INNER JOIN
ESLD.named_entity_annotation AS ne ON da.document_annotation_id = ne.document_annotation_id INNER JOIN
ESLD.ontology_concept_annotation AS o ON o.document_annotation_id = ne.document_annotation_id INNER JOIN
ESLD.umls_concept_annotation AS u ON u.ontology_concept_annotation_id = o.ontology_concept_annotation_id
;
go

drop view esld.v_document_cui_sent
;
go

create view esld.v_document_cui_sent
as
SELECT da.document_id, 
ne.certainty, 
o.code, 
substring(d.doc_text, da.span_begin+1, da.span_end-da.span_begin) cui_text, 
substring(d.doc_text, sentence.span_begin+1, sentence.span_end-sentence.span_begin) sentence_text
FROM 
ESLD.document_annotation AS da 
INNER JOIN ESLD.named_entity_annotation AS ne ON da.document_annotation_id = ne.document_annotation_id 
INNER JOIN ESLD.ontology_concept_annotation AS o ON o.document_annotation_id = ne.document_annotation_id 
INNER join esld.document_annotation as sentence on da.document_id = sentence.document_id
INNER JOIN esld.document AS d on da.document_id = d.document_id
where 
sentence.span_begin <= da.span_begin 
and sentence.span_end >= da.span_end
and sentence.uima_type_id in (select uima_type_id from esld.ref_uima_type t where t.uima_type_name = 'edu.mayo.bmi.uima.core.sentence.type.Sentence')
;
go

drop view esld.V_UMLS_FWORD_LOOKUP
;
go

create view esld.V_UMLS_FWORD_LOOKUP
as
select fword, cui, text
from esld.umls_ms_2009
where tui in 
(
'T021','T022','T023','T024','T025','T026','T029','T030','T031',
'T059','T060','T061',
'T019','T020','T037','T046','T047','T048','T049','T050','T190','T191',
'T033','T034','T040','T041','T042','T043','T044','T045','T046','T056','T057','T184'
)
and sourcetype = 'SNOMEDCT'
;
go


drop VIEW [ESLD].[V_DOCUMENT_ONTOANNO]
;
go

CREATE VIEW [ESLD].[V_DOCUMENT_ONTOANNO]
AS
SELECT d.document_id, da.span_begin, da.span_end, ne.certainty, o.coding_scheme, o.code
FROM ESLD.document AS d INNER JOIN
ESLD.document_annotation AS da ON d.document_id = da.document_id INNER JOIN
ESLD.named_entity_annotation AS ne ON da.document_annotation_id = ne.document_annotation_id INNER JOIN
ESLD.ontology_concept_annotation AS o ON o.document_annotation_id = ne.document_annotation_id
;
GO


drop view esld.v_document;
go

create  view esld.v_document
as
select 
	d.analysis_batch,
	d.study_id,
	d.document_id,
	d.uid,
	d.document_type_id,
	t.document_type_name,
	dt.doc_date,
	substring(d.doc_text, title.span_begin+1, title.span_end-title.span_begin) doc_title
from esld.document d
inner join esld.ref_document_type t on d.document_type_id = t.document_type_id
left join 
-- document date
	(
	select dt1.document_id, dt2.doc_date
	from esld.document_annotation dt1 
	inner join esld.docdate_annotation dt2 on dt1.document_annotation_id = dt2.document_annotation_id
	) dt on dt.document_id = d.document_id
left join 
-- doc title
	(
	select tda.document_id, tda.span_begin, tda.span_end
	from esld.document_annotation tda
	inner join esld.ref_uima_type u on tda.uima_type_id = u.uima_type_id 
	where u.uima_type_name = 'gov.va.vacs.esld.uima.types.DocumentTitle'
	) title on title.document_id = d.document_id
;
go