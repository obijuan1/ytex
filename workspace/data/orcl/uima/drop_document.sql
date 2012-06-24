drop trigger trg_anno_mm_cuiconcept;
drop trigger trg_fracture_demo;
drop trigger trg_anno_link;
drop trigger trg_anno_ontology_concept;

-- legacy
drop sequence document_id_sequence;
drop sequence anno_base_id_sequence;
-- end legacy
drop sequence anno_onto_concept_id_sequence;
drop sequence anno_link_id_sequence;
drop sequence anno_contain_id_sequence;
drop sequence demo_note_id_sequence;
drop sequence anno_mm_cuiconcept_id_sequence;


drop index IX_anno_contain_c;
drop index IX_anno_contain_p;
drop index IX_ontology_concept_code;
drop index IX_umls_concept_cui;
drop index IX_instance_id;
drop index IX_instance_key;
drop index IX_document_analysis_batch;
drop INDEX IX_docanno_doc;
drop INDEX IX_covered_text;
drop index IX_segment_anno_seg;
drop index IX_link;

-- drop 'operational' data
drop table fracture_demo;
drop table anno_contain;
drop table anno_link;
-- legacy
drop table anno_source_doc_info;
drop table anno_num_token;
drop table anno_word_token;
drop table anno_base_token;
drop table anno_umls_concept;
drop table anno_segment;
-- end legacy
drop table anno_mm_cuiconcept;
drop table anno_mm_candidate;
drop table anno_mm_acronym;
drop table anno_mm_utterance;
drop table anno_mm_negation;
drop table anno_token;
drop table anno_markable;
drop table anno_treebank_node;
drop table anno_drug_mention;
drop table anno_ontology_concept;
drop table anno_named_entity;
drop table anno_med_event;
drop table anno_sentence;
drop table anno_date;
drop table anno_base;
drop table document;

