-- delete from svm_test_result where name = '@EXPERIMENT@';

delete from weka_results where experiment = '@EXPERIMENT@-test';
-- 1	0	0	966	10	0	0	0	0	0	966	10	0	0	0
load data local infile 'test_results.txt'
into table weka_results 
(
label, 
num_true_positives, num_false_positives, num_true_negatives, num_false_negatives, 
ir_precision, ir_recall, f_measure, 
scutTP, scutFP, scutTN, scutFN, scutPrecision, scutRecall, scutFMeasure
)
set experiment = '@EXPERIMENT@-test'
;
