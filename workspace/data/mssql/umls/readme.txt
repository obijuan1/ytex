export snomed, nci, and icd9 subset using this:

bcp "select * from umls..MRCONSO where SAB in ('SNOMEDCT', 'RXNORM', 'SRC')" queryout MRCONSO.bcp -S localhost -T -n
bcp "select * from umls..MRSTY where CUI in (select distinct CUI from umls..MRCONSO where SAB in ('SNOMEDCT', 'RXNORM', 'SRC'))" queryout MRSTY.bcp -S localhost -T -n
bcp "select * from YTEX_TEST.test.umls_aui_fword where aui in (select distinct AUI from umls..MRCONSO where SAB in ('SNOMEDCT', 'RXNORM', 'SRC'))" queryout umls_aui_fword.bcp -S localhost -T -n

export examples using this:
bcp "select * from umls..MRCONSO where CUI in (select distinct code from YTEX_TEST.test.anno_ontology_concept) and SAB='SNOMEDCT'" queryout MRCONSO.bcp -S localhost -T -n
bcp "select * from umls..MRSTY where CUI in (select distinct code from YTEX_TEST.test.anno_ontology_concept)" queryout MRSTY.bcp -S localhost -T -n
bcp "select * from YTEX_TEST.test.umls_aui_fword where aui in (select AUI from umls..MRCONSO m inner join (select distinct code from YTEX_TEST.test.anno_ontology_concept) c on m.cui = c.code where SAB='SNOMEDCT')" queryout umls_aui_fword.bcp -S localhost -T -n
