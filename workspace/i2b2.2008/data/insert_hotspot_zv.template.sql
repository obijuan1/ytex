/**
 * script to insert zero vectors for a given experiment and cutoff
 */
delete from hotspot_zero_vector 
where corpus_name = '@corpus_name@'
and experiment = '@experiment@'
and cutoff = @export.cutoff@
;

/**
 * insert zero vectors based on the specified cutoff
 * export.cutoff will be replaced with a number
 */
insert into hotspot_zero_vector (corpus_name, experiment, label, instance_id, cutoff)
select '@corpus_name@', '@experiment@', disease, docId, @export.cutoff@
from
(
	select a.disease, d.docId
	from i2b2_2008_doc d
	/* get disease classifications for documents */
	inner join i2b2_2008_anno a 
		on d.docId = a.docId 
		and a.source = 'intuitive'
	/* get instances for the specified experiment */
	left join hotspot_instance hi 
		on hi.instance_id = d.docId
		and corpus_name = '@corpus_name@'
		and experiment = '@experiment@'
		and a.disease = hi.label
	/* get # features for specified cutoff */
	left join
	(
		select hotspot_instance_id, count(*) fc
		from hotspot_feature_eval e on hi.hotspot_instance_id = e.hotspot_instance_id
		where e.evaluation > @export.cutoff@
		group by hotspot_instance_id
	) hfe on hfe.hotspot_instance_id = hi.hotspot_instance_id
	/* it's a zero vector if there are no features */
	where coalesce(fc, 0) = 0
) s
;