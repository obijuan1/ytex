select name, label, feature_name, evaluation
from feature_eval e inner join feature_rank r on e.feature_eval_id = r.feature_eval_id
where name in ('i2b2.2008-train','i2b2.2008-cui','i2b2.2008-ncuiword')
;