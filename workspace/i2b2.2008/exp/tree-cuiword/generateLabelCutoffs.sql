/*
 * script to generate label cutoff property file
 */
select cast(concat('label.', s.label, '.param1=', max(param1)) as char(50))
from
(
	select label, max(f1) f1
	from
	(
		/*
		 * best f1 score by experiment (hotspot cutoff) and svm parameters
		 */
		select label, kernel, cost, gamma, weight, param1, avg(f1) f1
		from classifier_eval_irzv t
		inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
		inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
    	where experiment = 'bag-cuiword'
    	and name = 'i2b2.2008'
		group by label, kernel, cost, gamma, weight, param1
	) s
	group by label
) s 
inner join
(
    /*
     * f1 score by label, experiment, hotspot cutoff, and svm parameters
     */
    select label, cost, weight, param1, avg(f1) f1
    from classifier_eval_irzv t
    inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
    inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
    where experiment = 'bag-cuiword'
    and name = 'i2b2.2008'
    group by label, cost, weight, param1
) e on s.label = e.label and (round(s.f1, 2)-0.1) <= round(e.f1,2)
group by e.label, s.f1
order by s.label;