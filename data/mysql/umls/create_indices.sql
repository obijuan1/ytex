/*
create index sourcetypeidx on umls_ms_2009 (sourcetype);

create index fwordidx on umls_ms_2009 (fword);

create index cuiidx on umls_ms_2009 (cui);


create index codemapidx on umls_snomed_map (code);

create index cuimapidx on umls_snomed_map (cui);

alter table umls.mrsty add primary key(cui, tui);
create index cuiidx on umls.mrsty (cui);
create index tuiidx on umls.mrsty (tui);
*/

create index fwordidx on umls_aui_fword (fword);
