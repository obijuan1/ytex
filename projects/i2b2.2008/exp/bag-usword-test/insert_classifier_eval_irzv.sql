update classifier_eval e
inner join i2b2_2008_cv_best b on b.label = e.label and b.experiment = 'bag-usword'
set e.param1 = b.param1
where e.experiment = 'bag-usword-test' and e.name = 'i2b2.2008'
;

drop table if exists tmp_hotspot_zero_vector_tt 
;  

/**
 * per-label truth table for zero vectors induced by specified cutoffs.
 * we simply add this truth table to the truth table of the classifier.
 */
create temporary table tmp_hotspot_zero_vector_tt as
select disease_id label, ir_class_id, cutoff, tp, tn, fp, fn
from
(
	/* truth table */
	select ir_class_id, disease_id, cutoff,
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
		select a.docId, ds.disease_id, hzv.cutoff, ja.judgement_id ir_class_id, jauto.judgement_id pred_class_id, jgold.judgement_id target_class_id
		from i2b2_2008_doc d
		/* join with gold class */
		inner join i2b2_2008_anno a
		  on a.docId = d.docId
		  and a.source = 'intuitive'
		/* convert to label id */
		inner join i2b2_2008_disease ds
		  on ds.disease = a.disease
		/* join with zero vectors */
		inner join hotspot_instance hi 
			on hi.instance_id = d.docId
			and hi.corpus_name = 'i2b2.2008'
			and hi.label = ds.disease
      and hi.experiment = 'bag-usword'
		inner join hotspot_zero_vector hzv
			on hzv.hotspot_instance_id = hi.hotspot_instance_id 
		/* convert into class id */
		inner join i2b2_2008_judgement jgold on jgold.judgement = a.judgement
		/* get default class for zero vector */
		inner join hotspot_zv_default hzvd
		  on hzvd.label = a.disease
		/* convert into class id */
		inner join i2b2_2008_judgement jauto on jauto.judgement = hzvd.class_name
		/* get all possible classes for this disease */
		inner join i2b2_2008_test_judgement ja on ja.disease = a.disease
		/* limit to test instances */
		where documentSet = 'test'
	) s
	group by ir_class_id, disease_id, cutoff
) s;

create index IX_hotspot_zero_vector_tt on  tmp_hotspot_zero_vector_tt(label, ir_class_id, cutoff);

/*
sanity check all - instances per class should be identical
*/
select *
from
(
    -- get min and max - they should be identical
    select label, cutoff, min(tot) mt, max(tot) xt
    from
    (
    	-- get number of instances per fold/class combo
	    select label, ir_class_id, cutoff, tp+tn+fp+fn tot
	    from tmp_hotspot_zero_vector_tt
    ) s
    group by label, cutoff
) s where mt <> xt
;
/*
 * combine zero vectors with classifier predictions 
 * for a 'complete' truth table w/ ir metrics
 */
delete classifier_eval_irzv
from classifier_eval_irzv
inner join classifier_eval e on classifier_eval_irzv.classifier_eval_id = e.classifier_eval_id
where e.experiment = 'bag-usword-test'
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
			and e.experiment = 'bag-usword-test'
			and e.name = 'i2b2.2008'
			and e.run = 0
			and e.fold = 0
    /* 
    limit to disease ids from test classes 
    training set has Q's where the test set doesn't
    */
    inner join i2b2_2008_disease d 
        on d.disease_id = e.label
		inner join i2b2_2008_test_judgement ja 
        on ja.disease = d.disease
        and ja.judgement_id = ir.ir_class_id
		left join tmp_hotspot_zero_vector_tt z
			on (z.label, z.ir_class_id, z.cutoff) = (e.label, ir.ir_class_id, e.param1)	) s
) s
;

/*
sanity check all - # predictions per class should be identical
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
		and experiment = 'bag-usword-test'
	    and run = 0
	    and fold = 0
    ) s
    group by label, run, fold, param1
) s where mt <> xt
;

/* 
 * sanity check - predictions per label should be identical to test
 */
select *
from
(
	select label, tp+tn+fp+fn n
	from classifier_eval e
	inner join classifier_eval_irzv i on e.classifier_eval_id = i.classifier_eval_id and i.ir_class_id = 0
	where e.experiment = 'bag-usword-test'
	group by label
) s
inner join
(
	select disease_id, count(*) n
	from i2b2_2008_doc d
	inner join i2b2_2008_anno a on a.docId = d.docId and source = 'intuitive'
	inner join i2b2_2008_disease ds on ds.disease = a.disease
	where d.documentSet = 'test'
	group by disease_id
) i on s.label = i.disease_id
where s.n <> i.n
;

/* 
 * sanity check - classes per label should be identical to test
 */
select *
from
(
	select label, count(distinct ir_class_id) nclass
	from classifier_eval e
	inner join classifier_eval_irzv i on e.classifier_eval_id = i.classifier_eval_id
	where e.experiment = 'bag-usword-test'
	group by label
) s
inner join
(
	select disease_id, count(distinct judgement) ngold
	from i2b2_2008_doc d
	inner join i2b2_2008_anno a on a.docId = d.docId and source = 'intuitive'
	inner join i2b2_2008_disease ds on ds.disease = a.disease
	where d.documentSet = 'test'
	group by disease_id
) i on s.label = i.disease_id
where s.nclass <> i.ngold;

/*
 * show the best f1 per label for the experiment
 */
select label, truncate(max(f1),3) f1
from
(
	/*
	 * best f1 score by experiment (hotspot cutoff) and svm parameters
	 */
	select label, kernel, cost, gamma, coalesce(weight, '') weight, param1, param2, avg(f1) f1
	from classifier_eval_irzv t
	inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
	inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
	where experiment = 'bag-usword-test'
	and name = 'i2b2.2008'
	and run = 0
	and fold = 0
	group by label, kernel, cost, gamma, coalesce(weight, ''), param1, param2
) s
group by label
order by cast(label as decimal(2,0))
;

/* get macro-averaged f1 across all labels */
select avg(f1)
from
(
    select *, if(ppv+sens > 0, 2*ppv*sens/(ppv+sens), 0) f1
    from
    (
        select *, if(tp+fp > 0, tp/(tp+fp), 0) ppv, if(tp+fn >0, tp/(tp+fn), 0) sens
        from
        (
            select ir_class_id, sum(tp) tp, sum(fp) fp, sum(tn) tn, sum(fn) fn
            from classifier_eval e
            inner join classifier_eval_irzv i on e.classifier_eval_id = i.classifier_eval_id
            where e.experiment = 'bag-usword-test'
            group by ir_class_id
        ) s 
    ) s
) s;