/*
 * update the abs_radiology_review table with 'false positives'
 * manually review these to see if these are indeed false positives
 */
insert into esld.abs_radiology_review (uid, studyid, rad_procedure_type_id)
select d.uid, d.study_id, c.class_auto
from esld.document_class c
inner join esld.v_document d on c.document_id = d.document_id
left join esld.abs_radiology_review r on r.uid = d.uid and r.studyid = d.study_id
where c.class_gold = 0
and c.class_auto <> c.class_gold
and c.task = 'RADIOLOGY_TYPE'
and d.analysis_batch = '$(analysis_batch)'
and r.id is null
;