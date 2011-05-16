drop table hmls.tfidf_termfreq;
drop table hmls.tfidf_doclength;
drop table hmls.tfidf_docfreq;

create table $(db_schema).tfidf_doclength (
  tfidf_doclength_id int identity not null primary key,
  name varchar(255) not null default '',
  instance_id int not null,
  length int not null default 0
);
create unique index NK_doclength on $(db_schema).tfidf_doclength (name, instance_id);

create table $(db_schema).tfidf_docfreq (
  tfidf_docfreq_id int identity not null primary key,
  name varchar(255) not null default '',
  term varchar(50) not null,
  numdocs int not null default 0
)
create unique index NK_docfreq on $(db_schema).tfidf_docfreq (name, term);

create table hmls.tfidf_termfreq (
  tfidf_termfreq_id int identity primary key,
  name varchar(50) not null,
  instance_id int not null,
  term varchar(50) not null,
  freq int not null,
);
create unique index NK_tfidif_termfreq on hmls.tfidf_termfreq (name, instance_id, term);
create index IX_instance on hmls.tfidf_termfreq(name, instance_id);