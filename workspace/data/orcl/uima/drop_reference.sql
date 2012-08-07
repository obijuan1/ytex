drop trigger trg_ref_named_entity_regex; 
drop trigger trg_ref_segment_regex; 

drop sequence named_entity_regex_id_sequence;
drop sequence segment_regex_id_sequence;
drop sequence hibernate_sequence;

drop index NK_ref_uima_type;
-- drop 'reference' data
drop table ref_uima_type;
drop table ref_named_entity_regex;
drop table ref_segment_regex;
drop table ref_stopword;
drop table hibernate_sequences;
drop table anno_base_sequence;
