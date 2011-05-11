-- best f1 for each experiment
select *
from
(
	select label, max(f1) f1, experiment
	from
	(
		/*
		 * best f1 score by experiment (hotspot cutoff) and svm parameters
		 */
		select experiment, label, kernel, cost, gamma, weight, avg(f1) f1
		from classifier_eval_irzv t
		inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
		inner join classifier_eval_libsvm l on e.classifier_eval_id = l.classifier_eval_id
		group by experiment, label, kernel, cost, gamma, weight
	) s
	group by label, experiment
) s order by label, f1 desc, experiment
;

-- best for each
select d.disease, s.*
from i2b2_2008_disease d
inner join
(
select label, sum(ambert) ambert, sum(bocuis) bocuis, sum(nekern) nekern, sum(semkern) semkern, sum(rbskern) rbskern
from
(
	select label, max(f1) ambert, 0 bocuis, 0 nekern, 0 semkern, 0 rbskern
	from
	(
		select label, max(f1) f1, experiment
		from
		(
			/*
			 * best f1 score by experiment (hotspot cutoff) and svm parameters
			 */
			select experiment, label, kernel, cost, gamma, weight, avg(f1) f1
			from classifier_eval_irzv t
			inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
			inner join classifier_eval_libsvm l on e.classifier_eval_id = l.classifier_eval_id
	    where experiment like 'ambert0%'
			group by experiment, label, kernel, cost, gamma, weight
		) s
		group by label, experiment
	) s
	group by label
	
	union
	
	select label, 0, max(f1) bocuis, 0, 0, 0
	from
	(
		select label, max(f1) f1, experiment
		from
		(
			/*
			 * best f1 score by experiment (hotspot cutoff) and svm parameters
			 */
			select experiment, label, kernel, cost, gamma, weight, avg(f1) f1
			from classifier_eval_irzv t
			inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
			inner join classifier_eval_libsvm l on e.classifier_eval_id = l.classifier_eval_id
	    where experiment like 'bocuis%'
			group by experiment, label, kernel, cost, gamma, weight
		) s
		group by label, experiment
	) s
	group by label
	
	union
	
	select label, 0, 0, max(f1) nekern_f1, 0, 0
	from
	(
		select label, max(f1) f1, experiment
		from
		(
			/*
			 * best f1 score by experiment (hotspot cutoff) and svm parameters
			 */
			select experiment, label, kernel, cost, gamma, weight, avg(f1) f1
			from classifier_eval_irzv t
			inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
			inner join classifier_eval_libsvm l on e.classifier_eval_id = l.classifier_eval_id
	    where experiment like 'nekern%'
			group by experiment, label, kernel, cost, gamma, weight
		) s
		group by label, experiment
	) s
	group by label
	
	union

	select label, 0, 0, 0, max(f1) semkern, 0
	from
	(
		select label, max(f1) f1, experiment
		from
		(
			/*
			 * best f1 score by experiment (hotspot cutoff) and svm parameters
			 */
			select experiment, label, kernel, cost, gamma, weight, avg(f1) f1
			from classifier_eval_irzv t
			inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
			inner join classifier_eval_libsvm l on e.classifier_eval_id = l.classifier_eval_id
	    where experiment like 'semkern%'
			group by experiment, label, kernel, cost, gamma, weight
		) s
		group by label, experiment
	) s
	group by label
	
	union
	
	select label, 0, 0, 0, 0, max(f1) rbskern
	from
	(
		select label, max(f1) f1, experiment
		from
		(
			/*
			 * best f1 score by experiment (hotspot cutoff) and svm parameters
			 */
			select experiment, label, kernel, cost, gamma, weight, avg(f1) f1
			from classifier_eval_irzv t
			inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
			inner join classifier_eval_libsvm l on e.classifier_eval_id = l.classifier_eval_id
	    where experiment like 'rbskern%'
			group by experiment, label, kernel, cost, gamma, weight
		) s
		group by label, experiment
	) s
	group by label
) s
group by label
) s on s.label = d.disease_id order by disease_id
;