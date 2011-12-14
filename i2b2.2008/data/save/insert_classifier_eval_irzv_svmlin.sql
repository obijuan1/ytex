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
			and e.experiment = 'bag-word-svmlin'
			and e.name = 'i2b2.2008'
			and e.run > 0
			and e.fold > 0
		left join hotspot_zero_vector_tt z
			on (z.name, z.label, z.run, z.fold, z.ir_class_id, z.cutoff) = (e.name, e.label, e.run, e.fold, ir.ir_class_id, e.param1)
			and z.experiment = 'bag-word'
	) s
) s
;

/*
sanity check all - this should return 0
make sure the number of instances for each class is the same
*/
select *
from
(
    -- get min and max - they should be identical
    select label, run, fold, param1, min(tot) mt, max(tot) xt
    from
    (
    	-- get number of instances per fold/class combo
	    select label, run, fold, ir_class_id, param1, tp+tn+fp+fn tot
	    from classifier_eval_irzv ir
	    inner join classifier_eval e on ir.classifier_eval_id = e.classifier_eval_id 
	    where name = 'i2b2.2008' 
		and experiment = 'bag-word-svmlin'
	    and run > 0
	    and fold > 0
    ) s
    group by label, run, fold, param1
) s where mt <> xt
;

/*
sanity check - this should return no rows
make sure the number of instances in the test fold is identical
to the number of instances in our truth table
*/
select e.*, (tp+tn+fp+fn) tot, fc
from classifier_eval_irzv t
inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
inner join i2b2_2008_disease d on d.disease_id = e.label
inner join
(
	/* count up instances per test fold */
	select corpus_name, label, run, fold, count(*) fc
	from
	(
	    select corpus_name, label, run, fold, i.instance_id, sum(i.train) train
	    from cv_fold f
	    inner join cv_fold_instance i 
	        on f.cv_fold_id = i.cv_fold_id
	    group by corpus_name, label, run, fold, i.instance_id   
	) s where train = 0
) cv on cv.corpus_name = e.name
    and d.disease = cv.label 
    and e.run = cv.run 
    and e.fold = cv.fold
where e.experiment = 'bag-word-svmlin'
and e.name = 'i2b2.2008' 
and e.run > 0
and e.fold > 0
and (tp+tn+fp+fn) <> fc
;

/*
 * show the best f1 per label for the experiment
 */
select label, truncate(max(f1),3) f1
from
(
	/*
	 * best f1 score by experiment (hotspot cutoff) and svm parameters
	 */
	select label, kernel, cost, gamma, weight, param1, param2, avg(f1) f1
	from classifier_eval_irzv t
	inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
	inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
	where experiment = 'bag-word-svmlin'
	and name = 'i2b2.2008'
	and run > 0
	and fold > 0
	group by label, kernel, cost, gamma, weight, param1, param2
) s
group by label
order by cast(label as decimal(2,0))
;

