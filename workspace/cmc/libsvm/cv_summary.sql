/*
 * generate table with micro and macro scores
 * for the optimal cross-validation parameter settings 
 */
select 'micro' metric, experiment, round(prec,2) prec, round(recall,2) recall, round(2*recall*prec/(recall+prec),2) f,
round(scut_prec,2) scut_prec, round(scut_recall,2) scut_recall, round(2*scut_recall*scut_prec/(scut_recall+scut_prec),2) scut_f
from
(
select experiment, TP/(TP + FP) prec, TP/(TP+FN) recall,
scutTP/(scutTP + scutFP) scut_prec, scutTP/(scutTP+scutFN) scut_recall
from
(
select r.experiment, sum(Num_true_positives) tp, sum(Num_false_positives) fp, sum(Num_false_negatives) fn,
sum(scutTP) scutTP, sum(scutFP) scutFP, sum(scutFN) scutFN
from weka_results r
inner join libsvm_cv_best b
	on r.experiment = b.experiment
	and r.kernel = b.kernel
	and r.weight = b.weight
	and r.cost = b.min_cost
	and r.label = b.label
	and r.degree = b.degree
	and r.gamma = b.gamma
group by r.experiment
) s
) s

union

select 'macro' metric, s.*
from
(
select
	r.experiment,
	round(avg(ir_precision),2) prec,
	round(avg(ir_recall),2) recall,
	round(2*avg(ir_precision)*avg(ir_recall)/(avg(ir_precision)+avg(ir_recall)),2) f,
	round(avg(scutPrecision),2) scut_prec,
	round(avg(scutRecall),2) scut_recall,
	round(2*avg(scutPrecision)*avg(scutRecall)/(avg(scutPrecision)+avg(scutRecall)),2) scut_f
from weka_results r
inner join libsvm_cv_best b
	on r.experiment = b.experiment
	and r.kernel = b.kernel
	and r.weight = b.weight
	and r.cost = b.min_cost
	and r.label = b.label
	and r.degree = b.degree
	and r.gamma = b.gamma
group by r.experiment
) s
order by metric, f desc
;