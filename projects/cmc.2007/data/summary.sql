-- get all the micro and macro f1s

-- get micro-averaged f1 across all labels
select 'bow-metamap-wsd' experiment, 'micro' typ, round(f1, 6) f1, round(ppv, 6) ppv, round(sens, 6) sens, denom
from
(
    select *, if(ppv+sens > 0, 2*ppv*sens/(ppv+sens), 0) f1
    from
    (
        select *, if(tp+fp > 0, tp/(tp+fp), 0) ppv, if(tp+fn >0, tp/(tp+fn), 0) sens, tp+fp+fn denom
        from
        (
            select sum(tp) tp, sum(fp) fp, sum(tn) tn, sum(fn) fn
            from classifier_eval e
            inner join classifier_eval_ir i on e.classifier_eval_id = i.classifier_eval_id
			where experiment = 'bow-metamap-wsd-test'
			and name = 'cmc.2007'
            and ir_class_id = '1'
        ) s 
    ) s
) s

union

-- get macro-averaged f1 across all labels
select 'bow-metamap-wsd', 'macro', round(avg(f1),6) f1, round(avg(ppv),6) ppv, round(avg(sens),6) sens, sum(tp+fp+fn) denom
from classifier_eval e
inner join classifier_eval_ir i on e.classifier_eval_id = i.classifier_eval_id
where experiment = 'bow-metamap-wsd-test'
and name = 'cmc.2007'
and ir_class_id = '1'

union

-- get micro-averaged f1 across all labels
select 'bow-metamap', 'micro' typ, round(f1, 6) f1, round(ppv, 6) ppv, round(sens, 6) sens, denom
from
(
    select *, if(ppv+sens > 0, 2*ppv*sens/(ppv+sens), 0) f1
    from
    (
        select *, if(tp+fp > 0, tp/(tp+fp), 0) ppv, if(tp+fn >0, tp/(tp+fn), 0) sens, tp+fp+fn denom
        from
        (
            select sum(tp) tp, sum(fp) fp, sum(tn) tn, sum(fn) fn
            from classifier_eval e
            inner join classifier_eval_ir i on e.classifier_eval_id = i.classifier_eval_id
			where experiment = 'bow-metamap-test'
			and name = 'cmc.2007'
            and ir_class_id = '1'
        ) s 
    ) s
) s

union

-- get macro-averaged f1 across all labels
select 'bow-metamap', 'macro', round(avg(f1),6) f1, round(avg(ppv),6) ppv, round(avg(sens),6) sens, sum(tp+fp+fn) denom
from classifier_eval e
inner join classifier_eval_ir i on e.classifier_eval_id = i.classifier_eval_id
where experiment = 'bow-metamap-test'
and name = 'cmc.2007'
and ir_class_id = '1'

union

-- get micro-averaged f1 across all labels
select 'bow-ctakes-wsd' experiment, 'micro' typ, round(f1, 6) f1, round(ppv, 6) ppv, round(sens, 6) sens, denom
from
(
    select *, if(ppv+sens > 0, 2*ppv*sens/(ppv+sens), 0) f1
    from
    (
        select *, if(tp+fp > 0, tp/(tp+fp), 0) ppv, if(tp+fn >0, tp/(tp+fn), 0) sens, tp+fp+fn denom
        from
        (
            select sum(tp) tp, sum(fp) fp, sum(tn) tn, sum(fn) fn
            from classifier_eval e
            inner join classifier_eval_ir i on e.classifier_eval_id = i.classifier_eval_id
			where experiment = 'bow-ctakes-wsd-test'
			and name = 'cmc.2007'
            and ir_class_id = '1'
        ) s 
    ) s
) s

union

-- get macro-averaged f1 across all labels
select 'bow-ctakes-wsd', 'macro', round(avg(f1),6) f1, round(avg(ppv),6) ppv, round(avg(sens),6) sens, sum(tp+fp+fn) denom
from classifier_eval e
inner join classifier_eval_ir i on e.classifier_eval_id = i.classifier_eval_id
where experiment = 'bow-ctakes-wsd-test'
and name = 'cmc.2007'
and ir_class_id = '1'

union

-- get micro-averaged f1 across all labels
select 'bow-ctakes', 'micro' typ, round(f1, 6) f1, round(ppv, 6) ppv, round(sens, 6) sens, denom
from
(
    select *, if(ppv+sens > 0, 2*ppv*sens/(ppv+sens), 0) f1
    from
    (
        select *, if(tp+fp > 0, tp/(tp+fp), 0) ppv, if(tp+fn >0, tp/(tp+fn), 0) sens, tp+fp+fn denom
        from
        (
            select sum(tp) tp, sum(fp) fp, sum(tn) tn, sum(fn) fn
            from classifier_eval e
            inner join classifier_eval_ir i on e.classifier_eval_id = i.classifier_eval_id
			where experiment = 'bow-ctakes-test'
			and name = 'cmc.2007'
            and ir_class_id = '1'
        ) s 
    ) s
) s

union

-- get macro-averaged f1 across all labels
select 'bow-ctakes', 'macro', round(avg(f1),6) f1, round(avg(ppv),6) ppv, round(avg(sens),6) sens, sum(tp+fp+fn) denom
from classifier_eval e
inner join classifier_eval_ir i on e.classifier_eval_id = i.classifier_eval_id
where experiment = 'bow-ctakes-test'
and name = 'cmc.2007'
and ir_class_id = '1'
;

-- get number of distinct concepts per NER system
select analysis_batch, count(distinct c.code)
from document d
/* get sections */
inner join anno_base ab 
    on ab.document_id = d.document_id
inner join anno_segment s
    on s.anno_base_id = ab.anno_base_id
    and s.id  in ('CMC_HISTORY', 'CMC_IMPRESSION')
/* get section concepts */
inner join anno_contain ac 
    on ac.parent_anno_base_id = ab.anno_base_id
inner join anno_ontology_concept c 
    on c.anno_base_id = ac.child_anno_base_id
where d.analysis_batch in ('cmc.2007-ctakes', 'cmc.2007-metamap')
group by analysis_batch
;

-- get number of distinct disambiguated concepts per NER system
select analysis_batch, count(distinct c.code)
from document d
/* get sections */
inner join anno_base ab 
    on ab.document_id = d.document_id
inner join anno_segment s
    on s.anno_base_id = ab.anno_base_id
    and s.id  in ('CMC_HISTORY', 'CMC_IMPRESSION')
/* get section concepts */
inner join anno_contain ac 
    on ac.parent_anno_base_id = ab.anno_base_id
inner join anno_ontology_concept c 
    on c.anno_base_id = ac.child_anno_base_id
    and c.disambiguated = 1
where d.analysis_batch in ('cmc.2007-ctakes', 'cmc.2007-metamap')
group by analysis_batch
;