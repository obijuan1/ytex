/**
 * script to insert zero vectors for a given kernel.experiment and cutoff
 */
delete z 
from hotspot_zero_vector z
inner join hotspot_instance i 
	on z.hotspot_instance_id = i.hotspot_instance_id
	and i.experiment = '@kernel.experiment@'
	and z.cutoff = @export.cutoff@
where i.corpus_name = '@kernel.name@'
;

/**
 * insert zero vectors based on the specified cutoff
 * export.cutoff will be replaced with a number
 */
insert into hotspot_zero_vector (hotspot_instance_id, cutoff)
select hotspot_instance_id, @export.cutoff@
from
(
	select hotspot_instance_id
	from
	(
		select hi.hotspot_instance_id, sum(hs.hotspot_sentence_id is not null) sc
		/* we have a hotspot_instance for every doc/label combo */
		from hotspot_instance hi
		/* get the sentences */
		left join hotspot_sentence hs
			on hs.hotspot_instance_id = hi.hotspot_instance_id
			and hs.evaluation >= @export.cutoff@
		where corpus_name = '@kernel.name@'
		and experiment = '@kernel.experiment@'
		group by hi.label, hi.instance_id	
	) s
	/* it's a zero vector if there are no sentences */
	where sc = 0
) s
;