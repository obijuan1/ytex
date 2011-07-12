CREATE TABLE $(db_schema).[dummy_document](
	[studyid] [int] NOT NULL,
	[uid] int identity(1,1) NOT NULL,
	[document_type_id] [int] NOT NULL,
	[doc_text] [varchar](max) NOT NULL,
	[site_id] varchar(4) NOT NULL default '',
 CONSTRAINT [PK_dummy_document] PRIMARY KEY CLUSTERED 
(
	[studyid] ASC,
	[uid] ASC,
	[document_type_id] ASC
)
)
