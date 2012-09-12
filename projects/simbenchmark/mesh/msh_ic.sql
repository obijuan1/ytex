drop table if exists pub_msh_ic;
create table pub_msh_ic
as
select header, max(freq) freq, max(intrinsic_ic) intrinsic_ic, max(corpus_ic) corpus_ic
from
(
    select code header, sum(freq) freq, 0 intrinsic_ic, 0 corpus_ic
    from mesh_freq
    where code is not null and freq > 0
    group by code

    union 

    select feature_name, 0, evaluation, 0
    from feature_rank
    where feature_eval_id = 83

    union 

    select feature_name, 0, 0, evaluation
    from feature_rank
    where feature_eval_id = 94
)
s
group by header
;

drop table if exists pub_msh_pref_ic;
create table pub_msh_pref_ic
as
select cui, max(freq) freq, max(intrinsic_ic) intrinsic_ic, max(corpus_ic) corpus_ic
from
(
    select cui, sum(freq) freq, 0 intrinsic_ic, 0 corpus_ic
    from mesh_freq
    where cui is not null and freq > 0
    group by cui

    union

    select feature_name, 0, evaluation, 0
    from feature_rank
    where feature_eval_id = 84

    union

    select feature_name, 0, 0, evaluation
    from feature_rank
    where feature_eval_id = 95
) s
group by cui
;



drop table if exists pub_msh_all_ic;
create table pub_msh_all_ic
as
select cui, max(freq) freq, max(intrinsic_ic) intrinsic_ic, max(corpus_ic) corpus_ic
from
(
    select c.conceptUMLSUI cui, freq, 0 intrinsic_ic, 0 corpus_ic
    from
    (
        select code, sum(freq) freq
        from mesh_freq
        where code is not null
        group by code
    ) f
    inner join mesh_concept c on c.descriptorUI = f.code

    union

    select feature_name, 0, evaluation, 0
    from feature_rank
    where feature_eval_id = 84

    union

    select feature_name, 0, 0, evaluation
    from feature_rank
    where feature_eval_id = 96
) s
group by cui
;


