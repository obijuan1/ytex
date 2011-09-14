/* get the macro-averaged f1 */
select avg(f1)
from
(
	select label, max(f1) f1
	from
	(
		select label, cost, avg(f1) f1
		from classifier_eval_ir t
		inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
		inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
		where name = 'cmc.2007'
		and experiment in ('kern-ctakes')
		and t.ir_class_id = 1
		group by label, cost
	) s group by label
) s
;
-- macro: 0.49
/* get the micro-averaged f1 */
select ppv, sens, 2*ppv*sens/(ppv+sens) f1
from
(
	select tp/(tp+fp) ppv, tp/(tp+fn) sens
	from
	(
		/* create grand truth table from all folds for the best cost */
		select sum(tp) tp, sum(tn) tn, sum(fp) fp, sum(fn) fn
		from
		(
			/* create grand truth table from all folds for each label with the best cost */
			select label, cost, sum(tp) tp, sum(tn) tn, sum(fp) fp, sum(fn) fn
			from classifier_eval_ir t
			inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
			inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
			where name = 'cmc.2007'
			and experiment in ('kern-ctakes')
			and t.ir_class_id = 1
			group by label, cost
		) m
		inner join
		(
			/* get the best cost for each label */
			select a.label, min(cost) cost
			from
			(
				/* get avg f1 for each label/cost */
				select label, cost, avg(f1) f1
				from classifier_eval_ir t
				inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
				inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
				where name = 'cmc.2007'
				and experiment in ('kern-ctakes')
				and t.ir_class_id = 1
				group by label, cost
			) a
			inner join
			(
				/* get best f1 for each label */
				select label, max(f1) f1
				from
				(
					/* get avg f1 for each label/cost */
					select label, cost, avg(f1) f1
					from classifier_eval_ir t
					inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
					inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
					where name = 'cmc.2007'
					and experiment in ('kern-ctakes')
					and t.ir_class_id = 1
					group by label, cost
				) sb
				group by label
			) b on b.label = a.label and b.f1 = a.f1
			group by a.label
		) b on m.label = b.label and m.cost = b.cost
	) s
) s
;
/*
 * 
ppv	sens	f1
0.8508, 0.7308, 0.786247647951
 */

/* get the best cost for each label */
select cast(concat('label.',a.label, '.cv.costs=', min(cost)) as char(100))
from
(
	/* get avg f1 for each label/cost */
	select label, cost, avg(f1) f1
	from classifier_eval_ir t
	inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
	inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
	where name = 'cmc.2007'
	and experiment in ('kern-ctakes')
	and t.ir_class_id = 1
	group by label, cost
) a
inner join
(
	/* get best f1 for each label */
	select label, max(f1) f1
	from
	(
		/* get avg f1 for each label/cost */
		select label, cost, avg(f1) f1
		from classifier_eval_ir t
		inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
		inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
		where name = 'cmc.2007'
		and experiment in ('kern-ctakes')
		and t.ir_class_id = 1
		group by label, cost
	) sb
	group by label
) b on b.label = a.label and b.f1 = a.f1
group by a.label
;