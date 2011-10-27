create view v_snomed_fword_lookup
as
select c.fword, mrc.cui, mrc.str text
from umls_aui_fword c
inner join @UMLS_SCHEMA@.MRCONSO mrc on c.aui = mrc.aui
where mrc.SAB in ( 'SNOMEDCT','RXNORM' )
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
