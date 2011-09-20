delete from hotspot_zero_vector_tt 
where name = '@kernel.name@' 
and experiment = '@kernel.experiment@'
and run > 0
and fold > 0;  

/**
 * per-fold truth table for zero vectors induced by specified cutoffs.
 * we simply add this truth table to the truth table of the classifier.
 */
insert into hotspot_zero_vector_tt (name, experiment, label, run, fold, ir_class_id, cutoff, tp, tn, fp, fn)
select '@kernel.name@', '@kernel.experiment@', label, run, fold, ir_class_id, cutoff, tp, tn, fp, fn
from
(
	/* truth table */
	select label, cutoff, ir_class_id, run, fold,
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
		select ds.disease_id label, ce.run, ce.fold, hzv.cutoff, hi.instance_id, ir_class_id, jauto.judgement_id pred_class_id, jgold.judgement_id target_class_id
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
		where hi.corpus_name = '@kernel.name@' 
		and hi.experiment = '@kernel.experiment@'
	) s
	group by label, run, fold, ir_class_id, cutoff
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
	    where name = '@kernel.name@' 
		and experiment = '@kernel.experiment@'
	    and run > 0
	    and fold > 0
    ) s
    group by label, run, fold, cutoff
) s where mt <> xt
;
