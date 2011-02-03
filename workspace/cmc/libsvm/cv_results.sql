

-- view avg results per parameter combination
select r.*, cc.ndocs
from
(
    select label, weight, cost, avg(F_measure) f1a, avg(scutFMeasure) scutF1, avg(measureNumSupportVectors) nsva
    from weka_results
    where experiment = 'cmc-cv-libsvm'
    group by label, weight, cost
) r
inner join
(
    select label, max(f1a) f1a
    from
    (
        select label, weight, cost, avg(F_measure) f1a
        from weka_results
	    where experiment = 'cmc-cv-libsvm'
        group by label, weight, cost
    ) s 
    group by label
) rm on rm.label = r.label and rm.f1a = r.f1a
inner join 
(
    select l.labelId, count(*) ndocs
    from CMCClassLabels l 
    inner join CMCDocumentCode c on l.code = c.code
    inner join CMCDocument d on d.documentId = c.documentId
    where d.documentSet = 'train'
    group by l.labelId
) cc on cc.labelId = r.label
order by label asc, nsva asc, cost desc, weight asc
;