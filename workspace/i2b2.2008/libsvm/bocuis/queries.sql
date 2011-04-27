/*
 * Queries to view features with high mutual information
 */

select label,
  count(*) total,
  sum(case when e.name = 'i2b2.2008-cui' then 1 else 0 end) cui,
  sum(case when e.name = 'i2b2.2008-ncuiword' then 1 else 0 end) ncuiword
from feature_eval e inner join feature_rank r on e.feature_eval_id = r.feature_eval_id
where e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui')
and r.evaluation > 0.03
group by label;

select label,
  count(*) total,
  sum(case when e.name = 'i2b2.2008-cui' then 1 else 0 end) cui,
  sum(case when e.name = 'i2b2.2008-ncuiword' then 1 else 0 end) ncuiword
from feature_eval e inner join feature_rank r on e.feature_eval_id = r.feature_eval_id
where e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui')
and r.evaluation > 0.05
group by label;

select label,
  count(*) total,
  sum(case when e.name = 'i2b2.2008-cui' then 1 else 0 end) cui,
  sum(case when e.name = 'i2b2.2008-ncuiword' then 1 else 0 end) ncuiword
from feature_eval e inner join feature_rank r on e.feature_eval_id = r.feature_eval_id
where e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui')
and r.evaluation > 0.10
group by label;

select * 
from feature_eval e 
inner join feature_rank r on e.feature_eval_id = r.feature_eval_id
where e.label = 'Asthma' 
and r.evaluation > 0.10 
and e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui');
