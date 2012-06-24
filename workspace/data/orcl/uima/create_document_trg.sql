-- insert triggers to generate primary keys from sequence
create or replace trigger trg_anno_ontology_concept before insert on anno_ontology_concept
for each row
when (new.anno_ontology_concept_id is null)
begin
 select anno_onto_concept_id_sequence.nextval into :new.anno_ontology_concept_id from dual;
end;
/

create or replace trigger trg_anno_link before insert on anno_link
for each row
when (new.anno_link_id is null)
begin
 select anno_link_id_sequence.nextval into :new.anno_link_id from dual;
end;
/

create or replace trigger trg_fracture_demo before insert on fracture_demo
for each row
when (new.note_id is null)
begin
 select demo_note_id_sequence.nextval into :new.note_id from dual;
end;
/

create or replace trigger trg_anno_mm_cuiconcept before insert on anno_mm_cuiconcept
for each row
when (new.anno_mm_cuiconcept_id is null)
begin
 select anno_mm_cuiconcept_id_sequence.nextval into :new.anno_mm_cuiconcept_id from dual;
end;
/
