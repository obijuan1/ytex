create index sourcetypeidx on $(db_schema).umls_ms_2009 (sourcetype);

create index fwordidx on $(db_schema).umls_ms_2009 (fword);

create index cuiidx on $(db_schema).umls_ms_2009 (cui);

create index codemapidx on $(db_schema).umls_snomed_map (code);

create index cuimapidx on $(db_schema).umls_snomed_map (cui);
