drop table if exists cv_best ;
create temporary table cv_best (
    label varchar(50) primary key,
    f1 double,
    experiment varchar(50),
    kernel int,
    cost double,
    weight varchar(50),
    param1 double,
    param2 varchar(50)
) engine = myisam;

/*
* get the best f1-score
*/
insert into cv_best (label, f1)
select label, truncate(max(f1),3) f1
from
(
	/*
	 * best f1 score by experiment (hotspot cutoff) and svm parameters
	 */
	select label, experiment, kernel, cost, gamma, weight, param1, param2, avg(f1) f1
	from classifier_eval_irzv t
	inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
	inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
	where name = 'i2b2.2008'
  and experiment in ('bag-cuiword', 'bag-cui', 'kern-cuiword-filteredlin')
	group by label, experiment, kernel, cost, gamma, weight, param1, param2
) s
group by label
order by cast(label as decimal(2,0));

/*
* get the best experiment
*/
drop table if exists cv_exp;
create temporary table cv_exp (label varchar(50), experiment varchar(50));
insert into cv_exp
select label, 
    /* convert back to name for sorting */
    case
        when exp = 1 then 'bag-cui' 
        when exp = 2 then 'bag-cuiword'
        else 'kern-cuiword-filteredlin'
    end exp
from
(
    select c.label, min(exp) exp
    from
    (
        select label, 
        /* convert to number for sorting */
        case
            when experiment = 'bag-cui' then 1
            when experiment = 'bag-cuiword' then 2
            else 3
        end exp
        , kernel, cost, gamma, weight, param1, param2, avg(f1) f1
        from classifier_eval_irzv t
        inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
        inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
        where name = 'i2b2.2008'
      and experiment in ('bag-cuiword', 'bag-cui', 'kern-cuiword-filteredlin')
        group by label, experiment, kernel, cost, gamma, weight, param1, param2
    ) s 
    inner join cv_best c 
        on c.label = s.label 
        and s.f1 >= c.f1 
    group by c.label
) s
;
update cv_best b inner join cv_exp p on b.label = p.label set b.experiment = p.experiment;


/*
* get the best hotspot cutoff (param1)
*/
drop table if exists cv_param1;
create temporary table cv_param1 (label varchar(50), param1 double);
insert into cv_param1
select c.label, max(s.param1)
from
(
	select label, experiment, kernel, cost, gamma, weight, param1, param2, avg(f1) f1
	from classifier_eval_irzv t
	inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
	inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
	where name = 'i2b2.2008'
  and experiment in ('bag-cuiword', 'bag-cui', 'kern-cuiword-filteredlin')
	group by label, experiment, kernel, cost, gamma, weight, param1, param2
) s 
inner join cv_best c 
    on c.label = s.label 
    and c.experiment = s.experiment
    and s.f1 >= c.f1
group by c.label
;

update cv_best b inner join cv_param1 p on b.label = p.label set b.param1 = p.param1;


/*
* get the best filter cutoff (param2), applicable only for filteredlin
*/
drop table if exists cv_fil;
create temporary table cv_fil (label varchar(50), param2 double);
insert into cv_fil
select c.label, max(s.param2)
from
(
	select label, experiment, kernel, cost, gamma, weight, param1, cast(param2 as decimal(4,3)) param2, avg(f1) f1
	from classifier_eval_irzv t
	inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
	inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
	where name = 'i2b2.2008'
  and experiment in ('kern-cuiword-filteredlin')
	group by label, experiment, kernel, cost, gamma, weight, param1, param2
) s 
inner join cv_best c 
    on c.label = s.label 
    and c.param1 = s.param1
    and c.experiment = s.experiment
    and s.f1 >= c.f1 
group by c.label
;
update cv_best b inner join cv_fil p on b.label = p.label set b.param2 = p.param2;

/*
* get the best cost
*/

drop table if exists cv_cost;
create temporary table cv_cost (label varchar(50), cost double);
insert into cv_cost
select c.label, min(s.cost)
from
(
	select label, experiment, kernel, cost, gamma, weight, param1, param2, avg(f1) f1
	from classifier_eval_irzv t
	inner join classifier_eval e on e.classifier_eval_id = t.classifier_eval_id
	inner join classifier_eval_svm l on e.classifier_eval_id = l.classifier_eval_id
	where name = 'i2b2.2008'
  and experiment in ('bag-cuiword', 'bag-cui', 'kern-cuiword-filteredlin')
	group by label, experiment, kernel, cost, gamma, weight, param1, param2
) s 
inner join cv_best c 
    on c.label = s.label 
    and c.param1 = s.param1
    and c.experiment = s.experiment
    and s.f1 >= c.f1 
group by c.label
;
update cv_best b inner join cv_cost p on b.label = p.label set b.cost = p.cost;

update cv_best set kernel=0;
update cv_best set kernel=4 where experiment like 'kern-%';

-- select * from cv_best;



select cast(concat('label.', label, '.kernel.experiment=', experiment) as char(200))
from cv_best
;
select cast(concat('label.', label, '.kernel.types=', kernel)as char(200))
from cv_best
;
select cast(concat('label.', label, '.cv.costs=', cost)as char(200))
from cv_best
;
select cast(concat('label.', label, '.kernel.param1=', param1)as char(200))
from cv_best
;
select cast(concat('label.', label, '.kernel.param2=', param2)as char(200))
from cv_best
where param2 is not null
;
