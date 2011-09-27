create table i2b2_2008_segment_map (
  segment_id varchar(50) not null primary key,
  segment_group varchar(50) not null
);
delete from i2b2_2008_segment_map;
LOAD DATA INFILE 'i2b2_2008_segment_map.txt' INTO TABLE i2b2_2008_segment_map lines terminated by '\r\n' ;
select * from i2b2_2008_segment_map;

alter table hotspot_sentence add column section varchar(50) not null default 'OTHER';

update hotspot_sentence hs 
inner join anno_contain c on c.child_anno_base_id = hs.anno_base_id and c.parent_uima_type_id = 12
inner join anno_segment seg on seg.anno_base_id = c.parent_anno_base_id
inner join i2b2_2008_segment_map m on m.segment_id = seg.segment_id
set hs.section = m.segment_group
;