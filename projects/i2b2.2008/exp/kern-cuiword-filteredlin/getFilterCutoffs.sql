select cast(concat('label.', d.disease_id, '.filters=', group_concat(truncate(r.evaluation,3) order by r.evaluation separator ',' )) as char(50))
from feature_eval e
inner join feature_rank r 
    on e.feature_eval_id = r.feature_eval_id and r.rank in (5,10,15)
inner join i2b2_2008_disease d on d.disease = e.label
where e.type = 'mutualinfo-parent'
group by d.disease_id