create sequence named_entity_regex_id_sequence;
create sequence segment_regex_id_sequence;
create sequence hibernate_sequence;

create table hibernate_sequences (
	sequence_name varchar2(100) not null,
	next_val int default 1 not null,
	primary key (sequence_name)
);
insert into hibernate_sequences(sequence_name, next_val) values ('document_id_sequence', 1);

create table anno_base_sequence (
	sequence_name varchar(100) not null,
	next_val int not null default 1,
	primary key (sequence_name)
);
insert into anno_base_sequence(sequence_name, next_val) values ('anno_base_id_sequence', 1);


create table ref_named_entity_regex (
	named_entity_regex_id int NOT NULL,
	regex varchar2(512) not null,
	coding_scheme varchar2(20) not null,
	code varchar2(20) not null,
	oid varchar2(10) null,
	context varchar2(256) null,
	primary key (named_entity_regex_id)
) ;

create table ref_segment_regex (
	segment_regex_id int  NOT NULL,
	regex varchar2(256) not null,
	segment_id varchar2(256) not null,
	limit_to_regex numeric(1) default 0 check (limit_to_regex between 0 and 1),
	primary key (segment_regex_id)
) ;

create table ref_uima_type (
	uima_type_id int not null,
	uima_type_name varchar2(256) not null,
	table_name varchar(100) null,
	CONSTRAINT PK_ref_uima_type PRIMARY KEY  
	(
		uima_type_id 
	)
) ;

CREATE UNIQUE  INDEX NK_ref_uima_type ON ref_uima_type
(
	uima_type_name
)
;

CREATE TABLE ref_stopword (
	stopword varchar(50) not null,
	constraint PK_ref_stopword primary key
	(
		stopword
	)
)
;
