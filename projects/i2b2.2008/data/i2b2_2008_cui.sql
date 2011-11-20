-- throw out cuis that appear in almost every document
-- limit to certain semantic types
-- go from 12936 cuis to 8065 cuis

create table i2b2_2008_cui
as
select term cui
from tfidf_docfreq 
/* get cuis that are not too frequent */
where numdocs < 1000 
/* get cuis that have the right semantic types */
and exists (
    select * 
    from umls.MRSTY
    where cui = term
    and tui in
	(
	'T017' /* Anatomical Structure */,
	'T021','T022','T023','T024','T025','T026','T029','T030','T031',
	'T059','T060','T061',
	'T019','T020','T037','T046','T047','T048','T049','T050','T190','T191',
	'T033','T034','T040','T041','T042','T043','T044','T045','T046','T056','T057','T184','T121'
	)
)
and name = 'i2b2.2008-cui'
;

create unique index IX_cui on i2b2_2008_cui(cui);