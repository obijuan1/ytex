/**
 * for some hotspot cutoffs + folds, we may be missing entire classes
 * add the missing truth tables
 */
drop table if exists tmp_missing_ir;
create temporary table tmp_missing_ir
as
select fc.*, e.classifier_eval_id, e.param1
from i2b2_2008_fold_class fc
inner join cv_fold f 
    on f.label = fc.disease 
    and f.cv_fold_id = fc.cv_fold_id
    and f.corpus_name = '@kernel.name@'
inner join classifier_eval e
    on e.name = '@kernel.name@'
    and e.experiment = '@kernel.experiment@'
    and e.label = fc.disease_id
    and e.run = f.run
    and e.fold = f.fold
/* filter out the class ids for which we have the ir metrics */
left join classifier_eval_ir ir 
    on ir.classifier_eval_id = e.classifier_eval_id 
    and ir.ir_class_id = fc.judgement_id
where ir.classifier_eval_ir_id is null
;

create unique index NK_tmp_missing_ir on tmp_missing_ir (cv_fold_id, disease, disease_id, judgement_id, classifier_eval_id, param1);

insert into classifier_eval_ir (classifier_eval_id, ir_class_id, tp, tn, fn, fp)
select fc.classifier_eval_id, fc.judgement_id, 
    0 tp, 
    sum(j.judgement_id <> fc.judgement_id) tn,
    sum(j.judgement_id = fc.judgement_id) fn,
    0 fp
from
/* get fold, classifier evaluation, and missing class ids */
tmp_missing_ir fc 
/* get the test instances for this fold */
inner join cv_fold_instance fi 
    on fi.cv_fold_id = fc.cv_fold_id 
    and fi.train = 0
/* get the judgement for these instances */
inner join i2b2_2008_anno a 
    on a.docId = fi.instance_id 
    and a.source = 'intuitive' 
    and a.disease = fc.disease
inner join i2b2_2008_judgement j 
    on j.judgement = a.judgement
inner join hotspot_instance i 
    on i.experiment = '@kernel.hzv.experiment@' 
    and i.label = fc.disease 
    and i.instance_id = fi.instance_id
/* filter out zero vectors */
left join hotspot_zero_vector zv 
    on zv.hotspot_instance_id = i.hotspot_instance_id 
    and zv.cutoff = fc.param1
where zv.hotspot_zero_vector_id is null
group by fc.classifier_eval_id, fc.judgement_id
;


/*
 * combine zero vectors with classifier predictions 
 * for a 'complete' truth table w/ ir metrics
 */
delete classifier_eval_irzv
from classifier_eval_irzv
inner join classifier_eval e on classifier_eval_irzv.classifier_eval_id = e.classifier_eval_id
where e.experiment = '@kernel.experiment@'
and e.name = '@kernel.name@'
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
			and e.experiment = '@kernel.experiment@'
			and e.name = '@kernel.name@'
			and e.run > 0
			and e.fold > 0
		left join hotspot_zero_vector_tt z
			on (z.name, z.label, z.run, z.fold, z.ir_class_id, z.cutoff) = (e.name, e.label, e.run, e.fold, ir.ir_class_id, e.param1)
			and z.experiment = '@kernel.hzv.experiment@'
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
	    where name = '@kernel.name@' 
		and experiment = '@kernel.experiment@'
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
where e.experiment = '@kernel.experiment@'
and e.name = '@kernel.name@' 
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
	where experiment = '@kernel.experiment@'
	and name = '@kernel.name@'
	and run > 0
	and fold > 0
	group by label, kernel, cost, gamma, weight, param1, param2
) s
group by label
order by cast(label as decimal(2,0))
;