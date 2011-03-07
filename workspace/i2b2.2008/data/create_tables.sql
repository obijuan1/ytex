drop table if exists i2b2_2008_anno;
drop table if exists i2b2_2008_doc;

create table i2b2_2008_doc (
  docId int primary key not null,
  docText longtext,
  documentSet varchar(50) comment 'train/test'
);

create table i2b2_2008_anno (
  i2b2_2008_anno_id int auto_increment primary key not null,
  source varchar(100) not null comment 'textual/intuitive',
  disease varchar(100) not null,
  docId int not null comment 'fk i2b2_2008_doc',
  judgement char(1) not null
);

create index idx_docId on i2b2_2008_anno(docId);
create unique index idx_anno on i2b2_2008_anno(source, disease, docId);
