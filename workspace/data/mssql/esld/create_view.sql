drop view $(db_schema).[v_all_documents]
;
GO

create view $(db_schema).[v_all_documents]
as
select
	r.UID, 
	e.studyid, 
	(select document_type_id from $(db_schema).ref_document_type where document_type_name = 'PROGRESS_NOTE') document_type_id, 
	r.[site] site_id,
	r.notes doc_text
from ALL_PROGNOTES_V r 
inner join esld_sample_v e on r.ssn = e.ssn
-- inner join $(db_schema).adjudication a on a.studyid = e.studyid
union
select
	r.UID, 
	e.studyid, 
	(select document_type_id from $(db_schema).ref_document_type where document_type_name = 'RADIOLOGY') document_type_id,
	'' site_id,
	r.note doc_text
from ALL_RADNOTES_V r 
inner join esld_sample_v e on r.ssn = e.ssn
-- inner join $(db_schema).adjudication a on a.studyid = e.studyid
union
select 
	r.UID, 
	e.studyid, 
	(select document_type_id from $(db_schema).ref_document_type where document_type_name = 'PATHOLOGY') document_type_id,
	r.[site] site_id,
	r.note doc_text
from ALL_PATHNOTES_V r 
inner join esld_sample_v e on r.ssn = e.ssn
-- inner join $(db_schema).adjudication a on a.studyid = e.studyid
;
GO



drop view $(db_schema).[V_ESLD_FWORD_LOOKUP];
go

create view $(db_schema).[V_ESLD_FWORD_LOOKUP]
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
or cui in (
	'C0079304', /* EGD */
	'C0521362', /* Gastrointestinal */
	'C0040405', /* CT */
	'C0041618', /* ultrasound */ 
	'C0205054' /* hepatic */
	)
or sourcetype = ('ESLD')
;
GO