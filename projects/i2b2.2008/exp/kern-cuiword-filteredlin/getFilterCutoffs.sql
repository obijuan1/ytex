/* get the labels for which there is a 2x imputed feature */
drop table if exists i2b2_2008_param2;
create table i2b2_2008_param2
select d.disease_id, '2x' factor, b.param1*2 param2
from i2b2_2008_cv_best b
inner join i2b2_2008_disease d on d.disease_id = b.label
where b.experiment = 'bag-impcuiword'
    and b.param1 <> 1
    and d.disease in
    (
        select distinct e.label 
        from feature_rank r1 
        inner join feature_eval e 
            on e.feature_eval_id = r1.feature_eval_id
            and e.type = 'infogain-imputed'
            and e.corpus_name = 'i2b2.2008'
        where r1.evaluation >= (2*b.param1)
            and e.label = d.disease
    )
;
/* for the labels without a 2x imputed feature, pick the top feature */
insert into i2b2_2008_param2
select d.disease_id, '2x' factor, max(evaluation)
from i2b2_2008_disease d 
inner join feature_eval e 
    on e.type = 'infogain-imputed'
    and e.corpus_name = 'i2b2.2008'
    and e.label = d.disease
inner join feature_rank r1 
    on e.feature_eval_id = r1.feature_eval_id
left join i2b2_2008_param2 i2
    on i2.disease_id = d.disease_id
where i2.disease_id is null
group by d.disease_id
;        

/* 1x - for thoste that have it*/
insert into i2b2_2008_param2
select cast(b.label as decimal(2)), '1x', param1
from i2b2_2008_cv_best b
where b.experiment = 'bag-impcuiword'
    and b.param1 <> 1
;

select cast(concat('label.', disease_id, '.param2=', group_concat(truncate(param2,3) order by param2 separator ',' )) as char(50))
from i2b2_2008_param2
group by disease_id;
