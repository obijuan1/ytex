create index sourcetypeidx on umls_ms_2009 (sourcetype);

create index fwordidx on umls_ms_2009 (fword);

create index fwordidxci on umls_ms_2009 (nlssort(fword,'NLS_SORT=BINARY_CI'));

create index cuiidx on umls_ms_2009 (cui);

create index codemapidx on umls_snomed_map (code);

create index cuimapidx on umls_snomed_map (cui);
