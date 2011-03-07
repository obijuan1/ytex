drop table cmcdocument;
create table cmcdocument (
  	documentId int not null,
    documentSet varchar(50) not null,
	clinicalHistory varchar(2000) not null,
	impression varchar(2000) not null,

  	CONSTRAINT PK_Document PRIMARY KEY
	(
	documentId
	)
);

drop table cmcdocumentCode;
create table cmcdocumentCode (
	documentId int not null,
	code varchar(50) not null,
	CONSTRAINT PK_DocumentCode PRIMARY KEY
	(
	documentId, code
	)
);

-- document types for cmc
delete from ref_document_type where document_type_id in (100, 101);
insert into ref_document_type (document_type_id, document_type_name)
values (100, 'CMC_CLINICALHISTORY');
insert into ref_document_type (document_type_id, document_type_name)
values (101, 'CMC_IMPRESSION');
