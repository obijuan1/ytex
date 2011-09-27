drop table if exists i2b2_2008_disease;
create table i2b2_2008_disease (
  disease_id int auto_increment not null primary key,
  disease varchar(100),
  unique key IX_disease (disease)
);

insert into i2b2_2008_disease (disease)
select distinct disease
from i2b2_2008_anno
order by disease;

drop table if exists i2b2_2008_judgement;
create table i2b2_2008_judgement (
  judgement_id int not null primary key,
  judgement char(1) not null,
  unique key IX_judgement (judgement)
);

insert into i2b2_2008_judgement (judgement_id, judgement) values (0, 'N');
insert into i2b2_2008_judgement (judgement_id, judgement) values (1, 'Y');
insert into i2b2_2008_judgement (judgement_id, judgement) values (2, 'Q');
insert into i2b2_2008_judgement (judgement_id, judgement) values (3, 'U');
