-- create temporary table to hold feature_rank records
drop TABLE if exists tmp_ic
;
create table tmp_ic(
  feature_name varchar(50) not null,
  evaluation double not null,
  rank int not null
);
