/**
 * mysql performance on views is abominable
 * use a table instead
 */
drop view if exists v_snomed_fword_lookup;
drop table if exists v_snomed_fword_lookup;

create table v_snomed_fword_lookup (
  fword varchar(100) not null,
  cui varchar(10) not null,
  text text not null
) engine=myisam, comment 'umls lookup table, created from umls_aui_fword and mrconso' ;

insert into v_snomed_fword_lookup
select c.fword, mrc.cui, mrc.str text
from umls_aui_fword c
inner join umls.MRCONSO mrc on c.aui = mrc.aui
where  mrc.SAB in ( 'SNOMEDCT' )
and exists
(
	select *
	from umls.MRSTY sty
	where mrc.cui = sty.cui
	and sty.tui in
	(
	'T021','T022','T023','T024','T025','T026','T029','T030','T031',
	'T059','T060','T061',
	'T019','T020','T037','T046','T047','T048','T049','T050','T190','T191',
	'T033','T034','T040','T041','T042','T043','T044','T045','T046','T056','T057','T184'
	)
)
;

create index idx_fword on v_snomed_fword_lookup (fword);
create index idx_cui on v_snomed_fword_lookup (cui);


/*
create table v_umls_fword_lookup (
);
as
select c.fword, mrc.cui, mrc.str text
from umls_aui_fword c
inner join umls.MRCONSO mrc on c.aui = mrc.aui
where  mrc.SAB in ( 'SNOMEDCT', 'RXNORM', 'NCI', 'ICD9CM', 'ICD10CM')
and exists
(
	select *
	from umls.MRSTY sty
	where mrc.cui = sty.cui
	and sty.tui in
	(
	'T021','T022','T023','T024','T025','T026','T029','T030','T031',
	'T059','T060','T061',
	'T019','T020','T037','T046','T047','T048','T049','T050','T190','T191',
	'T033','T034','T040','T041','T042','T043','T044','T045','T046','T056','T057','T184'
	)
)
;

drop table if exists umls_fword_lookup;
create table umls_fword_lookup select * from v_umls_fword_lookup;
*/