drop table if exists tmp_f1;
create temporary table tmp_f1(label varchar(50) primary key, f1 double);
    insert into tmp_f1
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
        where experiment = 'kern-cuiword-filteredlin'
        and name = 'i2b2.2008'
        group by label, kernel, cost, gamma, weight, param1, param2
    ) s
    group by label
;

select tmp_f1.label, tmp_f1.f1, min(param2), max(param2)
from
(
        select label, kernel, cost, gamma, weight, param1, param2, avg(f1) f1
        from classifier_eval_irzv t
        inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
        inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
        where experiment = 'kern-cuiword-filteredlin'
        and name = 'i2b2.2008'
        group by label, kernel, cost, gamma, weight, param1, param2
) savg
inner join tmp_f1 on savg.f1 >= tmp_f1.f1 and savg.label = tmp_f1.label
group by tmp_f1.label, tmp_f1.f1
order by cast(tmp_f1.label as decimal(2,0))
;
