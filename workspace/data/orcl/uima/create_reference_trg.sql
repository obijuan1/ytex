-- insert triggers to generate primary keys from sequence

create or replace trigger trg_ref_named_entity_regex before insert on ref_named_entity_regex
for each row
when (new.named_entity_regex_id is null)
begin
 select named_entity_regex_id_sequence.nextval into :new.named_entity_regex_id from dual;
end;
/

create or replace trigger trg_ref_segment_regex before insert on ref_segment_regex
for each row
when (new.segment_regex_id is null)
begin
 select segment_regex_id_sequence.nextval into :new.segment_regex_id from dual;
end;
/