create view v_umls_fword_lookup
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
create index idx_fword on umls_fword_lookup (fword);
create index idx_cui on umls_fword_lookup (cui);
