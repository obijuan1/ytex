select d.disease, substring(experiment, 7) cutoff, zf1
from i2b2_2008_disease d
inner join
(
select label, experiment, max(zf1) zf1
from
(
select experiment, label, kernel, cost, gamma, weight, avg(f1) zf1
from classifier_eval_irzv t
inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
inner join classifier_eval_libsvm l on e.classifier_eval_id = l.classifier_eval_id
where experiment like 'bocuis%'
group by experiment, label, kernel, cost, gamma, weight
) s group by label, experiment
) s on s.label = d.disease_id
order by d.disease, substring(experiment, 7)
;