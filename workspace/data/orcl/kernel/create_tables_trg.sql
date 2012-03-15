-- insert triggers to generate primary keys from sequence
create trigger trg_feature_eval before insert on feature_eval
for each row
when (new.feature_eval_id is null)
begin
 select feature_eval_sequence.nextval into :new.feature_eval_id from dual;
end;
/


create trigger trg_feature_rank before insert on feature_rank
for each row
when (new.feature_rank_id is null)
begin
 select feature_rank_sequence.nextval into :new.feature_rank_id from dual;
end;
/

create trigger trg_feature_parchd before insert on feature_parchd
for each row
when (new.feature_parchd_id is null)
begin
 select feature_parchd_sequence.nextval into :new.feature_parchd_id from dual;
end;
/
