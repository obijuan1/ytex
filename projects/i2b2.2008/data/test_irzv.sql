/*
* we assume that the hotspot cutoff is in the param1 column
* fill this in for the test classifier evaluations
*/
update classifier_eval e
inner join cv_best_svm s 
    on e.label = s.label 
    and e.name = s.corpus_name
    and s.experiment = '@kernel.cv.experiment@'
set e.param1 = s.param1, e.param2 = s.param2
where e.experiment = '@kernel.experiment@'
and e.name = '@kernel.name@'
;

/*
 * combine zero vectors with classifier predictions 
 * for a 'complete' truth table w/ ir metrics
 * cleanup anything that didn't come from libsvm
 */
delete ir
from classifier_eval_ir ir
inner join classifier_eval e on ir.classifier_eval_id = e.classifier_eval_id
where e.experiment = '@kernel.experiment@'
and e.name = '@kernel.name@'
and ir.ir_type <> ''
;

drop table if exists hzv_tt;


/**
 * per-label truth table for zero vectors induced by specified cutoffs.
 * we simply add this truth table to the truth table of the classifier.
 */
create temporary table hzv_tt as
select label, ir_class, tp, tn, fp, fn
from
(
	/* truth table */
	select label, ir_class, 
	  sum(case
	    when ir_class = target_class and ir_class = 'N' then 1
	    else 0
	  end) tp,
	  sum(case
	    when ir_class <> target_class and ir_class <> 'N' then 1
	    else 0
	  end) tn,
	  sum(case
	    when ir_class <> target_class and ir_class = 'N' then 1
	    else 0
	  end) fp,
	  sum(case
	    when ir_class = target_class and ir_class <> 'N' then 1
	    else 0
	  end) fn
	from
	(
		select l.label, l.instance_id, ir_class, l.class target_class
		from corpus_doc d 
		/* join with gold class */
		inner join corpus_label l 
			on l.instance_id = d.instance_id
			and l.corpus_name = d.corpus_name
		inner join
		/* create truth table - get all unique class ids */
		(
			select label, class ir_class
			from v_corpus_group_class
			where corpus_name = '@kernel.name@'
			and doc_group = 'test'
		) ja on ja.label = l.label
		/* limit to zero vectors */
		inner join classifier_eval e 
	        on e.name = l.corpus_name
	        and e.label = l.label
	        and e.experiment = '@kernel.experiment@'
	    inner join hotspot_instance hi
			on l.instance_id = hi.instance_id
			and l.corpus_name = hi.corpus_name
			and l.label = hi.label
			and hi.experiment = '@kernel.cv.experiment@'
			and hi.min_rank > e.param1
		where d.corpus_name = '@kernel.name@'
        and d.doc_group = 'test'
  ) s
	group by label, ir_class
) s;

create unique index NK_hzv_tt on hzv_tt(label, ir_class);

/*
sanity check all - instances per class should be identical
*/
select  *
from
(
    -- get min and max - they should be identical
    select label, min(tot) mt, max(tot) xt
    from
    (
    	-- get number of instances per label/class combo
	    select label, ir_class, tp+tn+fp+fn tot
	    from hzv_tt
    ) s
    group by label
) s where mt <> xt
;

/*
 * combined classifier + zero vector truth table and ir metrics
 * the zero vectors for one experiment (hzv.experiment) may be applicable to the zero vectors
 * for another experiment (experiment), 
 */
insert into classifier_eval_ir (classifier_eval_id, ir_class, ir_class_id, tp, tn, fp, fn, ppv, npv, sens, spec, f1, ir_type)
select s.*,
  case when ppv + sens > 0 then 2*ppv*sens/(ppv+sens) else 0 end f1, 'zv'
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
		  ir.ir_class,
		  ir.ir_class_id,
		  ir.tp + coalesce(z.tp, 0) tp,
		  ir.tn + coalesce(z.tn, 0) tn,
		  ir.fp + coalesce(z.fp, 0) fp,
		  ir.fn + coalesce(z.fn, 0) fn
		from classifier_eval_ir ir
		inner join classifier_eval e 
			on ir.classifier_eval_id = e.classifier_eval_id
	    /* limit only to classes from test set */
	    inner join v_corpus_group_class vc
	        on vc.label = e.label
	        and vc.class = ir.ir_class
	        and vc.doc_group = 'test'   
        left join hzv_tt z
			on (z.label, z.ir_class) = (e.label, ir.ir_class)
		where ir.ir_type = ''
			and e.experiment = '@kernel.experiment@'
			and e.name = '@kernel.name@'	
	) s
) s
;

/*
sanity check all - # predictions per class should be identical
*/
select  *
from
(
    -- get min and max - they should be identical
    select label, min(tot) mt, max(tot) xt
    from
    (
    	-- get number of instances per fold/class combo
	    select label, run, fold, ir_class, tp+tn+fp+fn tot
	    from classifier_eval_ir ir
	    inner join classifier_eval e 
            on ir.classifier_eval_id = e.classifier_eval_id 
            and e.experiment = '@kernel.experiment@'
            and e.name = '@kernel.name@' 
	    where ir.ir_type = 'zv'
    ) s
    group by label
) s where mt <> xt
;

/* 
 * sanity check - predictions per label should be identical to test
 */
select  s.label, nclass, ncorpus
from
(
    select distinct label, tp+tn+fp+fn nclass
    from classifier_eval_ir ir
    inner join classifier_eval e 
        on ir.classifier_eval_id = e.classifier_eval_id 
        and e.experiment = '@kernel.experiment@'
        and e.name = '@kernel.name@' 
    where ir.ir_type = 'zv'
) s
inner join
(
	select label, count(*) ncorpus
	from corpus_doc d
	inner join corpus_label a 
        on a.instance_id = d.instance_id 
        and a.corpus_name = d.corpus_name
	where d.doc_group = 'test'
	group by label
) i on s.label = i.label
where ncorpus <> nclass
;


/* 
 * sanity check - classes per label should be identical to test
 */
select  *
from
(
	select label, count(distinct ir_class_id) nclass
	from classifier_eval_ir ir
	inner join classifier_eval e 
		on ir.classifier_eval_id = e.classifier_eval_id 
		and e.experiment = '@kernel.experiment@'
		and e.name = '@kernel.name@' 
	where ir.ir_type = 'zv'
	group by label
) s
inner join
(
	select label, count(*) ngold
	from v_corpus_group_class
	where corpus_name = '@kernel.name@'
	and doc_group = 'test'
	group by label
) i on s.label = i.label
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
	select label, avg(f1) f1
	from classifier_eval_ir t
	inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
	inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
	where experiment = '@kernel.experiment@'
	and name = '@kernel.name@'
    and ir_type = 'zv'
	group by label
) s
group by label
order by label
;

/* get macro-averaged f1 across all labels */
select 'macro f1', avg(f1) f1
from
(
    select *, if(ppv+sens > 0, 2*ppv*sens/(ppv+sens), 0) f1
    from
    (
        select *, if(tp+fp > 0, tp/(tp+fp), 0) ppv, if(tp+fn >0, tp/(tp+fn), 0) sens
        from
        (
            select ir_class, sum(tp) tp, sum(fp) fp, sum(tn) tn, sum(fn) fn
            from classifier_eval e
            inner join classifier_eval_ir i on e.classifier_eval_id = i.classifier_eval_id
			where experiment = '@kernel.experiment@'
			and name = '@kernel.name@'
			and ir_type = 'zv'
            group by ir_class
        ) s 
    ) s
) s;

/* get micro-averaged f1 across all labels */
select 'micro f1', avg(f1) f1
from
(
    select *, if(ppv+sens > 0, 2*ppv*sens/(ppv+sens), 0) f1
    from
    (
        select *, if(tp+fp > 0, tp/(tp+fp), 0) ppv, if(tp+fn >0, tp/(tp+fn), 0) sens
        from
        (
            select sum(tp) tp, sum(fp) fp, sum(tn) tn, sum(fn) fn
            from classifier_eval e
            inner join classifier_eval_ir i on e.classifier_eval_id = i.classifier_eval_id
			where experiment = '@kernel.experiment@'
			and name = '@kernel.name@'
		    and ir_type = 'zv'
        ) s 
    ) s
) s;