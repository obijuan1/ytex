-- manually assign document type ids
create table ref_document_type (
	document_type_id int not null,
	document_type_name varchar2(20) not null,
	CONSTRAINT PK_ref_document_type PRIMARY KEY
	(
	document_type_id
	)
) ;

