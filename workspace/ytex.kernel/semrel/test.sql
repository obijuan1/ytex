select count(*) from sct2_relationship where typeId = 116680003 and active = 1;
-- 440649 snomed is-a relationships removing inactives

select count(*)
from umls.MRREL 
where sab = 'SNOMEDCT'
and rel = 'CHD' and rela = 'isa'
;
-- 532724 is-a relationships in umls

select count(distinct code) from umls.MRCONSO where sab = 'SNOMEDCT' ;
-- 391173 snomed concepts in umls

select count(distinct cui) from umls.MRCONSO where sab = 'SNOMEDCT' ;
-- 320648 snomed concepts map to less cuis (390K to 320K)

select count(*) from sct2_concept where active = 1;
-- 295708 only 295K active concepts in snomed???

