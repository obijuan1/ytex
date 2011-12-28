-- insert triggers to generate primary keys from sequence
create trigger trg_document before insert on document
for each row
when (new.document_id is null)
begin
 select document_id_sequence.nextval into :new.document_id from dual;
end;
/

create trigger trg_anno_base before insert on anno_base
for each row
when (new.anno_base_id is null)
begin
 select anno_base_id_sequence.nextval into :new.anno_base_id from dual;
end;
/

create trigger trg_anno_ontology_concept before insert on anno_ontology_concept
for each row
when (new.anno_ontology_concept_id is null)
begin
 select anno_onto_concept_id_sequence.nextval into :new.anno_ontology_concept_id from dual;
end;
/

create trigger trg_fracture_demo before insert on fracture_demo
for each row
when (new.note_id is null)
begin
 select demo_note_id_sequence.nextval into :new.note_id from dual;
end;
/
