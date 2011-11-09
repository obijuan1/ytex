drop sequence document_id_sequence;
drop sequence anno_base_id_sequence;
drop sequence anno_onto_concept_id_sequence;
drop sequence anno_contain_id_sequence;
drop sequence demo_note_id_sequence;

drop index IX_anno_contain_c;
drop index IX_anno_contain_p;
drop index NK_anno_contain;
drop index IX_ontology_concept_code;
drop index IX_umls_concept_cui;
drop index IX_uid;
drop index IX_document_analysis_batch;
drop INDEX IX_docanno_doc;
drop INDEX IX_covered_text;
drop index IX_segment_anno_seg;


-- drop 'operational' data
drop table fracture_demo;
drop table anno_contain;
drop table anno_source_doc_info;
drop table anno_num_token;
drop table anno_word_token;
drop table anno_base_token;
drop table anno_segment;
drop table anno_umls_concept;
drop table anno_ontology_concept;
drop table anno_named_entity;
drop table anno_sentence;
drop table anno_date;
drop table anno_base;
drop table document;

