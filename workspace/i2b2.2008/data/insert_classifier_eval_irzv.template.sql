/*
 * combine zero vectors with classifier predictions 
 * for a 'complete' truth table w/ ir metrics
 */
delete classifier_eval_irzv
from classifier_eval_irzv
inner join classifier_eval e on classifier_eval_irzv.classifier_eval_id = e.classifier_eval_id
where e.experiment = '@experiment@'
and e.name = 'i2b2.2008';

delete from hotspot_zero_vector_tt 
where name = 'i2b2.2008' 
and experiment = '@experiment@';  

/**
 * per-fold truth table for zero vectors induced by specified cutoffs.
 * we simply add this truth table to the truth table of the classifier.
 */
insert into hotspot_zero_vector_tt (name, experiment, label, run, fold, ir_class_id, cutoff, tp, tn, fp, fn)
select corpus_name, experiment, label, run, fold, ir_class_id, cutoff, tp, tn, fp, fn
from
(
	/* truth table */
	select corpus_name, experiment, label, cutoff, ir_class_id, run, fold,
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
		select hzv.corpus_name, hzv.experiment, ds.disease_id label, ce.run, ce.fold, hzv.cutoff, hzv.instance_id, ir_class_id, jauto.judgement_id pred_class_id, jgold.judgement_id target_class_id
		from hotspot_zero_vector hzv
		/* join with gold class */
		inner join i2b2_2008_anno a
		  on a.disease = hzv.label
		  and a.docId = hzv.instance_id
		  and a.source = 'intuitive'
	    inner join cv_fold ce on ce.label = hzv.label
	    /* limit to the test instances of this fold */
	    inner join cv_fold_instance ci
	      on ci.cv_fold_id = ce.cv_fold_id
	      and ci.instance_id = hzv.instance_id
	      and ci.train = 0
		/* convert into class id */
		inner join i2b2_2008_judgement jgold on jgold.judgement = a.judgement
		/* get default class for zero vector */
		inner join hotspot_zv_default hzvd
		  on hzvd.label = hzv.label
		/* convert to label id */
		inner join i2b2_2008_disease ds
		  on ds.disease = hzv.label
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
		where hzv.corpus_name = 'i2b2.2008' 
		and hzv.experiment = '@experiment@'
	) s
	group by corpus_name, experiment, label, run, fold, ir_class_id, cutoff
) s;

/*
 * combined classifier + zero vector truth table and ir metrics
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
		inner join classifier_eval e on ir.classifier_eval_id = e.classifier_eval_id
		left join hotspot_zero_vector_tt z
		  on (z.name, z.experiment, z.label, z.run, z.fold, z.ir_class_id, z.cutoff) = (e.name, e.experiment, e.label, e.run, e.fold, ir.ir_class_id, e.param1)
		where e.experiment = '@experiment@'
		and e.name = 'i2b2.2008'
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
    	where experiment = '@experiment@'
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
    where experiment = '@experiment@'
    and name = 'i2b2.2008'
    group by label, cost, weight, param1
) e on s.label = e.label and (round(s.f1, 2)-0.1) <= round(e.f1,2)
group by e.label, s.f1
order by cast(label as decimal(2,0));