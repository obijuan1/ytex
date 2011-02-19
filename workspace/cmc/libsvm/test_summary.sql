select 'macro' metric, s.*
from
(
select
  experiment,
	round(avg(ir_precision),2) prec,
	round(avg(ir_recall),2) recall,
	round(2*avg(ir_precision)*avg(ir_recall)/(avg(ir_precision)+avg(ir_recall)),2) f,
	round(avg(scutPrecision),2) scuc_prec,
	round(avg(scutRecall),2) scut_recall,
	round(2*avg(scutPrecision)*avg(scutRecall)/(avg(scutPrecision)+avg(scutRecall)),2) scut_f
from weka_results
where experiment like '%-test'
group by experiment
) s

union

select 'micro', experiment,
  round(prec, 2),
  round(recall, 2),
  round(2*recall*prec/(recall+prec),2) f,
  round(scut_prec,2),
  round(scut_recall,2),
  round(2*scut_recall*scut_prec/(scut_recall+scut_prec),2) scut_f
from
(
select experiment, TP/(TP + FP) prec, TP/(TP+FN) recall,
scutTP/(scutTP + scutFP) scut_prec, scutTP/(scutTP+scutFN) scut_recall
from
(
select experiment, sum(Num_true_positives) tp, sum(Num_false_positives) fp, sum(Num_false_negatives) fn,
sum(scutTP) scutTP, sum(scutFP) scutFP, sum(scutFN) scutFN
from weka_results
where experiment like '%-test'
group by experiment
) s
) s
order by metric, f desc
;

