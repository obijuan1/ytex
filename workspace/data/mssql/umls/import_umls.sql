/*
 * this is only executed if you don't have umls installed in your db 
 * load the umls mrconso table from a dump file.
 * the following was copied directly from the umls load script.
 * 
 * intentionally do not drop MRCONSO - if it exists then we should use it 
 * instead of overwriting it
 */

CREATE TABLE $(db_schema).[MRCONSO](
	[CUI] [nchar](8) NOT NULL,
	[LAT] [nchar](3) NOT NULL,
	[TS] [nchar](1) NOT NULL,
	[LUI] [nvarchar](10) NOT NULL,
	[STT] [nvarchar](3) NOT NULL,
	[SUI] [nvarchar](10) NOT NULL,
	[ISPREF] [nchar](1) NOT NULL,
	[AUI] [nvarchar](9) NOT NULL,
	[SAUI] [nvarchar](50) NULL,
	[SCUI] [nvarchar](50) NULL,
	[SDUI] [nvarchar](50) NULL,
	[SAB] [nvarchar](20) NOT NULL,
	[TTY] [nvarchar](20) NOT NULL,
	[CODE] [nvarchar](50) NOT NULL,
	[STR] [nvarchar](max) NOT NULL,
	[SRL] [int] NOT NULL,
	[SUPPRESS] [nchar](1) NOT NULL,
	[CVF] [int] NULL
);

CREATE INDEX X_MRCONSO_CUI ON $(db_schema).MRCONSO(CUI);

ALTER TABLE $(db_schema).MRCONSO ADD CONSTRAINT X_MRCONSO_PK  PRIMARY KEY (AUI);

CREATE INDEX X_MRCONSO_SUI ON $(db_schema).MRCONSO(SUI);

CREATE INDEX X_MRCONSO_LUI ON $(db_schema).MRCONSO(LUI);

CREATE INDEX X_MRCONSO_CODE ON $(db_schema).MRCONSO(CODE);

CREATE INDEX X_MRCONSO_SAB_TTY ON $(db_schema).MRCONSO(SAB,TTY);

CREATE INDEX X_MRCONSO_SCUI ON $(db_schema).MRCONSO(SCUI);

CREATE INDEX X_MRCONSO_SDUI ON $(db_schema).MRCONSO(SDUI);


CREATE TABLE  $(db_schema).MRSTY (
    CUI	nchar(8) NOT NULL,
    TUI	nchar(4) NOT NULL,
    STN	nvarchar(100) NOT NULL,
    STY	nvarchar(50) NOT NULL,
    ATUI	nvarchar(11) NOT NULL,
    CVF	int 
);

CREATE INDEX X_MRSTY_CUI ON $(db_schema).MRSTY(CUI);

ALTER TABLE $(db_schema).MRSTY ADD CONSTRAINT X_MRSTY_PK  PRIMARY KEY (ATUI);

CREATE INDEX X_MRSTY_STY ON $(db_schema).MRSTY(STY);
