
create table $(db_schema).ref_document_type (
	document_type_id int not null,
	document_type_name varchar(20) not null
	CONSTRAINT PK_ref_document_type PRIMARY KEY  
	(
	document_type_id ASC
	)
);
