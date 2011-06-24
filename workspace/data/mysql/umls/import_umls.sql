/*
 * this is only executed if you don't have umls installed in your db 
 * load the umls mrconso table from a dump file.
 * the following was copied directly from the umls load script.
 * 
 * intentionally do not drop MRCONSO - if it exists then we should use it 
 * instead of overwriting it
 */
CREATE TABLE  MRCONSO (
    CUI	nchar(8) NOT NULL,
    LAT	nchar(3) NOT NULL,
    TS	nchar(1) NOT NULL,
    LUI	nvarchar(10) NOT NULL,
    STT	nvarchar(3) NOT NULL,
    SUI	nvarchar(10) NOT NULL,
    ISPREF	nchar(1) NOT NULL,
    AUI	nvarchar(9) NOT NULL,
    SAUI	nvarchar(50),
    SCUI	nvarchar(50),
    SDUI	nvarchar(50),
    SAB	nvarchar(20) NOT NULL,
    TTY	nvarchar(20) NOT NULL,
    CODE	nvarchar(50) NOT NULL,
    STR	nvarchar(3000) NOT NULL,
    SRL	int  NOT NULL,
    SUPPRESS	nchar(1) NOT NULL,
    CVF	int 
);


load data local infile 'MRCONSO.RRF' into table MRCONSO fields terminated by '|' ESCAPED BY '' lines terminated by '\r\n'
(@cui,@lat,@ts,@lui,@stt,@sui,@ispref,@aui,@saui,@scui,@sdui,@sab,@tty,@code,@str,@srl,@suppress,@cvf)
SET CUI = @cui,
LAT = @lat,
TS = @ts,
LUI = @lui,
STT = @stt,
SUI = @sui,
ISPREF = @ispref,
AUI = @aui,
SAUI = NULLIF(@saui,''),
SCUI = NULLIF(@scui,''),
SDUI = NULLIF(@sdui,''),
SAB = @sab,
TTY = @tty,
CODE = @code,
STR = @str,
SRL = @srl,
SUPPRESS = @suppress,
CVF = NULLIF(@cvf,'');

CREATE INDEX X_MRCONSO_CUI ON MRCONSO(CUI);

ALTER TABLE MRCONSO ADD CONSTRAINT X_MRCONSO_PK  PRIMARY KEY BTREE (AUI);

CREATE INDEX X_MRCONSO_SUI ON MRCONSO(SUI);

CREATE INDEX X_MRCONSO_LUI ON MRCONSO(LUI);

CREATE INDEX X_MRCONSO_CODE ON MRCONSO(CODE);

CREATE INDEX X_MRCONSO_SAB_TTY ON MRCONSO(SAB,TTY);

CREATE INDEX X_MRCONSO_SCUI ON MRCONSO(SCUI);

CREATE INDEX X_MRCONSO_SDUI ON MRCONSO(SDUI);

CREATE INDEX X_MRCONSO_STR ON MRCONSO(STR(255));


CREATE TABLE MRSTY (
    CUI	char(8) NOT NULL,
    TUI	char(4) NOT NULL,
    STN	varchar(100) NOT NULL,
    STY	varchar(50) NOT NULL,
    ATUI	varchar(11) NOT NULL,
    CVF	int unsigned
) CHARACTER SET utf8;

load data local infile 'MRSTY.RRF' into table MRSTY fields terminated by '|' ESCAPED BY '' lines terminated by '\r\n'
(@cui,@tui,@stn,@sty,@atui,@cvf)
SET CUI = @cui,
TUI = @tui,
STN = @stn,
STY = @sty,
ATUI = @atui,
CVF = NULLIF(@cvf,'');

CREATE INDEX X_MRSTY_CUI ON MRSTY(CUI);

ALTER TABLE MRSTY ADD CONSTRAINT X_MRSTY_PK  PRIMARY KEY BTREE (ATUI);

CREATE INDEX X_MRSTY_STY ON MRSTY(STY);
