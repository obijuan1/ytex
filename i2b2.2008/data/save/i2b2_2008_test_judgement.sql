/*
 * this table is needed to piece together the truth tables
 * for evaluating the classifiers on the test data
 */
drop table if exists i2b2_2008_test_judgement;
create table i2b2_2008_test_judgement
as 
select distinct a.disease, j.judgement, j.judgement_id
from i2b2_2008_doc d
inner join i2b2_2008_anno a on a.docId = d.docId and a.source='intuitive'
inner join i2b2_2008_judgement j on j.judgement = a.judgement
where d.documentSet = 'test'
;
create unique index NK_i2b2_2008_test_judgement on i2b2_2008_test_judgement(disease, judgement_id);
