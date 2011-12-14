update classifier_eval e
inner join classifier_eval_ir ir on e.classifier_eval_id = ir.classifier_eval_id
set ir.ir_class_id = 0
where e.experiment = 'bag-word-svmlin-test' and ir_class_id = 1
;

update classifier_eval e
inner join classifier_eval_ir ir on e.classifier_eval_id = ir.classifier_eval_id
set ir.ir_class_id = 1
where e.experiment = 'bag-word-svmlin-test' and ir_class_id = 2
;

update classifier_eval e
inner join classifier_eval_ir ir on e.classifier_eval_id = ir.classifier_eval_id
set ir.ir_class_id = 2
where e.experiment = 'bag-word-svmlin-test' and ir_class_id = 3
;

delete from hotspot_zero_vector_tt 
where name = 'i2b2.2008' 
and experiment = 'bag-word-svmlin-test'
and run = 0
and fold = 0
;  

/**
 * per-fold truth table for zero vectors induced by specified cutoffs.
 * we simply add this truth table to the truth table of the classifier.
 */
insert into hotspot_zero_vector_tt (name, experiment, label, run, fold, ir_class_id, cutoff, tp, tn, fp, fn)
select 'i2b2.2008', 'bag-word-svmlin-test', label, 0, 0, ir_class_id, cutoff, tp, tn, fp, fn
from
(
	/* truth table */
	select label, cutoff, ir_class_id,
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
		select b.label, b.cutoff, a.docId, ir_class_id, jauto.judgement_id pred_class_id, jgold.judgement_id target_class_id
		from i2b2_2008_doc d
		/* join with gold class */
		inner join i2b2_2008_anno a
		  on a.docId = d.docId
		  and a.source = 'intuitive'
		/* convert to label id */
		inner join i2b2_2008_disease ds
		  on ds.disease = a.disease
    inner join best_svmlin b
        on b.label = ds.disease_id
		/* join with zero vectors */
		inner join hotspot_instance hi 
			on hi.instance_id = d.docId
			and hi.corpus_name = 'i2b2.2008'
			and hi.label = ds.disease
      and hi.experiment = 'bag-word'
		inner join hotspot_zero_vector hzv
			on hzv.hotspot_instance_id = hi.hotspot_instance_id 
      and hzv.cutoff = b.cutoff
		/* convert into class id */
		inner join i2b2_2008_judgement jgold on jgold.judgement = a.judgement
		/* get default class for zero vector */
		inner join hotspot_zv_default hzvd
		  on hzvd.label = a.disease
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
		where documentSet = 'test'
	) s
	group by label, cutoff, ir_class_id
) s;

/*
sanity check all - this should return 0
*/
select *
from
(
    -- get min and max - they should be identical
    select label, run, fold, cutoff, min(tot) mt, max(tot) xt
    from
    (
    	-- get number of instances per fold/class combo
	    select label, run, fold, ir_class_id, cutoff, tp+tn+fp+fn tot
	    from hotspot_zero_vector_tt
	    where name = 'i2b2.2008' 
		and experiment = 'bag-word-svmlin-test'
	    and run = 0
	    and fold = 0
    ) s
    group by label, run, fold, cutoff
) s where mt <> xt
;
/*
 * combine zero vectors with classifier predictions 
 * for a 'complete' truth table w/ ir metrics
 */
delete classifier_eval_irzv
from classifier_eval_irzv
inner join classifier_eval e on classifier_eval_irzv.classifier_eval_id = e.classifier_eval_id
where e.experiment = 'bag-word-svmlin-test'
and e.name = 'i2b2.2008'
and e.run = 0
and e.fold = 0
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
			and e.experiment = 'bag-word-svmlin-test'
			and e.name = 'i2b2.2008'
			and e.run = 0
			and e.fold = 0
		left join hotspot_zero_vector_tt z
			on (z.name, z.label, z.run, z.fold, z.ir_class_id, z.experiment) = (e.name, e.label, e.run, e.fold, ir.ir_class_id, e.experiment)
	) s
) s
;

/*
sanity check all - this should return 0
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
		and experiment = 'bag-word-svmlin-test'
	    and run = 0
	    and fold = 0
    ) s
    group by label, run, fold, param1
) s where mt <> xt
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
	where experiment = 'bag-word-svmlin-test'
	and name = 'i2b2.2008'
	and run = 0
	and fold = 0
	group by label, kernel, cost, gamma, weight, param1, param2
) s
group by label
order by cast(label as decimal(2,0))
;

select label, experiment, irz.ir_class_id, ir.f1, irz.f1
from classifier_eval e
inner join classifier_eval_irzv irz on e.classifier_eval_id = irz.classifier_eval_id
inner join classifier_eval_ir ir on e.classifier_eval_id = ir.classifier_eval_id and ir.ir_class_id = irz.ir_class_id
where experiment in ('bag-word-svmlin-test', 'bag-word-test')
and label = 11
;