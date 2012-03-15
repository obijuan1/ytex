-- create temporary table to hold feature_rank records
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'$(db_schema).tmp_ic') AND type in (N'U'))
	drop TABLE  $(db_schema).tmp_ic
;
create table $(db_schema).tmp_ic(
  feature_name varchar(50) not null ,
  evaluation float not null default 0 ,
  rank int not null default 0,
);
