drop table if exists v_wsd_fword_lookup;

create table v_wsd_fword_lookup (
  cui char(8) not null,
  fword varchar(100) not null,
  fstem varchar(100) not null,
  tok_str varchar(250) not null,
  stem_str varchar(250) not null
) engine=myisam, comment 'umls lookup table, created from umls_aui_fword and mrconso' ;

-- do the insert
-- maybe add T167
insert into v_wsd_fword_lookup
select mrc.cui, c.fword, c.fstem, c.tok_str, c.stem_str
from umls_aui_fword c
inner join @UMLS_SCHEMA@.MRCONSO mrc on c.aui = mrc.aui
inner join
(
	select distinct cui 
	from @UMLS_SCHEMA@.MRSTY 
	where tui in
	(
	'T015'
	,'T016'
	,'T017'
	,'T019'
	,'T020'
	,'T021'
	,'T022'
	,'T023'
	,'T024'
	,'T025'
	,'T026'
	,'T029'
	,'T030'
	,'T031'
	,'T032'
	,'T033'
	,'T034'
	,'T037'
	,'T038'
	,'T039'
	,'T040'
	,'T041'
	,'T042'
	,'T043'
	,'T044'
	,'T045'
	,'T046'
	,'T047'
	,'T048'
	,'T049'
	,'T050'
	,'T054'
	,'T055'
	,'T056'
	,'T057'
	,'T058'
	,'T059'
	,'T060'
	,'T061'
	,'T064'
	,'T067'
	,'T070'
	,'T074'
	,'T078'
	,'T079'
	,'T080'
	,'T081'
	,'T082'
	,'T091'
	,'T098'
	,'T118'
	,'T119'
	,'T121'
	,'T123'
	,'T131'
	,'T167'
	,'T169'
	,'T170'
	,'T171'
	,'T184'
	,'T190'
	,'T191'
	,'T196'
	)
) t on mrc.cui = t.cui
where  mrc.SAB in ('SNOMEDCT', 'MSH', 'MEDCIN', 'LNC', 'MTH', 'CSP', 'AOD')
;

-- create indices
create index idx_fword on v_wsd_fword_lookup (fword);
