/**
 * mysql performance on views is abominable
 * use a table instead
 * replace @UMLS_SCHEMA@ with the appropriate value
 */
drop view if exists v_snomed_fword_lookup;
drop table if exists v_snomed_fword_lookup;

create table v_snomed_fword_lookup (
  cui varchar(10) not null,
  fword varchar(100) not null,
  fstem varchar(100) not null,
  tok_str varchar(250) not null,
  stem_str varchar(250) not null
) engine=myisam, comment 'umls lookup table, created from umls_aui_fword and mrconso' ;

insert into v_snomed_fword_lookup
select mrc.cui, c.fword, c.fstem, c.tok_str, c.stem_str
from umls_aui_fword c
inner join @UMLS_SCHEMA@.MRCONSO mrc on c.aui = mrc.aui
where  mrc.SAB in ( 'SNOMEDCT', 'RXNORM')
and exists
(
	select *
	from @UMLS_SCHEMA@.MRSTY sty
	where mrc.cui = sty.cui
	and sty.tui in
	(
	'T021','T022','T023','T024','T025','T026','T029','T030','T031',
	'T059','T060','T061',
	'T019','T020','T037','T046','T047','T048','T049','T050','T190','T191',
	'T033','T034','T040','T041','T042','T043','T044','T045','T046','T056','T057','T184','T121'
	)
)
;

create index idx_fword on v_snomed_fword_lookup (fword);
create index idx_fstem on v_snomed_fword_lookup (fstem);
create index idx_cui on v_snomed_fword_lookup (cui);
