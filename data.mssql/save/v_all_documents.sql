drop view [ESLD].[v_all_documents];
go

create view [ESLD].[v_all_documents]
as
select
	r.UID, 
	e.studyid, 
	(select document_type_id from esld.ref_document_type where document_type_name = 'PROGRESS_NOTE') document_type_id, 
	r.notes doc_text
from ALL_PROGNOTES_V r 
inner join esld_sample_v e on r.ssn = e.ssn
-- inner join esld.adjudication a on a.studyid = e.studyid
union
select
	r.UID, 
	e.studyid, 
	(select document_type_id from esld.ref_document_type where document_type_name = 'RADIOLOGY') document_type_id,
	r.note doc_text
from ALL_RADNOTES_V r 
inner join esld_sample_v e on r.ssn = e.ssn
-- inner join esld.adjudication a on a.studyid = e.studyid
union
select 
	r.UID, 
	e.studyid, 
	(select document_type_id from esld.ref_document_type where document_type_name = 'PATHOLOGY') document_type_id,
	r.note doc_text
from ALL_PATHNOTES_V r 
inner join esld_sample_v e on r.ssn = e.ssn
-- inner join esld.adjudication a on a.studyid = e.studyid
;
go