delete from weka_results where experiment = 'cmc-test-minnsv'
;

load data local infile 'results-all.txt' 
into table weka_results 
fields TERMINATED BY ' ' 
(label, Num_true_negatives, Num_false_positives, Num_false_negatives, Num_true_positives) 
set experiment = 'cmc-test-minnsv'
;

update weka_results
set IR_precision = Num_true_positives/(Num_true_positives+Num_false_positives),
IR_recall = Num_true_positives/(Num_true_positives+Num_false_negatives)
where experiment='cmc-test-minnsv'
;

update weka_results
set F_measure = 2*IR_precision*IR_recall/(IR_precision+IR_recall)
where experiment='cmc-test-minnsv' and (IR_precision+IR_recall) > 0
;

select IR_precision, IR_recall, 2*IR_precision*IR_recall/(IR_precision+IR_recall) F_measure
from
(
select avg(IR_precision) IR_precision, avg(IR_recall) IR_recall
from weka_results
where experiment='cmc-test-minnsv'
) s
;
-- Macro w/ resampling:
-- 0.565586280848525, 0.525028875109736, 0.544553460841076
-- w/out resampling max cost:
-- 0.6805183357777959 | 0.34461759496096317 | 0.45753657670271397
-- w/out resampling min cost:
-- 0.6915572968167567 | 0.33424722459059275 | 0.4506728178371579 |
-- w/out resampling min nsv, cost:
-- 0.764870306650277 | 0.40028004030526754 | 0.5255327228357366 |
-- cost params from libsvm:
-- 0.7817556433045343 | 0.3940527513865905 | 0.5239849682091592 
-- libsvm:
-- 0.46, 0.38, 0.41

-- micro:
select prec, recall, 2*prec*recall/(prec+recall) F_measure
from
(
select tp/(tp + fp) prec, tp/(tp+fn) recall
from
(
select sum(Num_true_positives) tp, sum(Num_false_positives) fp, sum(Num_false_negatives) fn
from weka_results
where experiment='cmc-test-minnsv'
) s
) s
;
-- with resampling
-- 0.171848597499155, 0.843983402489627, 0.285553839674295
-- no resampling max cost
-- 0.833 0.6912863070539419 0.7555555555555554
-- no resampling min cost
-- 0.8502564102564103 | 0.6879668049792531 | 0.7605504587155963 |
-- no resampling min nsv, cost
-- 0.8421052631578947 | 0.7435684647302905 | 0.7897752313794624 |
-- using cost parameters from libsvm
-- 0.8570048309178744 | 0.7360995850622407 | 0.7919642857142857 |
-- for comparison - libsvm
-- 0.8425	0.7859	0.813216347335
-- possible problem with weka - didn't look at number of support vectors when selecting cost

-- compare weka to libsvm
-- libsvm wins hands down
select distinct *, flibsvm - fweka
from
(
select label, round(l.f1score,2) flibsvm, round(w.f_measure,2) fweka
from
(
select label, IR_precision, IR_recall, F_measure
from weka_results
where experiment='cmc-test-minnsv'
) w
inner join
(
select cl.code, prec, recall, f1score
from svm_test_result s inner join cmcclasslabels cl on s.task = cl.labelid
where name = 'cmc_sujeevan_umlskernel_test3'
and classLabel = '1'
) l on l.code = w.label
) s where flibsvm - fweka > 0.1
order by flibsvm - fweka
;


/*
 * 
 * using best max cost:
 591    |    0.82 |  0.71 |            0.11 |
 753.3  |    0.32 |  0.21 |            0.11 |
 486    |    0.81 |  0.67 |            0.14 |
 599.7  |    0.92 |  0.76 |            0.16 |
 786.50 |    0.86 |  0.67 |            0.19 |
 v67.09 |    0.44 |  0.14 |            0.30 |
 759.89 |    0.78 |  0.43 |            0.35 |
 593.89 |    0.43 |  0.07 |            0.36 |
 493.90 |    0.75 |  0.32 |            0.43 |
 
 using params from libsvm:
| 486    |    0.81 |  0.67 |            0.14 |
| 741.90 |    0.52 |  0.38 |            0.14 |
| v67.09 |    0.44 |  0.29 |            0.15 |
| 786.05 |    0.67 |  0.50 |            0.17 |
| 791.0  |    0.75 |  0.50 |            0.25 |
| 277.00 |    0.50 |  0.11 |            0.39 |
 */
