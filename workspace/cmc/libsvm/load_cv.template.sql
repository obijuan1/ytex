/**
 * need to use CHARACTER SET latin1 COLLATE latin1_swedish_ci
 * with utf8_general_ci get following error:
 * ERROR 1271 (HY000) at line 63 in file: 'load_cv.sql': Illegal mix of collations for operation '='
 */

delete from weka_results 
where experiment = '@EXPERIMENT@';

load data local infile 'cv.txt'
into table weka_results
(label, cost, weight, measureNumSupportVectors, 
num_true_positives, num_false_positives, num_true_negatives, num_false_negatives, IR_precision, IR_recall, F_measure,
scutThreshold,
scutTP, scutFP, scutTN, scutFN, scutPrecision, scutREcall, scutFMeasure
)
set experiment = '@EXPERIMENT@', key_scheme_options = 'libsvm'
;

drop table if exists best_f1;
create temporary table best_f1 (
    label varchar(10),
    best_f1 double
) CHARACTER SET latin1 COLLATE latin1_swedish_ci;


insert into best_f1 (label, best_f1)
select label, max(f1a)
from
(
    select label, cost, weight, avg(F_measure) f1a
    from weka_results
    where experiment in ('@EXPERIMENT@')
    group by label, cost, weight
    order by label asc, avg(F_measure) desc, experiment asc, cost desc
) s
group by label
;


drop table if exists best_nsv;
create temporary table best_nsv (
    label varchar(10),
    best_f1 double,
    best_nsv int
) CHARACTER SET latin1 COLLATE latin1_swedish_ci;

insert into best_nsv
select p.label, p.best_f1, round(min(nsva)) min_nsv
from
(
    select label, cost, weight, avg(F_measure) f1a, avg(measureNumSupportVectors) nsva
    from weka_results
    where experiment in ('@EXPERIMENT@')
    group by label, cost, weight
) s inner join best_f1 p on s.label = p.label and s.f1a = p.best_f1
group by p.label, p.best_f1
;

drop table if exists best_cost;
create temporary table best_cost (
    label varchar(10),
    best_f1 double,
    best_nsv int,
    max_cost double,
    min_cost double
) CHARACTER SET latin1 COLLATE latin1_swedish_ci;

insert into best_cost
select p.label, p.best_f1, p.best_nsv, max(cost), min(cost)
from
(
    select label, cost, weight, avg(F_measure) f1a, round(avg(measureNumSupportVectors)) nsva
    from weka_results
    where experiment in ('@EXPERIMENT@')
    group by label, cost, weight
) s inner join best_nsv p
    on (s.label, s.f1a, s.nsva)  = (p.label, p.best_f1, p.best_nsv)
group by p.label, p.best_f1
;


delete from libsvm_cv_best where experiment = '@EXPERIMENT@';

insert into libsvm_cv_best
select p.label, p.best_f1, p.best_nsv, p.max_cost, p.min_cost, min(weight), null, '@EXPERIMENT@' 
from
(
    select label, cost, weight, avg(F_measure) f1a, round(avg(measureNumSupportVectors)) nsva
    from weka_results
    where experiment in ('@EXPERIMENT@')
    group by label, cost, weight
) s inner join best_cost p
    on (s.label, s.f1a, s.nsva, s.cost)  = (p.label, p.best_f1, p.best_nsv, p.min_cost)
group by p.label, p.best_f1
;


select lcb.* 
from libsvm_cv_best lcb
inner join
(
select label, cost, weight, experiment,
	avg(scutFMeasure) sf1a, avg(scutFMeasure-F_Measure) sf1diff,  
	avg(scutThreshold) scutThreshold, stddev_pop(scutThreshold) sdevScutThreshold
from weka_results
group by label, cost, weight, experiment
) cv on (cv.label, cv.cost, cv.weight, cv.experiment) = (lcb.label, lcb.min_cost, lcb.weight, lcb.experiment)
where cv.experiment = '@EXPERIMENT@'
;

/*
 * use scut when the stdev of the scut threshold is < 0.10 (the threshold is stable)
 * and if the average improvement in f-score is > 5%
 */
update libsvm_cv_best lcb
inner join
(
select label, cost, weight, experiment,
	avg(scutPrecision) sprec, avg(scutRecall) srec, avg(scutFMeasure) sf1a, 
	avg(scutFMeasure-F_Measure) sf1diff, avg(scutThreshold) scutThreshold, stddev_pop(scutThreshold) sdevScutThreshold
from weka_results
group by label, cost, weight, experiment
) cv on (cv.label, cv.cost, cv.weight, cv.experiment) = (lcb.label, lcb.min_cost, lcb.weight, lcb.experiment)
set lcb.scutThreshold = cv.scutThreshold
where sdevScutThreshold < 0.10
and sf1diff > 0.05
and lcb.experiment = '@EXPERIMENT@' 
;
