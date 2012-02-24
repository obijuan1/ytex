CREATE TABLE document(
	document_id int  AUTO_INCREMENT  NOT NULL,
	uid bigint not null default 0,
	analysis_batch varchar(50) NOT NULL,
	cas longblob NULL,
	doc_text text NULL,
	CONSTRAINT PK_document PRIMARY KEY
	(
		document_id
	)
) engine=myisam
;

CREATE INDEX IX_document_analysis_batch ON document 
(
	analysis_batch,
	document_id
)
;

CREATE INDEX IX_uid ON document 
(
	uid
)
;

create table anno_base (
	anno_base_id int auto_increment not null,
	document_id int not null  comment 'fk document',
	span_begin int,
	span_end int,
	uima_type_id int not null comment 'fk ref_uima_type',
	covered_text varchar(100) null,
	primary key (anno_base_id)
)engine=myisam
;

ALTER TABLE `anno_base` 
	ADD INDEX `IX_type_span`(`document_id`, `span_begin`, `span_end`, `uima_type_id`),
	ADD INDEX `IX_type`(`document_id`, `uima_type_id`);
 
CREATE INDEX IX_docanno_doc ON anno_base (document_id)
;

CREATE INDEX IX_covered_text ON anno_base (covered_text)
;

create table anno_sentence (
	anno_base_id int not null comment 'fk anno_base',
	sentence_number int,
	primary key (anno_base_id)
)engine=myisam;

create table anno_named_entity (
	anno_base_id int not null comment 'fk anno_base',
	discovery_technique int,
	status int,
	certainty int,
	type_id int,
	confidence float,
	segment_id varchar(64),
	primary key (anno_base_id)
)engine=myisam;

create table anno_ontology_concept (
	anno_ontology_concept_id int auto_increment not null,
	anno_base_id int not null comment 'fk anno_base',
	coding_scheme varchar(20),
	code varchar(20),
	oid varchar(10),
	primary key (anno_ontology_concept_id),
	KEY `IX_anno_base_id` (`anno_base_id`),
	KEY `IX_code` (`code`),
	KEY `IX_anno_code` (`anno_base_id`,`code`)
)engine=myisam;

create table anno_umls_concept (
	anno_ontology_concept_id int not null comment 'fk anno_ontology_concept',
	cui varchar(10),
	primary key (anno_ontology_concept_id)
) engine=myisam;

CREATE INDEX IX_umls_concept_cui ON anno_umls_concept (cui)
;

CREATE TABLE anno_segment(
	anno_base_id int NOT NULL  comment 'fk anno_base',
	segment_id varchar(50) NULL,
PRIMARY KEY
(
	anno_base_id ASC
)
)engine=myisam
;

CREATE INDEX IX_segment_anno_seg ON anno_segment
(
	anno_base_id ASC,
	segment_id ASC
)
;

-- mapped to SourceDocumentInformation
create table anno_source_doc_info (
	anno_base_id int NOT NULL comment 'fk anno_base',
	uri varchar(256),
	offset_in_source int,
	document_size int,
	last_segment bit,
	PRIMARY KEY
	(
		anno_base_id ASC
	)
)engine=myisam;


-- mapped to BaseToken
create table anno_base_token (
	anno_base_id int NOT NULL comment 'fk anno_base',
	token_number int,
	normalized_form varchar(256),
	part_of_speech varchar(5),
	PRIMARY KEY
	(
		anno_base_id ASC
	)
) engine=myisam;


-- mapped to NumToken
create table anno_num_token (
	anno_base_id int NOT NULL  comment 'fk anno_base',
	num_type int,
	PRIMARY KEY
	(
		anno_base_id ASC
	)
) engine=myisam;

-- mapped to WordToken
create table anno_word_token (
	anno_base_id int NOT NULL  comment 'fk anno_base',
	capitalization int,
	num_position int,
	suggestion int,
	canonical_form varchar(256),
	negated bit not null default 0,
	possible bit not null default 0,
	PRIMARY KEY
	(
		anno_base_id ASC
	),
	KEY `IX_canonical_form` (`canonical_form`),
	KEY `IX_anno_canonical_form` (`anno_base_id`,`canonical_form`)
) engine=myisam;

create table anno_date (
	anno_base_id int not null  comment 'fk anno_base',
	tstamp datetime,
	primary key (anno_base_id) 
) engine=myisam;

create table anno_contain (
  parent_anno_base_id int not null comment 'parent anno fk anno_base',
  parent_uima_type_id int not null comment 'parent type',
  child_anno_base_id int not null comment 'child anno fk anno_base',
  child_uima_type_id int not null comment 'child type',
  primary key (parent_anno_base_id, child_anno_base_id),
  key IX_parent_id_child_type (parent_anno_base_id, child_uima_type_id),
  key IX_child_id_parent_type (child_anno_base_id, parent_uima_type_id)
) engine=myisam, comment 'containment relationships between annotations';


CREATE TABLE fracture_demo(
	note_id int auto_increment NOT NULL primary key,
	site_id varchar(10) NULL,
	note_text text NULL,
	fracture varchar(20) NULL,
	note_set varchar(10) NULL
) engine=myisam, comment 'demo data';
