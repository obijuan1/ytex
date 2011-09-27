/* 
 * additional umls lexical variants / 
 * additional concepts
 */
delete from $(db_schema).umls_ms_2009 where sourcetype = 'ESLD'
insert into $(db_schema).umls_ms_2009 (  cui, fword, text, code, sourcetype, tui)
values ('C0994163','MRCP','MRCP','314635004','ESLD', 'T060');

insert into $(db_schema).umls_ms_2009 (  cui, fword, text, code, sourcetype, tui)
values ('C0439734','RUQ','RUQ','255497008','ESLD','T082');

insert into $(db_schema).umls_ms_2009 (  cui, fword, text, code, sourcetype, tui)
values ('C0024485','MAGNETIC','MAGNETIC IMAGE','113091000','ESLD','T060');

insert into $(db_schema).umls_ms_2009 (  cui, fword, text, code, sourcetype, tui)
values ('C0041618','echogram','echogram','C0041618','ESLD','T060');

insert into $(db_schema).umls_ms_2009 (  cui, fword, text, code, sourcetype, tui)
values ('ESLD_MASS','echogenic','echogenic focus','ESLD_MASS','ESLD','T060');


insert into $(db_schema).umls_ms_2009 (  cui, fword, text, code, sourcetype, tui)
values ('ESLD_MASS','low','low attenuation region','ESLD_MASS','ESLD','T060');


insert into $(db_schema).umls_ms_2009 (  cui, fword, text, code, sourcetype, tui)
values ('ESLD_MASS','hypodense','hypodense area','ESLD_MASS','ESLD','T060');

insert into $(db_schema).umls_ms_2009 (  cui, fword, text, code, sourcetype, tui)
values ('ESLD_MASS','decreased','decreased attenuation','ESLD_MASS','ESLD','T060');

insert into $(db_schema).umls_ms_2009 (  cui, fword, text, code, sourcetype, tui)
values ('ESLD_MASS','low-attenuated','low-attenuated area','ESLD_MASS','ESLD','T060');

insert into $(db_schema).umls_ms_2009 (  cui, fword, text, code, sourcetype, tui)
values ('C0227486','left','left lobe','ESLD_MASS','ESLD','T060');

insert into $(db_schema).umls_ms_2009 (  cui, fword, text, code, sourcetype, tui)
values ('C0227481','right','right lower lobe','ESLD_MASS','ESLD','T060');

insert into $(db_schema).umls_ms_2009 (  cui, fword, text, code, sourcetype, tui)
values ('C0227481','right','right lobe','ESLD_MASS','ESLD','T060');

/* 
 * ref_named_entity_regex
 */
insert into $(db_schema).ref_named_entity_regex (regex, coding_scheme, code, context)
values ('\(US\)','UMLS','C0041618','TITLE')
;

insert into $(db_schema).ref_named_entity_regex (regex, coding_scheme, code, context)
values ('\bCT\b','UMLS','C0040405','TITLE')
;

insert into $(db_schema).ref_named_entity_regex (regex, coding_scheme, code)
values ('(?i)\bSEE\s+.*#{0,1}+\s*\d+','ESLD','DOCREF')
;

insert into $(db_schema).ref_named_entity_regex (regex, coding_scheme, code)
values ('(?i)\bREFER\s+TO\s+.*#{0,1}+\s*\d+','ESLD','DOCREF')
;

insert into $(db_schema).ref_named_entity_regex (regex, coding_scheme, code)
values ('(?i)\bfluid\b[^\p{Punct}]*\bliver\b|\bfluid\b[^\p{Punct}]*\babdomen\b|\bfluid\b[^\p{Punct}]*abdominal\b|\bfluid\b[^\p{Punct}]*hepatic\b|abdominal\s+fluid','UMLS','C0401020')
;