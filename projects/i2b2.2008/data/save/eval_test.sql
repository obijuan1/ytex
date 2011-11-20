select d.disease, a.label, a.f1 word, b.f1 concept
from
(
select e.label, avg(f1) f1
from classifier_eval_irzv z
inner join classifier_eval e on z.classifier_eval_id = e.classifier_eval_id
where e.experiment = 'ambert-test'
group by e.label
) a
inner join
(
select e.label, avg(f1) f1
from classifier_eval_irzv z
inner join classifier_eval e on z.classifier_eval_id = e.classifier_eval_id
where e.experiment = 'bocuis-test'
group by e.label
) b on a.label = b.label
inner join i2b2_2008_disease d on d.disease_id = a.label
order by d.disease
;

