-- avg 605 features at 0.03
select 0.03, avg(total), avg(cui), avg(ncuiword)
from
(
select label,
  count(*) total,
  sum(case when e.name = 'i2b2.2008-cui' then 1 else 0 end) cui,
  sum(case when e.name = 'i2b2.2008-ncuiword' then 1 else 0 end) ncuiword
from feature_eval e inner join feature_rank r on e.feature_eval_id = r.feature_eval_id
where e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui')
and r.evaluation > 0.03
group by label
) s
union
select 0.04, avg(total), avg(cui), avg(ncuiword)
from
(
select label,
  count(*) total,
  sum(case when e.name = 'i2b2.2008-cui' then 1 else 0 end) cui,
  sum(case when e.name = 'i2b2.2008-ncuiword' then 1 else 0 end) ncuiword
from feature_eval e inner join feature_rank r on e.feature_eval_id = r.feature_eval_id
where e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui')
and r.evaluation > 0.04
group by label
) s
union
select 0.05, avg(total), avg(cui), avg(ncuiword)
from
(
select label,
  count(*) total,
  sum(case when e.name = 'i2b2.2008-cui' then 1 else 0 end) cui,
  sum(case when e.name = 'i2b2.2008-ncuiword' then 1 else 0 end) ncuiword
from feature_eval e inner join feature_rank r on e.feature_eval_id = r.feature_eval_id
where e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui')
and r.evaluation > 0.05
group by label
) s
union
select 0.06, avg(total), avg(cui), avg(ncuiword)
from
(
select label,
  count(*) total,
  sum(case when e.name = 'i2b2.2008-cui' then 1 else 0 end) cui,
  sum(case when e.name = 'i2b2.2008-ncuiword' then 1 else 0 end) ncuiword
from feature_eval e inner join feature_rank r on e.feature_eval_id = r.feature_eval_id
where e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui')
and r.evaluation > 0.06
group by label
) s
union
select 0.07, avg(total), avg(cui), avg(ncuiword)
from
(
select label,
  count(*) total,
  sum(case when e.name = 'i2b2.2008-cui' then 1 else 0 end) cui,
  sum(case when e.name = 'i2b2.2008-ncuiword' then 1 else 0 end) ncuiword
from feature_eval e inner join feature_rank r on e.feature_eval_id = r.feature_eval_id
where e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui')
and r.evaluation > 0.07
group by label
) s
union
select 0.08, avg(total), avg(cui), avg(ncuiword)
from
(
select label,
  count(*) total,
  sum(case when e.name = 'i2b2.2008-cui' then 1 else 0 end) cui,
  sum(case when e.name = 'i2b2.2008-ncuiword' then 1 else 0 end) ncuiword
from feature_eval e inner join feature_rank r on e.feature_eval_id = r.feature_eval_id
where e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui')
and r.evaluation > 0.08
group by label
) s
union
select 0.09, avg(total), avg(cui), avg(ncuiword)
from
(
select label,
  count(*) total,
  sum(case when e.name = 'i2b2.2008-cui' then 1 else 0 end) cui,
  sum(case when e.name = 'i2b2.2008-ncuiword' then 1 else 0 end) ncuiword
from feature_eval e inner join feature_rank r on e.feature_eval_id = r.feature_eval_id
where e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui')
and r.evaluation > 0.09
group by label
) s
union
select 0.1, avg(total), avg(cui), avg(ncuiword)
from
(
select label,
  count(*) total,
  sum(case when e.name = 'i2b2.2008-cui' then 1 else 0 end) cui,
  sum(case when e.name = 'i2b2.2008-ncuiword' then 1 else 0 end) ncuiword
from feature_eval e inner join feature_rank r on e.feature_eval_id = r.feature_eval_id
where e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui')
and r.evaluation > 0.1
group by label
) s
union
select 0.12, avg(total), avg(cui), avg(ncuiword)
from
(
select label,
  count(*) total,
  sum(case when e.name = 'i2b2.2008-cui' then 1 else 0 end) cui,
  sum(case when e.name = 'i2b2.2008-ncuiword' then 1 else 0 end) ncuiword
from feature_eval e inner join feature_rank r on e.feature_eval_id = r.feature_eval_id
where e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui')
and r.evaluation > 0.12
group by label
) s
union
select 0.14, avg(total), avg(cui), avg(ncuiword)
from
(
select label,
  count(*) total,
  sum(case when e.name = 'i2b2.2008-cui' then 1 else 0 end) cui,
  sum(case when e.name = 'i2b2.2008-ncuiword' then 1 else 0 end) ncuiword
from feature_eval e inner join feature_rank r on e.feature_eval_id = r.feature_eval_id
where e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui')
and r.evaluation > 0.14
group by label
) s
union
select 0.16, avg(total), avg(cui), avg(ncuiword)
from
(
select label,
  count(*) total,
  sum(case when e.name = 'i2b2.2008-cui' then 1 else 0 end) cui,
  sum(case when e.name = 'i2b2.2008-ncuiword' then 1 else 0 end) ncuiword
from feature_eval e inner join feature_rank r on e.feature_eval_id = r.feature_eval_id
where e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui')
and r.evaluation > 0.16
group by label
) s
union
select 0.2, avg(total), avg(cui), avg(ncuiword)
from
(
select label,
  count(*) total,
  sum(case when e.name = 'i2b2.2008-cui' then 1 else 0 end) cui,
  sum(case when e.name = 'i2b2.2008-ncuiword' then 1 else 0 end) ncuiword
from feature_eval e inner join feature_rank r on e.feature_eval_id = r.feature_eval_id
where e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui')
and r.evaluation > 0.2
group by label
) s
union
select 0.3, avg(total), avg(cui), avg(ncuiword)
from
(
select label,
  count(*) total,
  sum(case when e.name = 'i2b2.2008-cui' then 1 else 0 end) cui,
  sum(case when e.name = 'i2b2.2008-ncuiword' then 1 else 0 end) ncuiword
from feature_eval e inner join feature_rank r on e.feature_eval_id = r.feature_eval_id
where e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui')
and r.evaluation > 0.3
group by label
) s
union
select 0.5, avg(total), avg(cui), avg(ncuiword)
from
(
select label,
  count(*) total,
  sum(case when e.name = 'i2b2.2008-cui' then 1 else 0 end) cui,
  sum(case when e.name = 'i2b2.2008-ncuiword' then 1 else 0 end) ncuiword
from feature_eval e inner join feature_rank r on e.feature_eval_id = r.feature_eval_id
where e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui')
and r.evaluation > 0.5
group by label
) s
union
select 1.0, avg(total), avg(cui), avg(ncuiword)
from
(
select label,
  count(*) total,
  sum(case when e.name = 'i2b2.2008-cui' then 1 else 0 end) cui,
  sum(case when e.name = 'i2b2.2008-ncuiword' then 1 else 0 end) ncuiword
from feature_eval e inner join feature_rank r on e.feature_eval_id = r.feature_eval_id
where e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui')
and r.evaluation > 1.0
group by label
) s
;