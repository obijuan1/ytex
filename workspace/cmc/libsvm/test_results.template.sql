delete from svm_test_result where name = '@EXPERIMENT@';

-- 1	0	0	966	10	0	0	0	0	0	966	10	0	0	0
load data local infile 'E:/projects/ytex/sujeevan/processedData/test_results.txt'
into table weka_results 
(
label, 
num_true_positives, num_false_positives, num_true_negatives, num_false_negatives, 
ir_precision, ir_recall, f_measure, 
scutTP, scutFP, scutTN, scutFN, scutPrecision, scutRecall, scutFMeasure
)
set experiment = '@EXPERIMENT@'
;

select 
	round(avg(ir_precision),2) macro_precision, 
	round(avg(ir_recall),2) macro_recall, 
	round(2*avg(ir_precision)*avg(ir_recall)/(avg(ir_precision)+avg(ir_recall)),2) macro_f,
	round(avg(scutPrecision),2) scut_macro_precision, 
	round(avg(scutRecall),2) scut_macro_recall, 
	round(2*avg(scutPrecision)*avg(scutRecall)/(avg(scutPrecision)+avg(scutRecall)),2) scut_macro_f
from weka_results
where experiment = '@EXPERIMENT@'
;

select micro_precision, micro_recall, 2*micro_recall*micro_precision/(micro_recall+micro_precision) micro_f,
scut_micro_precision, scut_micro_recall, 2*scut_micro_recall*scut_micro_precision/(scut_micro_recall+scut_micro_precision) micro_f
from
(
select TP/(TP + FP) micro_precision, TP/(TP+FN) micro_recall,
scutTP/(scutTP + scutFP) scut_micro_precision, scutTP/(scutTP+scutFN) scut_micro_recall
from
(
select sum(Num_true_positives) tp, sum(Num_false_positives) fp, sum(Num_false_negatives) fn,
sum(scutTP) scutTP, sum(scutFP) scutFP, sum(scutFN) scutFN
from weka_results
where experiment = '@EXPERIMENT@'
) s
) s
;

select label,
	round(ir_precision,2) p, 
	round(ir_recall,2) r, 
	round(2*ir_precision*ir_recall/(ir_precision+ir_recall),2) f,
	round(scutPrecision,2) sp, 
	round(scutRecall,2) sr, 
	round(2*scutPrecision*scutRecall/(scutPrecision+scutRecall),2) sf
from weka_results
where experiment = '@EXPERIMENT@'
;
/*
Macro scores are way off:
0.46 0.38	0.41
vs sujeevan:
63  63  62

1/28 results:
without scut: 0.49, 0.37, 0.42,
with scut: 0.46, 0.50, 0.48

micro scores are in the ballpark:
prec    recall  F
0.8425	0.7859	0.813216347335
sujeevan paper:
82% 84% 83%

1/28 results:
without scut: 
0.9175603217158177	0.827190332326284	0.8700349539243726	
with scut: 
0.8230	0.8737	0.847592503094
difference from previous results: model selection based on scut
*/

select label, scutFP,
	round(ir_precision,2) p, 
	round(ir_recall,2) r, 
	round(2*ir_precision*ir_recall/(ir_precision+ir_recall),2) f,
	round(scutPrecision,2) sp, 
	round(scutRecall,2) sr, 
	round(2*scutPrecision*scutRecall/(scutPrecision+scutRecall),2) sf
from weka_results
where experiment = '@EXPERIMENT@'
order by scutFP desc
;

select w.label, scutFP - num_false_positives, l.scutThreshold, 
	ir_precision, 
	ir_recall, 
	f_measure,
	scutPrecision, 
	scutRecall, 
	scutFmeasure
from weka_results w inner join libsvm_cv_best l on w.label = l.label
where experiment = '@EXPERIMENT@'
order by scutFP - num_false_positives desc
;
