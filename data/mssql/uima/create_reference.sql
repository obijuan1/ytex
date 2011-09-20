create table $(db_schema).ref_named_entity_regex (
	named_entity_regex_id int IDENTITY(1,1) NOT NULL,
	regex varchar(512) not null,
	coding_scheme varchar(20) not null,
	code varchar(20) not null,
	oid varchar(10),
	context varchar(256),
	primary key (named_entity_regex_id)
);

create table $(db_schema).ref_segment_regex (
	segment_regex_id int IDENTITY(1,1) NOT NULL,
	regex varchar(256) not null,
	segment_id varchar(256),
	limit_to_regex bit null default 0, 
	primary key (segment_regex_id)
);

create table $(db_schema).ref_uima_type (
	uima_type_id int not null,
	uima_type_name varchar(256) not null,
	mapper_name varchar(256) not null,
	CONSTRAINT PK_ref_uima_type PRIMARY KEY  
	(
		uima_type_id ASC
	)
)
;

CREATE UNIQUE NONCLUSTERED INDEX NK_ref_uima_type ON $(db_schema).ref_uima_type
(
	uima_type_name
)
;
