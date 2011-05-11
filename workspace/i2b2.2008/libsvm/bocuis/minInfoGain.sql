select label, min(max_eval)
from
(
select label, instance_id, max(evaluation) max_eval
from hotspot_feature_eval
where name = 'i2b2.2008-cui'
group by label, instance_id
) s
group by label
;