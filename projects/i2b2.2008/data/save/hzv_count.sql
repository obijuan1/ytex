drop table if exists hzv_count;
create temporary table hzv_count
select name, label, cutoff, count(*) hzvc
from hotspot_zero_vector
group by name, label, cutoff;

-- alter table hzv_count add column rank int;

-- update hzv_count set rank = cutoff where name = 'i2b2.2008-ambert';

update hzv_count hc
inner join feature_eval e on hc.label = e.label and e.name = 'i2b2.2008-train' and hc.name = 'i2b2.2008-ambert'
inner join feature_rank r on r.feature_eval_id = e.feature_eval_id and r.rank = hc.cutoff
set hc.cutoff = r.evaluation
;

select * from hzv_count;