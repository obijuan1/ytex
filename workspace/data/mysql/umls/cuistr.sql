drop table if exists cuistr;
create table cuistr (
  cui char(8) primary key,
  str varchar(255),
  index IX_str(str)
) engine=myisam;

insert into cuistr
select cui, substring(str, 1, 255)
from umls.mrconso
where tty = 'PN';

drop table if exists tmpcuistr;

create temporary table tmpcuistr ( cui char(8), str varchar(255));

insert into tmpcuistr
select m.cui, substring(m.str, 1, 255)
from umls.mrconso m
left join cuistr c on m.cui = c.cui
where c.str is null
;

create index IX_cuistr on tmpcuistr(cui, str);