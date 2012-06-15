select mrc.cui, c.fword, c.fstem, c.tok_str, c.stem_str
into $(db_schema).v_snomed_fword_lookup
from $(db_schema).umls_aui_fword c
inner join $(umls_catalog).$(umls_schema).MRCONSO mrc 
	on c.aui = mrc.aui
	and mrc.sab in ( 'SNOMEDCT','RXNORM' )
inner join 
(
	select cui, min(tui) tui
	from $(umls_catalog).$(umls_schema).MRSTY sty
	where sty.tui in
	(
    /* diseasesAndDisordersTuis */
    'T019', 'T020', 'T037', 'T046', 'T047', 'T048', 'T049', 'T050', 
      'T190', 'T191', 'T033',
    /* signAndSymptomTuis */
    'T184',
    /* anatomicalSitesTuis */
    'T017', 'T029', 'T023', 'T030', 'T031', 'T022', 'T025', 'T026',
        'T018', 'T021', 'T024',
    /* medicationsAndDrugsTuis */
     'T116', 'T195', 'T123', 'T122', 'T118', 'T103', 'T120', 'T104',
        'T200', 'T111', 'T196', 'T126', 'T131', 'T125', 'T129', 'T130',
        'T197', 'T119', 'T124', 'T114', 'T109', 'T115', 'T121', 'T192',
        'T110', 'T127',
	/* proceduresTuis */
    'T060', 'T065', 'T058', 'T059', 'T063', 'T062', 'T061',
    /* deviceTuis */
    'T074', 'T075',
    /* laboratoryTuis */
    'T059'
	)
	group by cui
) t on t.cui = mrc.cui
;
create index IX_fword on test.v_snomed_fword_lookup(fword);
create index IX_fstem on test.v_snomed_fword_lookup(fstem);
