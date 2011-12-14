drop table if exists tmp_hotspot_zero_vector_tt;
/**
 * per-fold truth table for zero vectors induced by specified cutoffs.
 * we simply add this truth table to the truth table of the classifier.
 */
create temporary table tmp_hotspot_zero_vector_tt
as
select label, run, fold, ir_class_id, tp, tn, fp, fn
from
(
	/* truth table */
	select label, ir_class_id, run, fold,
	  sum(case
	    when ir_class_id = target_class_id and ir_class_id = pred_class_id then 1
	    else 0
	  end) tp,
	  sum(case
	    when ir_class_id <> target_class_id and ir_class_id <> pred_class_id then 1
	    else 0
	  end) tn,
	  sum(case
	    when ir_class_id <> target_class_id and ir_class_id = pred_class_id then 1
	    else 0
	  end) fp,
	  sum(case
	    when ir_class_id = target_class_id and ir_class_id <> pred_class_id then 1
	    else 0
	  end) fn
	from
	(
		select ds.disease_id label, ce.run, ce.fold, hi.instance_id, ir_class_id, jauto.judgement_id pred_class_id, jgold.judgement_id target_class_id
		from hotspot_instance hi
		inner join hotspot_zero_vector hzv 
			on hi.hotspot_instance_id = hzv.hotspot_instance_id
		/* join with gold class */
		inner join i2b2_2008_anno a
		  on a.disease = hi.label
		  and a.docId = hi.instance_id
		  and a.source = 'intuitive'
	    inner join cv_fold ce 
	    	on ce.label = hi.label
	    	and ce.corpus_name = hi.corpus_name
	    /* limit to the test instances of this fold */
	    inner join cv_fold_instance ci
	      on ci.cv_fold_id = ce.cv_fold_id
	      and ci.instance_id = hi.instance_id
	      and ci.train = 0
		/* convert into class id */
		inner join i2b2_2008_judgement jgold on jgold.judgement = a.judgement
		/* get default class for zero vector */
		inner join hotspot_zv_default hzvd
		  on hzvd.label = hi.label
		/* convert to label id */
		inner join i2b2_2008_disease ds
		  on ds.disease = hi.label
		/* convert into class id */
		inner join i2b2_2008_judgement jauto on jauto.judgement = hzvd.class_name
		inner join
		/* create truth table - get all unique class ids */
		(
			select distinct d.disease_id, j.judgement_id ir_class_id
			from i2b2_2008_judgement j
			inner join i2b2_2008_anno a on a.judgement = j.judgement
			inner join i2b2_2008_disease d on d.disease = a.disease
			where a.source = 'intuitive'
		) ja on ja.disease_id = ds.disease_id
		where hi.corpus_name = 'i2b2.2008' 
		and hi.experiment = 'bag-mrrel'
	) s
	group by label, run, fold, ir_class_id
) s;

/*
sanity check all - this should return 0
*/
select *
from
(
    -- get min and max - they should be identical
    select label, run, fold, min(tot) mt, max(tot) xt
    from
    (
    	-- get number of instances per fold/class combo
	    select label, run, fold, ir_class_id, tp+tn+fp+fn tot
	    from tmp_hotspot_zero_vector_tt
    ) s
    group by label, run, fold
) s where mt <> xt
;


/*
 * combine zero vectors with classifier predictions 
 * for a 'complete' truth table w/ ir metrics
 */
delete classifier_eval_irzv
from classifier_eval_irzv
inner join classifier_eval e on classifier_eval_irzv.classifier_eval_id = e.classifier_eval_id
where e.experiment = 'bag-mrrel'
and e.name = 'i2b2.2008'
and e.fold > 0
and e.run > 0
;

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
			and e.experiment = 'bag-mrrel'
			and e.name = 'i2b2.2008'
			and e.run > 0
			and e.fold > 0
		left join tmp_hotspot_zero_vector_tt z
			on (z.label, z.run, z.fold, z.ir_class_id) = (e.label, e.run, e.fold, ir.ir_class_id)
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
		and experiment = 'bag-mrrel'
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
    from cv_fold f
    inner join cv_fold_instance i 
        on f.cv_fold_id = i.cv_fold_id
        and i.train = 0
    group by corpus_name, label, run, fold
) cv on cv.corpus_name = e.name
    and d.disease = cv.label 
    and e.run = cv.run 
    and e.fold = cv.fold
where e.experiment = 'bag-mrrel'
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
	select label, cost, avg(f1) f1
	from classifier_eval_irzv t
	inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
	inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
	where experiment = 'bag-mrrel'
	and name = 'i2b2.2008'
	and run > 0
	and fold > 0
	group by label, kernel, cost, gamma, weight, param1, param2
) s
group by label
order by cast(label as decimal(2,0))
;