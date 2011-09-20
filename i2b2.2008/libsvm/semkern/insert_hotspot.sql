drop table if exists hotspot_sentence;
create table hotspot_sentence (
  hotspot_sentence_id int auto_increment not null primary key,
  name varchar(50) not null,
  label varchar(50) not null,
  instance_id int not null,
  anno_base_id int not null comment 'sentence id',
  evaluation double,
  unique index NK_hotspot_sentence (name, label, anno_base_id),
  index NK_instance (name, label, instance_id)
) engine = myisam;

insert into hotspot_sentence (name, label, instance_id, anno_base_id, evaluation)
select 'i2b2.2008-cui', s.*
from
(
	select e.label, h.instance_id, ac.parent_anno_base_id, max(r.evaluation)
	from hotspot h
	/* limit to the feature sets we're working on */
	inner join feature_rank r on r.feature_rank_id = h.feature_rank_id
	inner join feature_eval e on e.feature_eval_id = r.feature_eval_id and e.name in ('i2b2.2008-ncuiword', 'i2b2.2008-cui')
	/* get sentence for hotspot */
	inner join anno_contain ac on ac.child_anno_base_id = h.anno_base_id and ac.parent_uima_type_id = 9
	/* testing
	where h.instance_id = 1
	*/
	group by e.label, ac.parent_anno_base_id
) s
;
