delete from esld.document
where analysis_batch='$(analysis_batch)'
;
/*
delete from esld.document_class
where document_id in (select document_id from esld.document where analysis_batch='$(analysis_batch)')
;

delete from esld.segment_annotation
where document_annotation_id in 
	(
	select document_annotation_id 
	from esld.document d 
	inner join esld.document_annotation da on d.document_id = da.document_id 
	where analysis_batch='$(analysis_batch)'
	)
;

delete from esld.umls_concept_annotation
where ontology_concept_annotation_id in 
	(
	select ontology_concept_annotation_id 
	from esld.document d 
	inner join esld.document_annotation da on d.document_id = da.document_id 
	inner join esld.named_entity_annotation ne on ne.document_annotation_id = da.document_annotation_id
	inner join esld.ontology_concept_annotation o on o.document_annotation_id = da.document_annotation_id
	where analysis_batch='$(analysis_batch)'
	)
;

delete from esld.ontology_concept_annotation
where ontology_concept_annotation_id in 
	(
	select ontology_concept_annotation_id 
	from esld.document d 
	inner join esld.document_annotation da on d.document_id = da.document_id 
	inner join esld.named_entity_annotation ne on ne.document_annotation_id = da.document_annotation_id
	where analysis_batch='$(analysis_batch)'
	)
;

delete from esld.named_entity_annotation
where document_annotation_id in 
	(
	select ne.document_annotation_id 
	from esld.document d 
	inner join esld.document_annotation da on d.document_id = da.document_id 
	inner join esld.named_entity_annotation ne on ne.document_annotation_id = da.document_annotation_id
	where analysis_batch='$(analysis_batch)'
	)
;
delete from esld.sentence_annotation
where document_annotation_id in 
	(
	select ne.document_annotation_id 
	from esld.document d 
	inner join esld.document_annotation da on d.document_id = da.document_id 
	inner join esld.sentence_annotation ne on ne.document_annotation_id = da.document_annotation_id
	where analysis_batch='$(analysis_batch)'
	)
;
delete from esld.docdate_annotation
where document_annotation_id in 
	(
	select ne.document_annotation_id 
	from esld.document d 
	inner join esld.document_annotation da on d.document_id = da.document_id 
	inner join esld.docdate_annotation ne on ne.document_annotation_id = da.document_annotation_id
	where analysis_batch='$(analysis_batch)'
	)
;
delete from esld.document_annotation
where document_annotation_id in 
	(
	select da.document_annotation_id 
	from esld.document d 
	inner join esld.document_annotation da on d.document_id = da.document_id 
	where analysis_batch='$(analysis_batch)'
	)
;
*/
