-- create a feature_eval record for the concept graph
delete r
from feature_eval e inner join feature_rank r on e.feature_eval_id = r.feature_eval_id
where param2 = '@ytex.conceptGraphName@' 
and type = 'intrinsic-infocontent';

delete e
from feature_eval e
where param2 = '@ytex.conceptGraphName@' 
and type = 'intrinsic-infocontent';

insert into feature_eval (corpus_name, param2, type) values ('', '@ytex.conceptGraphName@', 'intrinsic-infocontent');

-- copy the feature_rank records from tmp_ic
insert into feature_rank (feature_eval_id, feature_name, evaluation, rank)
select feature_eval_id, feature_name, evaluation, rank
from feature_eval, tmp_ic
where param2 = '@ytex.conceptGraphName@' and type = 'intrinsic-infocontent';

-- cleanup
drop table tmp_ic;