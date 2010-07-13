create table ref_named_entity_regex (
	named_entity_regex_id int NOT NULL,
	regex varchar2(256) not null,
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
	primary key (segment_regex_id)
) ;

create table ref_uima_type (
	uima_type_id int not null,
	uima_type_name varchar2(256) not null,
	mapper_name varchar2(256) not null,
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
