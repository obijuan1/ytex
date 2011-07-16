/*
 * combine zero vectors with classifier predictions 
 * for a 'complete' truth table w/ ir metrics
 */
delete classifier_eval_irzv
from classifier_eval_irzv
inner join classifier_eval e on classifier_eval_irzv.classifier_eval_id = e.classifier_eval_id
where e.experiment = '@kernel.experiment@'
and e.name = 'i2b2.2008';

/*
 * combined classifier + zero vector truth table and ir metrics
 * the zero vectors for one experiment (hzv.experiment) may be applicable to the zero vectors
 * for another experiment (experiment), 
 */
insert into classifier_eval_irzv (classifier_eval_id, ir_class_id, tp, tn, fp, fn, ppv, npv, sens, spec, f1)
select s.*,
  case when ppv + sens > 0 then 2*ppv*sens/(ppv+sens) else 0 end f1
from
(
	select s.*,
	  case when tp+fp <> 0 then tp/(tp+fp) else 0 end ppv,
	  case when tn+fn <> 0 then tn/(tn+fn) else 0 end npv,
	  case when tp+fn <> 0 then tp/(tp+fn) else 0 end sens,
	  case when tn+fp <> 0 then tn/(tn+fp) else 0 end spec
	from
	(
		select
		  e.classifier_eval_id,
		  ir.ir_class_id,
		  ir.tp + coalesce(z.tp, 0) tp,
		  ir.tn + coalesce(z.tn, 0) tn,
		  ir.fp + coalesce(z.fp, 0) fp,
		  ir.fn + coalesce(z.fn, 0) fn
		from classifier_eval_ir ir
		inner join classifier_eval e 
			on ir.classifier_eval_id = e.classifier_eval_id
			and e.experiment = '@kernel.experiment@'
			and e.name = 'i2b2.2008'
		left join hotspot_zero_vector_tt z
			on (z.name, z.label, z.run, z.fold, z.ir_class_id, z.cutoff) = (e.name, e.label, e.run, e.fold, ir.ir_class_id, e.param1)
			and z.experiment = '@kernel.hzv.experiment@'
	) s
) s
;

/*
 * show the best f1 and cutoff for the experiment
 */
select cast(s.label as decimal(2,0)) label, round(s.f1, 3) f1, max(param1) min_cutoff, max(param1) max_cutoff
from
(
	select label, max(f1) f1
	from
	(
		/*
		 * best f1 score by experiment (hotspot cutoff) and svm parameters
		 */
		select label, kernel, cost, gamma, weight, param1, avg(f1) f1
		from classifier_eval_irzv t
		inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
		inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
    	where experiment = '@kernel.experiment@'
    	and name = 'i2b2.2008'
		group by label, kernel, cost, gamma, weight, param1
	) s
	group by label
) s 
inner join
(
    /*
     * f1 score by label, experiment, hotspot cutoff, and svm parameters
     */
    select label, cost, weight, param1, avg(f1) f1
    from classifier_eval_irzv t
    inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
    inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
    where experiment = '@kernel.experiment@'
    and name = 'i2b2.2008'
    group by label, cost, weight, param1
) e on s.label = e.label and (round(s.f1, 3)-0.001) <= round(e.f1,3)
group by e.label, s.f1
order by cast(label as decimal(2,0));