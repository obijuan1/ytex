create sequence document_id_sequence;
create sequence anno_base_id_sequence;
create sequence anno_onto_concept_id_sequence;
create sequence anno_contain_id_sequence;
create sequence demo_note_id_sequence;

CREATE TABLE document(
	document_id int  NOT NULL,
	"uid" NUMBER(19) default 0 not null,
	analysis_batch varchar2(50) default ' ' NOT NULL,
	cas blob NULL,
	doc_text clob NULL,
	CONSTRAINT PK_document PRIMARY KEY
	(
		document_id
	)	
) 
;

CREATE INDEX IX_document_analysis_batch ON document 
(
	analysis_batch,
	document_id
)
;

CREATE INDEX IX_uid ON document 
(
	"uid"
)
;


create table anno_base (
	anno_base_id int  not null,
	document_id int not null,
	span_begin int,
	span_end int,
	uima_type_id int not null,
	covered_text varchar2(100) null,
	primary key (anno_base_id),
	foreign key (document_id) references document (document_id) ON DELETE CASCADE,
	foreign key (uima_type_id) references ref_uima_type (uima_type_id)
)
;

CREATE INDEX IX_docanno_doc ON anno_base (document_id)
;

CREATE INDEX IX_covered_text ON anno_base (covered_text)
;

create table anno_sentence (
	anno_base_id int not null,
	sentence_number int,
	primary key (anno_base_id),
	foreign key (anno_base_id) references anno_base(anno_base_id)  ON DELETE CASCADE
);

create table anno_named_entity (
	anno_base_id int not null,
	discovery_technique int,
	status int,
	certainty int,
	type_id int,
	confidence float,
	segment_id varchar2(64),
	primary key (anno_base_id),
	foreign key (anno_base_id) references anno_base(anno_base_id)  ON DELETE CASCADE
);

create table anno_ontology_concept (
	anno_ontology_concept_id int  not null,
	anno_base_id int not null,
	coding_scheme varchar2(20),
	code varchar2(20),
	oid varchar2(10),
	primary key (anno_ontology_concept_id),
	foreign key (anno_base_id) references anno_named_entity(anno_base_id)  ON DELETE CASCADE
);

CREATE INDEX IX_ontology_concept_code ON anno_ontology_concept (code)
;


create table anno_umls_concept (
	anno_ontology_concept_id int not null,
	cui varchar2(10),
	primary key (anno_ontology_concept_id),
	foreign key (anno_ontology_concept_id) references anno_ontology_concept(anno_ontology_concept_id)  ON DELETE CASCADE
) ;

CREATE INDEX IX_umls_concept_cui ON anno_umls_concept (cui)
;

CREATE TABLE anno_segment(
	anno_base_id int NOT NULL,
	segment_id varchar2(50) NULL,
	PRIMARY KEY (anno_base_id),
	foreign key (anno_base_id) references anno_base(anno_base_id)  ON DELETE CASCADE
)
;


CREATE INDEX IX_segment_anno_seg ON anno_segment
(
	anno_base_id ,
	segment_id 
)
;

-- mapped to SourceDocumentInformation
create table anno_source_doc_info (
	anno_base_id int NOT NULL,
	uri varchar2(256),
	offset_in_source int,
	document_size int,
	last_segment numeric(1) default 0 check (last_segment between 0 and 1),
	PRIMARY KEY
	(
		anno_base_id 
	),
	foreign key (anno_base_id) references anno_base(anno_base_id)  ON DELETE CASCADE
);


-- mapped to BaseToken
create table anno_base_token (
	anno_base_id int NOT NULL,
	token_number int,
	normalized_form varchar2(256),
	part_of_speech varchar2(5),
	PRIMARY KEY
	(
		anno_base_id 
	),
	foreign key (anno_base_id)
		references anno_base(anno_base_id)
		ON DELETE CASCADE
) ;


-- mapped to NumToken
create table anno_num_token (
	anno_base_id int NOT NULL,
	num_type int,
	PRIMARY KEY
	(
		anno_base_id 
	),
	foreign key (anno_base_id)
		references anno_base_token(anno_base_id)
		ON DELETE CASCADE
) ;

-- mapped to WordToken
create table anno_word_token (
	anno_base_id int NOT NULL,
	capitalization int,
	num_position int,
	suggestion int,
	canonical_form varchar2(256),
	negated numeric(1) default 0 check (negated between 0 and 1),
	possible numeric(1) default 0 check (possible between 0 and 1),
	PRIMARY KEY
	(
		anno_base_id 
	),
	foreign key (anno_base_id)
		references anno_base_token(anno_base_id)
		ON DELETE CASCADE
) ;

create index IX_canonical_form on anno_word_token(canonical_form);

create table anno_date (
	anno_base_id int not null,
	tstamp timestamp,
	primary key (anno_base_id),
	foreign key (anno_base_id) references anno_base(anno_base_id) ON DELETE CASCADE 
) ;

create table anno_contain (
  parent_anno_base_id int not null,
  parent_uima_type_id int not null,
  child_anno_base_id int not null,
  child_uima_type_id int not null,
  primary key (parent_anno_base_id, child_anno_base_id),
  foreign key (parent_anno_base_id)
		references anno_base(anno_base_id)
		ON DELETE CASCADE
);

CREATE INDEX IX_anno_contain_p ON anno_contain (parent_anno_base_id, child_uima_type_id)
;

CREATE INDEX IX_anno_contain_c ON anno_contain (child_anno_base_id, parent_uima_type_id)
;


CREATE TABLE fracture_demo (
	note_id int NOT NULL primary key,
	site_id varchar(10) NULL,
	note_text clob NULL,
	fracture varchar2(20) NULL,
	note_set varchar2(10) NULL
);

