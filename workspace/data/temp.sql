alter table anno_contain rename anno_contain_old;

create table anno_contain (
  anno_contain_id int auto_increment not null primary key,
  parent_anno_base_id int not null comment 'parent anno fk anno_base',
  parent_uima_type_id int not null comment 'parent type',
  child_anno_base_id int not null comment 'child anno fk anno_base',
  child_uima_type_id int not null comment 'child type',
  key ix_child_id (child_anno_base_id),
  key ix_parent_id (parent_anno_base_id),
  key IX_parent_id_child_type (parent_anno_base_id, child_uima_type_id),
  key IX_child_id_parent_type (child_anno_base_id, parent_uima_type_id),
  unique key nk_anno_contain (parent_anno_base_id, child_anno_base_id)
) engine=myisam, comment 'containment relationships between annotations';

insert into anno_contain (parent_anno_base_id, parent_uima_type_id, child_anno_base_id, child_uima_type_id)
select parent_anno_base_id, parent_uima_type_id, child_anno_base_id, child_uima_type_id
from anno_contain_old;

drop table anno_contain_old;
