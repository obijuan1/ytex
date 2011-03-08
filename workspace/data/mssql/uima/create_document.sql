

CREATE TABLE $(db_schema).[document](
	[document_id] [int] IDENTITY(1,1) NOT NULL,
	[analysis_batch] [varchar](50) NOT NULL,
	[cas] [varbinary](max) NULL,
	[doc_text] [nvarchar](max) NULL,
 CONSTRAINT [PK_document] PRIMARY KEY CLUSTERED 
(
	[document_id]
)
)
;

CREATE NONCLUSTERED INDEX [IX_document_analysis_batch] ON $(db_schema).[document] 
(
	[analysis_batch]
)
;

CREATE TABLE $(db_schema).[document_class](
	[document_class_id] [int] IDENTITY(1,1) NOT NULL,
	[document_id] [int] NOT NULL,
	[task] [varchar](50) NOT NULL,
	[class_auto] [int] NOT NULL,
	[class_gold] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[document_class_id]
)
)
;

CREATE UNIQUE NONCLUSTERED INDEX [NK_document_class] ON $(db_schema).[document_class] 
(
	[document_id] ASC,
	[task] ASC
)
;

create table $(db_schema).anno_base (
	anno_base_id int identity not null, 
	document_id int not null, 
	span_begin int,
	span_end int,
	uima_type_id int not null,
	primary key (anno_base_id),
	foreign key (document_id) references $(db_schema).document (document_id) ON DELETE CASCADE,
	foreign key (uima_type_id) references $(db_schema).ref_uima_type (uima_type_id)
)
;

CREATE INDEX IX_docanno_doc ON $(db_schema).anno_base (document_id)
;

create table $(db_schema).anno_sentence (
	anno_base_id int not null,
	sentence_number int,
	primary key (anno_base_id),
	foreign key (anno_base_id) references $(db_schema).anno_base(anno_base_id)  ON DELETE CASCADE
);

create table $(db_schema).anno_named_entity (
	anno_base_id int not null, 
	discovery_technique int,
	status int,
	certainty int,
	type_id int,
	confidence float,
	segment_id varchar(64),
	primary key (anno_base_id),
	foreign key (anno_base_id) references $(db_schema).anno_base(anno_base_id)  ON DELETE CASCADE
);

create table $(db_schema).anno_ontology_concept (
	anno_ontology_concept_id int identity not null, 
	anno_base_id int not null,
	coding_scheme varchar(20),
	code varchar(20),
	oid varchar(10),
	primary key (anno_ontology_concept_id),
	foreign key (anno_base_id) references $(db_schema).anno_named_entity(anno_base_id)  ON DELETE CASCADE
);

create index IX_onto_concept_code on ${db_schema}.anno_ontology_concept (code);
create index IX_onto_concept_anno_code on ${db_schema}.anno_ontology_concept (anno_base_id, code);

create table $(db_schema).anno_umls_concept (
	anno_ontology_concept_id int not null,
	cui varchar(10),
	primary key (anno_ontology_concept_id),
	foreign key (anno_ontology_concept_id) references $(db_schema).anno_ontology_concept(anno_ontology_concept_id)  ON DELETE CASCADE
);

CREATE INDEX IX_umls_concept_cui ON $(db_schema).anno_umls_concept (cui)
;

CREATE TABLE $(db_schema).[anno_segment](
	[anno_base_id] [int] NOT NULL,
	[segment_id] [varchar](50) NULL,
PRIMARY KEY CLUSTERED 
(
	[anno_base_id] ASC
)
)
;

ALTER TABLE $(db_schema).[anno_segment]  WITH CHECK ADD FOREIGN KEY([anno_base_id])
REFERENCES $(db_schema).[anno_base] ([anno_base_id])
ON DELETE CASCADE
;

CREATE NONCLUSTERED INDEX [IX_segment_anno_seg] ON $(db_schema).[anno_segment] 
(
	[anno_base_id] ASC,
	[segment_id] ASC
)
;

/*
 * mapped to SourceDocumentInformation
 */
create table $(db_schema).anno_source_doc_info (
	[anno_base_id] [int] NOT NULL,
	uri varchar(256),
	offset_in_source int,
	document_size int,
	last_segment bit,
	PRIMARY KEY CLUSTERED 
	(
		[anno_base_id] ASC
	),
	foreign key (anno_base_id) references $(db_schema).anno_base(anno_base_id)  ON DELETE CASCADE
);


/**
 * mapped to BaseToken
 */
create table $(db_schema).anno_base_token (
	[anno_base_id] [int] NOT NULL,
	token_number int,
	normalized_form varchar(256),
	part_of_speech varchar(5),
	PRIMARY KEY CLUSTERED 
	(
		[anno_base_id] ASC
	),
	foreign key (anno_base_id) 
		references $(db_schema).anno_base(anno_base_id)  
		ON DELETE CASCADE
);

/**
 * BaseToken.lemmaEntries
create table $(db_schema).anno_lemma (
	anno_lemma_id [int] IDENTITY(1,1) NOT NULL,
	anno_base_id int not null,
	lemma_key varchar(10),
	pos_tag varchar(5),
	PRIMARY KEY CLUSTERED 
	(
		[anno_lemma_id] ASC
	),
	foreign key (anno_base_id) 
		references $(db_schema).anno_base_token(anno_base_id)  
		ON DELETE CASCADE

);
 */

/**
 * mapped to NumToken
 */
create table $(db_schema).anno_num_token (
	[anno_base_id] [int] NOT NULL,
	num_type int,
	PRIMARY KEY CLUSTERED 
	(
		[anno_base_id] ASC
	),
	foreign key (anno_base_id) 
		references $(db_schema).anno_base_token(anno_base_id)  
		ON DELETE CASCADE
)

/**
 * mapped to WordToken
 */
create table $(db_schema).anno_word_token (
	[anno_base_id] [int] NOT NULL,
	capitalization [int],
	num_position [int],
	suggestion [int],
	canonical_form varchar(256),
	PRIMARY KEY CLUSTERED 
	(
		[anno_base_id] ASC
	),
	foreign key (anno_base_id) 
		references $(db_schema).anno_base_token(anno_base_id)  
		ON DELETE CASCADE
);

create index IX_word_stem on $(db_schema).anno_word_token (canonical_form);
create index IX_word_anno_stem on $(db_schema).anno_word_token (anno_base_id, canonical_form);

create table $(db_schema).anno_date (
	anno_base_id int not null,
	tstamp datetime,
	primary key (anno_base_id),
	foreign key (anno_base_id) references $(db_schema).anno_base(anno_base_id) ON DELETE CASCADE
);
