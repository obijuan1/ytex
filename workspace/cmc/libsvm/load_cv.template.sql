/**
 * need to use CHARACTER SET latin1 COLLATE latin1_swedish_ci
 * with utf8_general_ci get following error:
 * ERROR 1271 (HY000) at line 63 in file: 'load_cv.sql': Illegal mix of collations for operation '='
 */

delete from weka_results 
where experiment = '@EXPERIMENT@';

load data local infile 'cv.txt'
into table weka_results
(
Key_Scheme, Key_Scheme_options, 
kernel, label, cost, weight, gamma, degree, 
measureNumSupportVectors, 
num_true_positives, num_false_positives, num_true_negatives, num_false_negatives, IR_precision, IR_recall, F_measure,
scutThreshold,
scutTP, scutFP, scutTN, scutFN, scutPrecision, scutREcall, scutFMeasure
)
set experiment = '@EXPERIMENT@'
;

drop table if exists best_f1;
create temporary table best_f1 (
    label varchar(10),
    kernel int,
    best_f1 double
) CHARACTER SET latin1 COLLATE latin1_swedish_ci;

-- select * from weka_results where experiment = '@EXPERIMENT@' and kernel = 0;

insert into best_f1 (kernel, label, best_f1)
select kernel, label, max(f1a)
from
(
    select kernel, label, cost, weight, coalesce(degree, 0), coalesce(gamma, 0), avg(F_measure) f1a
    from weka_results
    where experiment in ('@EXPERIMENT@')
    group by kernel, label, cost, weight, coalesce(degree, 0), coalesce(gamma, 0)
) s
group by kernel, label
;




drop table if exists best_nsv;
create temporary table best_nsv (
    label varchar(10),
    kernel int,
    best_f1 double,
    best_nsv int
) CHARACTER SET latin1 COLLATE latin1_swedish_ci;

insert into best_nsv
select p.label, p.kernel, p.best_f1, round(min(nsva)) min_nsv
from
(
    select kernel, label, cost, weight, coalesce(degree, 0), coalesce(gamma, 0), avg(F_measure) f1a, avg(measureNumSupportVectors) nsva
    from weka_results
    where experiment in ('@EXPERIMENT@')
    group by kernel, label, cost, weight, coalesce(degree, 0), coalesce(gamma, 0)
) s inner join best_f1 p on s.label = p.label and s.f1a = p.best_f1 and p.kernel = s.kernel
group by kernel, p.label, p.best_f1
;

drop table if exists best_cost;
create temporary table best_cost (
    label varchar(10),
    kernel int,
    best_f1 double,
    best_nsv int,
    max_cost double,
    min_cost double
) CHARACTER SET latin1 COLLATE latin1_swedish_ci;

insert into best_cost
select p.label, p.kernel, p.best_f1, p.best_nsv, max(cost), min(cost)
from
(
    select kernel, label, cost, weight, coalesce(degree, 0), coalesce(gamma, 0), avg(F_measure) f1a, round(avg(measureNumSupportVectors)) nsva
    from weka_results
    where experiment in ('@EXPERIMENT@')
    group by kernel, label, cost, weight, coalesce(degree, 0), coalesce(gamma, 0)
) s inner join best_nsv p
    on (s.kernel, s.label, s.f1a, s.nsva)  = (p.kernel, p.label, p.best_f1, p.best_nsv)
group by p.kernel, p.label, p.best_f1
;

-- for linear and precomputed kernels, no other parameters
-- for polynomial, select min(degree) - prefer lower degrees (less squiggly)
drop table if exists best_degree;
create temporary table best_degree (
    label varchar(10),
    kernel int,
    best_f1 double,
    best_nsv int,
    min_cost double,
    degree int
) CHARACTER SET latin1 COLLATE latin1_swedish_ci;


insert into best_degree
select p.label, p.kernel, p.best_f1, p.best_nsv, s.cost, min(degree)
from
(
    select kernel, label, cost, weight, degree, avg(F_measure) f1a, round(avg(measureNumSupportVectors)) nsva
    from weka_results
    where experiment in ('@EXPERIMENT@')
    and kernel = 1
    group by kernel, label, cost, weight, degree
) s inner join best_cost p
    on (s.kernel, s.label, s.f1a, s.nsva, s.cost)  = (p.kernel, p.label, p.best_f1, p.best_nsv, p.min_cost)
group by p.kernel, p.label, p.best_f1;


-- for rbg, select best min(gamma)
drop table if exists best_gamma;
create temporary table best_gamma (
    label varchar(10),
    kernel int,
    best_f1 double,
    best_nsv int,
    min_cost double,
    gamma double
) CHARACTER SET latin1 COLLATE latin1_swedish_ci;


insert into best_gamma
select p.label, p.kernel, p.best_f1, p.best_nsv, s.cost, min(gamma)
from
(
    select kernel, label, cost, weight, gamma, avg(F_measure) f1a, round(avg(measureNumSupportVectors)) nsva
    from weka_results
    where experiment in ('@EXPERIMENT@')
    and kernel = 2
    group by kernel, label, cost, weight, gamma
) s inner join best_cost p
    on (s.kernel, s.label, s.f1a, s.nsva, s.cost)  = (p.kernel, p.label, p.best_f1, p.best_nsv, p.min_cost)
group by p.kernel, p.label, p.best_f1;


delete from libsvm_cv_best where experiment = '@EXPERIMENT@';

insert into libsvm_cv_best (label, best_f1, best_nsv, max_cost, min_cost, weight, scutThreshold, experiment, kernel)
select p.label, p.best_f1, p.best_nsv, p.max_cost, p.max_cost, min(weight), null, '@EXPERIMENT@', p.kernel
from
(
    select kernel, label, cost, weight, avg(F_measure) f1a, round(avg(measureNumSupportVectors)) nsva
    from weka_results
    where experiment in ('@EXPERIMENT@')
    and kernel in (0, 4)
    group by kernel, label, cost, weight
) s inner join best_cost p
    on (s.kernel, s.label, s.f1a, s.nsva, s.cost)  = (p.kernel, p.label, p.best_f1, p.best_nsv, p.min_cost)
group by p.kernel, p.label, p.best_f1
;

-- polynomial kernel
insert into libsvm_cv_best (label, best_f1, best_nsv, min_cost, weight, scutThreshold, experiment, kernel, degree)
select p.label, p.best_f1, p.best_nsv, p.min_cost, min(weight), null, '@EXPERIMENT@', p.kernel, p.degree
from
(
    select kernel, label, cost, weight, degree, avg(F_measure) f1a, round(avg(measureNumSupportVectors)) nsva
    from weka_results
    where experiment in ('@EXPERIMENT@')
    and kernel in (1)
    group by kernel, label, cost, weight, degree
) s inner join best_degree p
    on (s.kernel, s.label, s.f1a , s.nsva, s.cost, s.degree)  = (p.kernel, p.label, p.best_f1, p.best_nsv, p.min_cost, p.degree)
group by p.kernel, p.label, p.best_f1
;

-- rbg kernel
insert into libsvm_cv_best (label, best_f1, best_nsv, min_cost, weight, scutThreshold, experiment, kernel, gamma)
select p.label, p.best_f1, p.best_nsv, p.min_cost, min(weight), null, '@EXPERIMENT@', p.kernel, p.gamma
from
(
    select kernel, label, cost, weight, gamma, avg(F_measure) f1a, round(avg(measureNumSupportVectors)) nsva
    from weka_results
    where experiment in ('@EXPERIMENT@')
    and kernel in (2)
    group by kernel, label, cost, weight, gamma
) s inner join best_gamma p
    on (s.kernel, s.label, s.f1a , s.nsva, s.cost, s.gamma)  = (p.kernel, p.label, p.best_f1, p.best_nsv, p.min_cost, p.gamma)
group by p.kernel, p.label, p.best_f1
;

select label, round(best_f1, 3), best_nsv, kernel
from libsvm_cv_best
where experiment = '@EXPERIMENT@'
order by label, best_f1 desc, best_nsv
;


-- select the 'simplest' kernel from the best performers
-- prefer linear (0), then poly (1), then rbg (2), then precomputed (3)
drop table if exists tmp_best;
create temporary table tmp_best (
  label varchar(10),
  kernel int
);

insert into tmp_best
select c.label, min(c.kernel)
from libsvm_cv_best c
inner join
(
  select label, max(best_f1) f1
  from
  (
    select label, kernel, round(best_f1, 3) best_f1, best_nsv
    from libsvm_cv_best
    where experiment in ('@EXPERIMENT@')
  ) s
  group by label
) s on c.label = s.label
  and round(c.best_f1, 3) = s.f1
  and c.experiment = '@EXPERIMENT@'
group by c.label
;

-- update libsvm_cv_best with best kernel
update libsvm_cv_best lb
inner join tmp_best tb on lb.label = tb.label
set lb.best_kernel = tb.kernel
where lb.experiment = '@EXPERIMENT@'
;

/*
 * use scut when the stdev of the scut threshold is < 0.10 (the threshold is stable)
 * and if the average improvement in f-score is > 5%
 */
update libsvm_cv_best lcb
inner join
(
select label, kernel, cost, weight, experiment, degree, gamma,
	avg(scutFMeasure-F_Measure) sf1diff, avg(scutThreshold) scutThreshold, stddev_pop(scutThreshold) sdevScutThreshold
from weka_results
group by label, kernel, cost, weight, experiment, degree, gamma
) cv on (cv.label, cv.kernel, cv.cost, cv.weight, cv.experiment, cv.gamma, cv.degree) = (lcb.label, lcb.kernel, lcb.min_cost, lcb.weight, lcb.experiment, lcb.gamma, lcb.degree)
set lcb.scutThreshold = cv.scutThreshold
where sdevScutThreshold < 0.10
and sf1diff > 0.05
and lcb.experiment = '@EXPERIMENT@'
;

/*
select avg(best_f1)
from libsvm_cv_best
where experiment = '@EXPERIMENT@'
and kernel = best_kernel
;
*/