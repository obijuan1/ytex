-- create a feature_eval record for the concept graph
delete from $(db_schema).feature_eval where param2 = '@ytex.conceptGraphName@' and type = 'intrinsic-infocontent';
insert into $(db_schema).feature_eval (corpus_name, param2, type) values ('', '@ytex.conceptGraphName@', 'intrinsic-infocontent');

-- copy the feature_rank records from tmp_ic
insert into $(db_schema).feature_rank (feature_eval_id, feature_name, evaluation, rank)
select feature_eval_id, feature_name, evaluation, rank
from $(db_schema).feature_eval, $(db_schema).tmp_ic
where param2 = '@ytex.conceptGraphName@' and type = 'intrinsic-infocontent';

-- cleanup
drop table $(db_schema).tmp_ic;