
/*
 * combine zero vectors with classifier predictions 
 * for a 'complete' truth table w/ ir metrics
 * cleanup anything that didn't come from libsvm
 */
delete ir
from classifier_eval_ir ir
inner join classifier_eval e on ir.classifier_eval_id = e.classifier_eval_id
where e.experiment = 'word-posthoc'
and e.name = 'i2b2.2008'
and ir.ir_type <> ''
;

drop table if exists hzv_cutoff;
create temporary table hzv_cutoff
as
select distinct param1 rank
from classifier_eval
where name = 'i2b2.2008' 
and experiment = 'word-posthoc'
;

create unique index IX_rank on  hzv_cutoff(rank);

drop table if exists hzv_tt;


/**
 * per-label truth table for zero vectors induced by specified cutoffs.
 * we simply add this truth table to the truth table of the classifier.
 */
create temporary table hzv_tt as
select label, ir_class, rank cutoff, tp, tn, fp, fn
from
(
	/* truth table */
	select label, rank, ir_class, 
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
		select l.label, hc.rank, l.instance_id, ir_class, l.class target_class
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
			where corpus_name = 'i2b2.2008'
			and doc_group = 'test'
		) ja on ja.label = l.label
		/* limit to zero vectors */
    inner join hotspot_instance hi
			on l.instance_id = hi.instance_id
			and l.corpus_name = hi.corpus_name
			and l.label = hi.label
			and hi.experiment = 'word'
    /* limit to zero vectors */
		inner join hzv_cutoff hc 
        on hi.min_rank > hc.rank            
    where d.corpus_name = 'i2b2.2008'
        and d.doc_group = 'test'
  ) s
	group by label, rank, ir_class
) s;

create unique index NK_hzv_tt on hzv_tt(label, ir_class, cutoff);

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
    	-- get number of instances per label/class combo
	    select label, ir_class, cutoff, tp+tn+fp+fn tot
	    from hzv_tt
    ) s
    group by label, cutoff
) s where mt <> xt
;


/**
 * for some hotspot cutoffs + folds, we may be missing entire classes
 * add the missing truth tables
 */
drop table if exists tmp_missing_ir;
create temporary table tmp_missing_ir
as
select fc.label, fc.class, e.classifier_eval_id, e.param1
from v_corpus_group_class fc
inner join classifier_eval e
    on e.experiment = 'word-posthoc'
    and e.label = fc.label
/* filter out the class ids for which we have the ir metrics */
left join classifier_eval_ir ir 
    on ir.classifier_eval_id = e.classifier_eval_id 
    and ir.ir_class = fc.class
where ir.classifier_eval_ir_id is null
and fc.doc_group = 'test'
;

create unique index NK_tmp_missing_ir on tmp_missing_ir (label, class, classifier_eval_id, param1);

insert into classifier_eval_ir (classifier_eval_id, ir_class, tn, fn, ir_class_id, tp, fp, ir_type)
select *, 0, 0, 0, 'miss'
from
(
	select fc.classifier_eval_id, fc.class, sum(a.class <> fc.class) tn, sum(a.class = fc.class) fn
	from
	/* get fold, classifier evaluation, and missing class ids */
	tmp_missing_ir fc
	/* get the judgement for these instances */
	inner join corpus_label a 
	    on a.label = fc.label
	    and a.corpus_name = 'i2b2.2008'
  inner join corpus_doc d 
    on d.instance_id = a.instance_id
    and d.doc_group = 'test'
	/* filter out zero vectors */
	inner join hotspot_instance i 
	    on i.experiment = 'word'
	    and i.corpus_name = a.corpus_name
	    and i.label = fc.label 
	    and i.instance_id = d.instance_id
	    and i.min_rank <= param1
	where a.corpus_name = 'i2b2.2008'
	group by fc.classifier_eval_id, fc.class
) s
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
			on (z.label, z.ir_class, z.cutoff) = (e.label, ir.ir_class, e.param1)
		where ir.ir_type in ('', 'miss')
			and e.experiment = 'word-posthoc'
			and e.name = 'i2b2.2008'	
	) s
) s
;

/*
sanity check all - # predictions per class should be identical
*/
select *
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
            and e.experiment = 'word-posthoc'
            and e.name = 'i2b2.2008' 
	    where ir.ir_type = 'zv'
    ) s
    group by label
) s where mt <> xt
;

/* 
 * sanity check - predictions per label should be identical to test
 */
select s.label, nclass, ncorpus
from
(
    select distinct label, tp+tn+fp+fn nclass
    from classifier_eval_ir ir
    inner join classifier_eval e 
        on ir.classifier_eval_id = e.classifier_eval_id 
        and e.experiment = 'word-posthoc'
        and e.name = 'i2b2.2008' 
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
select *
from
(
	select label, count(distinct ir_class) nclass
	from classifier_eval_ir ir
	inner join classifier_eval e 
		on ir.classifier_eval_id = e.classifier_eval_id 
		and e.experiment = 'word-posthoc'
		and e.name = 'i2b2.2008' 
	where ir.ir_type = 'zv'
	group by label
) s
inner join
(
	select label, count(*) ngold
	from v_corpus_group_class
	where corpus_name = 'i2b2.2008'
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
    select label, kernel, cost, param1, avg(f1) f1
    from classifier_eval_ir t
    inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
    inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
    where experiment = 'word-posthoc'
    and name = 'i2b2.2008'
    and ir_type = 'zv'
    group by label, kernel, cost, param1
) s
group by label
order by label
;

/* get best parameters */
delete from cv_best_svm 
where experiment = 'word-posthoc'
and corpus_name = 'i2b2.2008'
;

/* 
 * get the macro average f1 for each parameter combination. 
 * we leave the Q class out for the macro average when selecting the optimal params
 * because there are so few examples of this class that it skews results.
 */
drop table if exists tmp_param_f1;
create temporary table tmp_param_f1
as
select label, cost, param1, param2, avg(f1) f1, stddev(f1) stddevf1, avg(f1) - stddev(f1) f1s
from
(
  select label, cost, param1, param2, run, fold, avg(f1) f1
    from classifier_eval_ir t
    inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
    inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
    where name = 'i2b2.2008'
    and experiment = 'word-posthoc'
    and t.ir_type = 'zv'
    group by label, cost, param1, run, fold
) s group by label, cost, param1;

create unique index IX_paramf1 on tmp_param_f1(label, cost, param1, param2);

/* get the max f1 for each label */
insert into cv_best_svm (corpus_name, label, experiment, f1)
select 'i2b2.2008', label, 'word-posthoc', f1
from
(
	/*
	 * best f1 score by experiment (hotspot cutoff) and svm parameters
	 */
	select label, truncate(max(f1),3) f1
	from tmp_param_f1
	group by label
) s
;

/*
 * set the kernel type - assume only 1 kernel type per experiment
 */
update cv_best_svm 
set kernel = 
	(
	select distinct kernel 
	from classifier_eval e
    inner join classifier_eval_svm s on e.classifier_eval_id = s.classifier_eval_id
	where name = 'i2b2.2008'
    and experiment = 'word-posthoc'
    )
where corpus_name = 'i2b2.2008'
    and experiment = 'word-posthoc'
;

/*
* get the best hotspot cutoff (param1)
*/
drop table if exists cv_param1;
create temporary table cv_param1 (label varchar(50), param1 double)
as
select c.label, max(s.param1) param1
from tmp_param_f1 s 
inner join cv_best_svm c 
    on c.label = s.label
    and c.corpus_name = 'i2b2.2008'
    and s.f1 >= c.f1
    and c.experiment = 'word-posthoc'
group by c.label
;

update cv_best_svm b inner join cv_param1 p on b.label = p.label set b.param1 = p.param1
where b.experiment = 'word-posthoc' and corpus_name = 'i2b2.2008'
;

/*
* get the best lcs cutoff
* only applicable for superlin
* for others, nothing will be done here
*/
drop table if exists cv_param2;
create temporary table cv_param2 (label varchar(50), param2 char(5))
as
select c.label, max(cast(s.param2 as decimal(4,3))) param2
from tmp_param_f1 s 
inner join cv_best_svm c 
    on c.label = s.label 
    and c.corpus_name = 'i2b2.2008'
    and c.experiment = 'word-posthoc'
    and s.f1 >= c.f1
where s.param2 <> ''
group by c.label
;

update cv_best_svm b inner join cv_param2 p on b.label = p.label set b.param2 = p.param2
where b.experiment = 'word-posthoc' and corpus_name = 'i2b2.2008'
;


/*
* get the best f1
*/
drop table if exists cv_cost;
create temporary table cv_cost (label varchar(50), cost double)
select s.label, min(s.cost) cost
from tmp_param_f1 s 
inner join cv_best_svm c 
    on c.label = s.label 
    and s.param1 = c.param1
    and s.param2 = coalesce(c.param2, '')
    and c.experiment = 'word-posthoc'
    and c.corpus_name = 'i2b2.2008'
    and s.f1 >= c.f1
group by s.label;

update cv_best_svm b inner join cv_cost p on b.label = p.label set b.cost = p.cost
where b.experiment = 'word-posthoc' and corpus_name = 'i2b2.2008'
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
            select ir_class, sum(tp) tp, sum(fp) fp, sum(tn) tn, sum(fn) fn
            from cv_best_svm b
            inner join classifier_eval e 
                on b.experiment = e.experiment 
                and b.corpus_name = e.name
                and b.label = e.label
                and b.param1 = e.param1
            inner join classifier_eval_svm s
                on s.classifier_eval_id = e.classifier_eval_id
                and s.cost = b.cost
            inner join classifier_eval_ir ir
                on ir.classifier_eval_id = e.classifier_eval_id
                and ir.ir_type = 'zv'
            where b.corpus_name = 'i2b2.2008'
                and b.experiment = 'word-posthoc'
            group by ir_class
        ) s 
    ) s
) s;                


