-- msh wsd semantic type abbreviations
select group_concat(abr order by abr)
from
(
    select distinct abr
    from
    (
        /* semantic types for cuis from msh wsd corpus */
        select sr.abr
        from
        (
        select distinct cui from nlm_wsd_cui
        ) c
        inner join umls2011ab.MRSTY st on c.cui = st.cui
        inner join umls2011ab.SRDEF sr on sr.ui = st.tui 
        
        union
        
        /* the default semantic types */
        select abr
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
) s
;

-- get sources for msh wsd
select group_concat(sab order by sab)
from
(
select distinct sab
from
(select distinct if(word = 'blood_pressure', 'blood pressure', word) word, cui from nlm_wsd_cui) m
inner join umls2011ab.MRCONSO c on m.word = c.str and c.lat = 'ENG'
) s;

-- msh wsd semantic type abbreviations
select group_concat(abr order by abr)
from
(
    select distinct abr
    from
    (
        /* semantic types for cuis from msh wsd corpus */
        select sr.abr
        from
        (
        select distinct cui from msh_wsd
        ) c
        inner join umls2011ab.MRSTY st on c.cui = st.cui
        inner join umls2011ab.SRDEF sr on sr.ui = st.tui 
        
        union
        
        /* the default semantic types */
        select abr
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
) s
;
	
-- get sources for msh wsd
select group_concat(sab order by sab)
from
(
select distinct sab
from
(select distinct word, cui from msh_wsd) m
inner join umls2011ab.MRCONSO c on m.word = c.str and m.cui = c.cui and c.lat = 'ENG'
) s;