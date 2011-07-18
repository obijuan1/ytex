delete from hotspot_zero_vector_tt 
where name = 'i2b2.2008-test' 
and experiment = '@kernel.experiment@';  

/**
 * per-fold truth table for zero vectors induced by specified cutoffs.
 * we simply add this truth table to the truth table of the classifier.
 */
insert into hotspot_zero_vector_tt (name, experiment, label, run, fold, ir_class_id, cutoff, tp, tn, fp, fn)
select 'i2b2.2008-test', '@kernel.experiment@', 1, 0, 0, ir_class_id, @export.cutoff@, tp, tn, fp, fn
from
(
	/* truth table */
	select ir_class_id,
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
		select a.docId, ir_class_id, jauto.judgement_id pred_class_id, jgold.judgement_id target_class_id
		from i2b2_2008_doc d
		/* join with gold class */
		inner join i2b2_2008_anno a
		  on a.docId = d.docId
		  and a.source = 'intuitive'
		/* convert to label id */
		inner join i2b2_2008_disease ds
		  on ds.disease = a.disease
      and ds.disease_id = 1
		/* join with zero vectors */
		inner join hotspot_instance hi 
			on hi.instance_id = d.docId
			and hi.corpus_name = 'i2b2.2008'
			and hi.label = ds.disease
      and hi.experiment = '@kernel.experiment@'
		inner join hotspot_zero_vector hzv
			on hzv.hotspot_instance_id = hi.hotspot_instance_id 
      and hzv.cutoff = @export.cutoff@
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
	group by ir_class_id
) s;