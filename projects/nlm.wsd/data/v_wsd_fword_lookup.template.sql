drop table if exists v_wsd_fword_lookup;

create table v_wsd_fword_lookup (
  cui char(8) not null,
  fword varchar(100) not null,
  fstem varchar(100) not null,
  tok_str varchar(250) not null,
  stem_str varchar(250) not null
) engine=myisam, comment 'umls lookup table, created from umls_aui_fword and mrconso' ;

-- get the tuis we want
-- these are the 'defaults'
create temporary table tmp_tui
as
select ui from @UMLS_SCHEMA@.SRDEF where ui in 
  (
	'T017' /* Anatomical Structure */,
	'T021','T022','T023','T024','T025','T026','T029','T030','T031',
	'T059','T060','T061',
	'T019','T020','T037','T046','T047','T048','T049','T050','T190','T191',
	'T033','T034','T040','T041','T042','T043','T044','T045','T046','T056','T057','T184','T121'
  )
;
-- get additional tuis from the concepts for wsd
insert into tmp_tui
select tui
from @UMLS_SCHEMA@.MRSTY sty
inner join nlm_wsd_cui c on c.cui = sty.cui
inner join nlm_wsd_word w on w.word = c.word
;

-- do the insert
insert into v_wsd_fword_lookup
select mrc.cui, c.fword, c.fstem, c.tok_str, c.stem_str
from umls_aui_fword c
inner join @UMLS_SCHEMA@.MRCONSO mrc on c.aui = mrc.aui
where  mrc.SAB in ('SNOMEDCT', 'MSH', 'MEDCIN', 'LNC', 'MTH', 'CSP', 'AOD')
and exists
(
    select *
    from @UMLS_SCHEMA@.MRSTY sty
    inner join (select distinct ui from tmp_tui) tt on sty.tui = tt.ui
    where sty.cui = mrc.cui
)
;

-- create indices
create index idx_fword on v_wsd_fword_lookup (fword);
create index idx_fstem on v_wsd_fword_lookup (fstem);
