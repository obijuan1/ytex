delete from hotspot_zero_vector where name = 'i2b2.2008-cui' and cutoff = @export.cutoff@;

/**
 * insert zero vectors based on the specified cutoff
 * export.cutoff will be replaced with a number
 */
insert into hotspot_zero_vector (name, label, instance_id, cutoff)
select 'i2b2.2008-cui', disease, docId, @export.cutoff@
from
(
	select a.disease, d.docId
	from i2b2_2008_doc d
	inner join i2b2_2008_anno a on d.docId = a.docId and a.source = 'intuitive'
	left join
	(
		select label, instance_id, count(*) fc
		from hotspot_feature_eval
		where evaluation > @export.cutoff@
		and name = 'i2b2.2008-cui'
		group by label, instance_id
	) h on d.docId = h.instance_id and a.disease = h.label
	where fc is null
) s
;