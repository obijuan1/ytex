

create table $(db_schema).anno_docdate (
	anno_base_id int not null,
	doc_date datetime,
	primary key (anno_base_id),
	foreign key (anno_base_id) references $(db_schema).anno_base(anno_base_id) ON DELETE CASCADE
);

create table $(db_schema).anno_dockey (
	anno_base_id int not null,
	study_id numeric(20,0) not NULL default 0,
	uid bigint not NULL default 0,
	site_id varchar(4) null,
	document_type_id int not NULL default 0,
	primary key (anno_base_id),
	foreign key (anno_base_id) references $(db_schema).anno_base(anno_base_id) ON DELETE CASCADE
);


CREATE INDEX IX_anno_dockey_all ON $(db_schema).anno_dockey
(
	document_type_id,
	study_id,
	uid,
	site_id
)
;

CREATE INDEX IX_anno_dockey_studyid ON $(db_schema).anno_dockey
(
	study_id
)
;

CREATE INDEX IX_anno_dockey_uid ON $(db_schema).anno_dockey
(
	uid
)
;

CREATE INDEX IX_anno_id_uid ON $(db_schema).anno_dockey
(
	anno_base_id,
	uid
)
;