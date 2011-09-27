delete from hotspot_zero_vector_tt where name = 'i2b2.2008' and experiment like 'bocuis%';  

/**
 * truth table for zero vectors induced by specified cutoffs.
 * we simply add this truth table to the truth table of the classifier.
 */
insert into hotspot_zero_vector_tt (name, experiment, label, run, fold, ir_class_id, cutoff, tp, tn, fp, fn)
select 'i2b2.2008', concat('bocuis',cutoff), label, run, fold, ir_class_id, cutoff, tp, tn, fp, fn
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
		select hzv.name, ds.disease_id label, ce.run, ce.fold, hzv.cutoff, hzv.instance_id, ir_class_id, jauto.judgement_id pred_class_id, jgold.judgement_id target_class_id
		from hotspot_zero_vector hzv
		/* join with gold class */
		inner join i2b2_2008_anno a
		  on a.disease = hzv.label
		  and a.docId = hzv.instance_id
		  and a.source = 'intuitive'
    inner join cv_fold ce on ce.label = hzv.label
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
		where hzv.name = 'i2b2.2008-cui'
	) s
	group by label, cutoff, ir_class_id, run, fold
) s;
