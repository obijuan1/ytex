-- **********
-- nlm wsd
-- **********
-- get all semantic types of target words
drop table if exists tmp_tui;
create temporary table tmp_tui
as
select distinct tui
from
(
    select st.tui
    from
    (
    select distinct cui from nlm_wsd_cui
    ) c
    inner join @UMLS_SCHEMA@.MRSTY st on c.cui = st.cui
    inner join @UMLS_SCHEMA@.SRDEF sr on sr.ui = st.tui 
    
    union
    
    /* the default semantic types */
    select ui
    from @UMLS_SCHEMA@.SRDEF 
    where ui in (
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
)s;

-- get all source vocabularies of target words
drop table if exists tmp_sab;
create temporary table tmp_sab
as
select distinct sab
from
(select distinct if(word = 'blood_pressure', 'blood pressure', word) word, cui from nlm_wsd_cui) m
inner join @UMLS_SCHEMA@.MRCONSO c on m.word = c.str and c.lat = 'ENG'
;

-- create lookup tble with just these source vocabs and semantic types
drop table if exists v_nlm_wsd_fword_lookup;

create table v_nlm_wsd_fword_lookup (
  cui char(8) not null,
  fword varchar(100) not null,
  tok_str varchar(250) not null
) engine=myisam, comment 'umls lookup table, created from umls_aui_fword and mrconso' ;

-- do the insert
insert into v_nlm_wsd_fword_lookup
select mrc.cui, c.fword, c.tok_str
from umls_aui_fword c
inner join @UMLS_SCHEMA@.MRCONSO mrc on c.aui = mrc.aui
inner join tmp_sab s on s.sab = mrc.sab
inner join
(
    select distinct cui 
    from @UMLS_SCHEMA@.MRSTY st
    inner join tmp_tui t on st.tui = t.tui
) c on c.cui = mrc.cui
;

create index IX_fword on v_nlm_wsd_fword_lookup(fword);

-- **********
-- msh wsd
-- **********
drop table if exists tmp_tui;
create temporary table tmp_tui
select distinct tui
from
(
    /* semantic types for cuis from msh wsd corpus */
    select st.tui
    from
    (
    select distinct cui from msh_wsd
    ) c
    inner join umls2011ab.MRSTY st on c.cui = st.cui
    
    union
    
    /* the default semantic types */
    select ui
    from umls2011ab.SRDEF 
    where ui in (
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
) s
;
drop table if exists tmp_sab;
create temporary table tmp_sab
select distinct sab
from
    (select distinct word, cui from msh_wsd) m
    inner join umls2011ab.MRCONSO c on m.word = c.str and m.cui = c.cui and c.lat = 'ENG'
;


drop table if exists v_msh_wsd_fword_lookup;

create table v_msh_wsd_fword_lookup (
  cui char(8) not null,
  fword varchar(100) not null,
  tok_str varchar(250) not null
) engine=myisam, comment 'umls lookup table, created from umls_aui_fword and mrconso' ;

-- do the insert
insert into v_nlm_wsd_fword_lookup
select mrc.cui, c.fword, c.tok_str
from umls_aui_fword c
inner join umls2011ab.MRCONSO mrc on c.aui = mrc.aui
inner join tmp_sab s on s.sab = mrc.sab
inner join
(
    select distinct cui 
    from umls2011ab.MRSTY st
    inner join tmp_tui t on st.tui = t.tui
) c on c.cui = mrc.cui
;
create index IX_fword on v_nlm_wsd_fword_lookup(fword);