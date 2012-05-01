-- create sequence document_id_sequence;
-- create sequence anno_base_id_sequence;
create sequence anno_onto_concept_id_sequence;
create sequence anno_contain_id_sequence;
create sequence anno_link_id_sequence;
create sequence demo_note_id_sequence;

CREATE TABLE document(
	document_id int  NOT NULL,
	instance_id NUMBER(19) default 0 not null,
	uimaDocumentID varchar(256) null,
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

CREATE INDEX IX_instance_id ON document 
(
	instance_id
)
;


create table anno_base (
	anno_base_id int  not null,
	document_id int not null,
	span_begin int,
	span_end int,
	uima_type_id int not null,
	primary key (anno_base_id),
	foreign key (document_id) references document (document_id) ON DELETE CASCADE,
	foreign key (uima_type_id) references ref_uima_type (uima_type_id)
)
;

CREATE INDEX IX_docanno_doc ON anno_base (document_id)
;


create table anno_sentence (
	anno_base_id int not null,
	sentenceNumber int,
	segmentId varchar2(20),
	primary key (anno_base_id),
	foreign key (anno_base_id) references anno_base(anno_base_id)  ON DELETE CASCADE
);

create table anno_named_entity (
	anno_base_id int not null,
	discoveryTechnique int,
	status int,
	certainty int,
	typeID int,
	confidence float,
	segmentID varchar2(20),
	primary key (anno_base_id),
	foreign key (anno_base_id) references anno_base(anno_base_id)  ON DELETE CASCADE
);

create table anno_ontology_concept (
	anno_ontology_concept_id int  not null,
	anno_base_id int not null,
	code varchar2(20),
	cui char(8),
	disambiguated numeric(1) default 0 not null,
	primary key (anno_ontology_concept_id),
	foreign key (anno_base_id) references anno_named_entity(anno_base_id)  ON DELETE CASCADE
);

CREATE INDEX IX_ontology_concept_code ON anno_ontology_concept (code)
;


CREATE TABLE anno_segment(
	anno_base_id int NOT NULL,
	id varchar2(20) NULL,
	PRIMARY KEY (anno_base_id),
	foreign key (anno_base_id) references anno_base(anno_base_id)  ON DELETE CASCADE
)
;


CREATE INDEX IX_segment_anno_seg ON anno_segment
(
	id 
)
;


-- mapped to BaseToken
create table anno_token (
	anno_base_id int NOT NULL,
	tokenNumber int,
	normalizedForm varchar2(20),
	partOfSpeech varchar2(5),
	coveredText varchar2(20) null,
	capitalization int default 0 not null,
	numPosition int default 0 not null,
	suggestion varchar2(20),
	canonicalForm varchar2(20),
	negated NUMERIC(1) default 0 not null,
	possible NUMERIC(1) default 0 not null,
	PRIMARY KEY
	(
		anno_base_id 
	),
	foreign key (anno_base_id)
		references anno_base(anno_base_id)
		ON DELETE CASCADE
) ;

create index IX_covered_text on anno_token(coveredText);
create index IX_canonical_form on anno_token(canonicalForm);

create table anno_date (
	anno_base_id int not null,
	tstamp timestamp,
	primary key (anno_base_id),
	foreign key (anno_base_id) references anno_base(anno_base_id) ON DELETE CASCADE 
) ;

create table anno_drug_mention (
	anno_base_id int not null,
	status int default 0 not null,
	frequency varchar2(20),
	duration varchar2(20),
	route varchar2(20),
	drugChangeStatus varchar2(10),
	dosage varchar2(20),
	strength varchar2(20),
	form varchar2(20),
	frequencyUnit varchar2(20),
	startDate varchar2(20),
	primary key (anno_base_id),
	foreign key (anno_base_id) references anno_base(anno_base_id) ON DELETE CASCADE 
);

create table anno_markable (
	anno_base_id int not null,
	id int default 0,
	anaphoric_prob double PRECISION default 0,
	content int default 0,
	primary key (anno_base_id),
	foreign key (anno_base_id) references anno_base(anno_base_id) ON DELETE CASCADE 
);

create table anno_treebank_node (
	anno_base_id int not null,
	parent int default 0,
	nodeType varchar2(10),
	nodeValue varchar2(10),
	leaf numeric(1) default 0,
	headIndex int default 0,
	"index" int default 0,
	tokenIndex int default 0,
	primary key (anno_base_id),
	foreign key (anno_base_id) references anno_base(anno_base_id) ON DELETE CASCADE 
);

create table anno_link (
	anno_link_id int not null,
	parent_anno_base_id int not null,
	child_anno_base_id int not null,
	feature varchar2(20),
	primary key (anno_link_id),
	foreign key (parent_anno_base_id) references anno_base(anno_base_id) ON DELETE CASCADE 
);
create index IX_link on anno_link(parent_anno_base_id, child_anno_base_id, feature);

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

