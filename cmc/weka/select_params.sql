-- Update the cost
update weka_results
set cost = cast(substr(Key_scheme_version_id, INSTR(Key_scheme_version_id, '-C ') +3, INSTR(Key_scheme_version_id, '-L ')-INSTR(Key_scheme_version_id, '-C ')-3) as decimal(14,7))
where experiment in ('cmc-cv' )
;

drop table if exists best_f1;
create temporary table best_f1 (
    label varchar(10),
    best_f1 double
);


insert into best_f1 (label, best_f1)
select label, max(f1a)
from
(
    select label, experiment, cost, avg(F_measure) f1a
    from weka_results
    where experiment in ('cmc-cv' )
    group by label, experiment, cost
    order by label asc, avg(F_measure) desc, experiment asc, cost desc
) s
group by label
;


drop table if exists best_nsv;
create temporary table best_nsv (
    label varchar(10),
    best_f1 double,
    best_nsv int
);

insert into best_nsv
select p.label, p.best_f1, round(min(nsva)) min_nsv
from
(
    select label, cost, avg(F_measure) f1a, avg(measureNumSupportVectors) nsva
    from weka_results
    where experiment in ('cmc-cv' )
    group by label, cost
) s inner join best_f1 p on s.label = p.label and s.f1a = p.best_f1
group by p.label, p.best_f1
;

/*
drop table if exists best_cost;
create temporary table best_cost (
    label varchar(10),
    best_f1 double,
    best_nsv int,
    max_cost double,
    min_cost double
);

insert into best_cost
select p.label, p.best_f1, p.best_nsv, max(cost), min(cost)
from
(
    select label, cost, avg(F_measure) f1a, round(avg(measureNumSupportVectors)) nsva
    from weka_results
    where experiment in ('cmc-cv' )
    group by label, cost
) s inner join best_nsv p
    on (s.label, s.f1a, s.nsva)  = (p.label, p.best_f1, p.best_nsv)
group by p.label, p.best_f1
;

drop table if exists cmc_cv_best;
create table cmc_cv_best (
    label varchar(10),
    best_f1 double,
    max_cost double,
    min_cost double,
    experiment varchar(10)
);


insert into cmc_cv_best
select label, best_f1, max_cost, min_cost, case when exp_code = 0 then 'cmc-cv-r' else 'cmc-cv' end experiment
from
(
select p.label, p.best_f1, max_cost, min_cost, max(exp_code) exp_code
from
(
    select label, case when experiment = 'cmc-cv-r' then 0 else 1 end exp_code, cost, avg(F_measure) f1a
    from weka_results
    where experiment in ('cmc-cv' )
    group by label, case when experiment = 'cmc-cv-r' then 0 else 1 end, cost
) s inner join best_cost p
    on (s.label, s.f1a, cost)  = (p.label, p.best_f1, max_cost)
group by p.label, p.best_f1, max_cost, min_cost
) s
;

-- generate properties file with best parameters
select concat(label, '.cost=', min_cost) prop
from cmc_cv_best
union
select concat(label, '.resample=', case when experiment = 'cmc-cv-r' then 'yes' else 'no' end)
from cmc_cv_best
order by prop
;
*/

drop table if exists cmc_cv_best;
create table cmc_cv_best (
    label varchar(10),
    best_f1 double,
    best_nsv int,
    max_cost double,
    min_cost double
);

insert into cmc_cv_best
select p.label, p.best_f1, p.best_nsv, max(cost), min(cost)
from
(
    select label, cost, avg(F_measure) f1a, round(avg(measureNumSupportVectors)) nsva
    from weka_results
    where experiment in ('cmc-cv' )
    group by label, cost
) s inner join best_nsv p
    on (s.label, s.f1a, s.nsva)  = (p.label, p.best_f1, p.best_nsv)
group by p.label, p.best_f1
;

select concat(label, '.cost=', min_cost) prop
from cmc_cv_best
union
select concat(label, '.resample=', 'no')
from cmc_cv_best
order by prop