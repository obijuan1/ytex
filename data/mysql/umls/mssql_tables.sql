DROP TABLE $(db_schema).MRCOC;
CREATE TABLE  $(db_schema).MRCOC (
    CUI1	nchar(8) NOT NULL,
    AUI1	nvarchar(9) NOT NULL,
    CUI2	nchar(8),
    AUI2	nvarchar(9),
    SAB	nvarchar(20) NOT NULL,
    COT	nvarchar(3) NOT NULL,
    COF	int ,
    COA	nvarchar(300),
    CVF	int 
);

DROP TABLE $(db_schema).MRCOLS;
CREATE TABLE  $(db_schema).MRCOLS (
    COL	nvarchar(20),
    DES	nvarchar(200),
    REF	nvarchar(20),
    MIN	int ,
    AV	numeric(5,2),
    MAX	int ,
    FIL	nvarchar(50),
    DTY	nvarchar(20)
);

DROP TABLE $(db_schema).MRCONSO;
CREATE TABLE  $(db_schema).MRCONSO (
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

DROP TABLE $(db_schema).MRCUI;
CREATE TABLE  $(db_schema).MRCUI (
    CUI1	nchar(8) NOT NULL,
    VER	nvarchar(10) NOT NULL,
    REL	nvarchar(4) NOT NULL,
    RELA	nvarchar(100),
    MAPREASON	nvarchar(4000),
    CUI2	nchar(8),
    MAPIN	nchar(1)
);

DROP TABLE $(db_schema).MRCXT;
CREATE TABLE  $(db_schema).MRCXT (
    CUI	nchar(8),
    SUI	nvarchar(10),
    AUI	nvarchar(9),
    SAB	nvarchar(20),
    CODE	nvarchar(50),
    CXN	int ,
    CXL	nchar(3),
    RANK	int ,
    CXS	nvarchar(3000),
    CUI2	nchar(8),
    AUI2	nvarchar(9),
    HCD	nvarchar(50),
    RELA	nvarchar(100),
    XC	nvarchar(1),
    CVF	int 
);

DROP TABLE $(db_schema).MRDEF;
CREATE TABLE  $(db_schema).MRDEF (
    CUI	nchar(8) NOT NULL,
    AUI	nvarchar(9) NOT NULL,
    ATUI	nvarchar(11) NOT NULL,
    SATUI	nvarchar(50),
    SAB	nvarchar(20) NOT NULL,
    DEF	nvarchar(4000) NOT NULL,
    SUPPRESS	nchar(1) NOT NULL,
    CVF	int 
);

DROP TABLE $(db_schema).MRDOC;
CREATE TABLE  $(db_schema).MRDOC (
    DOCKEY	nvarchar(50) NOT NULL,
    VALUE	nvarchar(200),
    TYPE	nvarchar(50) NOT NULL,
    EXPL	nvarchar(max)
);

DROP TABLE $(db_schema).MRFILES;
CREATE TABLE  $(db_schema).MRFILES (
    FIL	nvarchar(50),
    DES	nvarchar(200),
    FMT	nvarchar(300),
    CLS	int ,
    RWS	int ,
    BTS	bigint
);

DROP TABLE $(db_schema).MRHIER;
CREATE TABLE  $(db_schema).MRHIER (
    CUI	nchar(8) NOT NULL,
    AUI	nvarchar(9) NOT NULL,
    CXN	int  NOT NULL,
    PAUI	nvarchar(10),
    SAB	nvarchar(20) NOT NULL,
    RELA	nvarchar(100),
    PTR	nvarchar(1000),
    HCD	nvarchar(50),
    CVF	int 
);

DROP TABLE $(db_schema).MRHIST;
CREATE TABLE  $(db_schema).MRHIST (
    CUI	nchar(8),
    SOURCEUI	nvarchar(50),
    SAB	nvarchar(20),
    SVER	nvarchar(20),
    CHANGETYPE	nvarchar(1000),
    CHANGEKEY	nvarchar(1000),
    CHANGEVAL	nvarchar(1000),
    REASON	nvarchar(1000),
    CVF	int 
);

DROP TABLE $(db_schema).MRMAP;
CREATE TABLE  $(db_schema).MRMAP (
    MAPSETCUI	nchar(8) NOT NULL,
    MAPSETSAB	nvarchar(20) NOT NULL,
    MAPSUBSETID	nvarchar(10),
    MAPRANK	int ,
    MAPID	nvarchar(50) NOT NULL,
    MAPSID	nvarchar(50),
    FROMID	nvarchar(50) NOT NULL,
    FROMSID	nvarchar(50),
    FROMEXPR	nvarchar(4000) NOT NULL,
    FROMTYPE	nvarchar(50) NOT NULL,
    FROMRULE	nvarchar(4000),
    FROMRES	nvarchar(4000),
    REL	nvarchar(4) NOT NULL,
    RELA	nvarchar(100),
    TOID	nvarchar(50),
    TOSID	nvarchar(50),
    TOEXPR	nvarchar(4000),
    TOTYPE	nvarchar(50),
    TORULE	nvarchar(4000),
    TORES	nvarchar(4000),
    MAPRULE	nvarchar(4000),
    MAPRES	nvarchar(4000),
    MAPTYPE	nvarchar(50),
    MAPATN	nvarchar(20),
    MAPATV	nvarchar(4000),
    CVF	int 
);

DROP TABLE $(db_schema).MRRANK;
CREATE TABLE  $(db_schema).MRRANK (
    RANK	int  NOT NULL,
    SAB	nvarchar(20) NOT NULL,
    TTY	nvarchar(20) NOT NULL,
    SUPPRESS	nchar(1) NOT NULL
);

DROP TABLE $(db_schema).MRREL;
CREATE TABLE  $(db_schema).MRREL (
    CUI1	nchar(8) NOT NULL,
    AUI1	nvarchar(9),
    STYPE1	nvarchar(50) NOT NULL,
    REL	nvarchar(4) NOT NULL,
    CUI2	nchar(8) NOT NULL,
    AUI2	nvarchar(9),
    STYPE2	nvarchar(50) NOT NULL,
    RELA	nvarchar(100),
    RUI	nvarchar(10) NOT NULL,
    SRUI	nvarchar(50),
    SAB	nvarchar(20) NOT NULL,
    SL	nvarchar(20) NOT NULL,
    RG	nvarchar(10),
    DIR	nvarchar(1),
    SUPPRESS	nchar(1) NOT NULL,
    CVF	int 
);

DROP TABLE $(db_schema).MRSAB;
CREATE TABLE  $(db_schema).MRSAB (
    VCUI	nchar(8),
    RCUI	nchar(8),
    VSAB	nvarchar(20) NOT NULL,
    RSAB	nvarchar(20) NOT NULL,
    SON	nvarchar(3000) NOT NULL,
    SF	nvarchar(20) NOT NULL,
    SVER	nvarchar(20),
    VSTART	nchar(8),
    VEND	nchar(8),
    IMETA	nvarchar(10) NOT NULL,
    RMETA	nvarchar(10),
    SLC	nvarchar(1000),
    SCC	nvarchar(1000),
    SRL	int  NOT NULL,
    TFR	int ,
    CFR	int ,
    CXTY	nvarchar(50),
    TTYL	nvarchar(300),
    ATNL	nvarchar(1000),
    LAT	nchar(3),
    CENC	nvarchar(20) NOT NULL,
    CURVER	nchar(1) NOT NULL,
    SABIN	nchar(1) NOT NULL,
    SSN	nvarchar(3000) NOT NULL,
    SCIT	nvarchar(4000) NOT NULL
);

DROP TABLE $(db_schema).MRSAT;
CREATE TABLE  $(db_schema).MRSAT (
    CUI	nchar(8) NOT NULL,
    LUI	nvarchar(10),
    SUI	nvarchar(10),
    METAUI	nvarchar(50),
    STYPE	nvarchar(50) NOT NULL,
    CODE	nvarchar(50),
    ATUI	nvarchar(11) NOT NULL,
    SATUI	nvarchar(50),
    ATN	nvarchar(50) NOT NULL,
    SAB	nvarchar(20) NOT NULL,
    ATV	nvarchar(4000),
    SUPPRESS	nchar(1) NOT NULL,
    CVF	int 
);

DROP TABLE $(db_schema).MRSMAP;
CREATE TABLE  $(db_schema).MRSMAP (
    MAPSETCUI	nchar(8) NOT NULL,
    MAPSETSAB	nvarchar(20) NOT NULL,
    MAPID	nvarchar(50) NOT NULL,
    MAPSID	nvarchar(50),
    FROMEXPR	nvarchar(4000) NOT NULL,
    FROMTYPE	nvarchar(50) NOT NULL,
    REL	nvarchar(4) NOT NULL,
    RELA	nvarchar(100),
    TOEXPR	nvarchar(4000),
    TOTYPE	nvarchar(50),
    CVF	int 
);

DROP TABLE $(db_schema).MRSTY;
CREATE TABLE  $(db_schema).MRSTY (
    CUI	nchar(8) NOT NULL,
    TUI	nchar(4) NOT NULL,
    STN	nvarchar(100) NOT NULL,
    STY	nvarchar(50) NOT NULL,
    ATUI	nvarchar(11) NOT NULL,
    CVF	int 
);

DROP TABLE $(db_schema).MRXNS_ENG;
CREATE TABLE  $(db_schema).MRXNS_ENG (
    LAT	nchar(3) NOT NULL,
    NSTR	nvarchar(3000) NOT NULL,
    CUI	nchar(8) NOT NULL,
    LUI	nvarchar(10) NOT NULL,
    SUI	nvarchar(10) NOT NULL
);

DROP TABLE $(db_schema).MRXNW_ENG;
CREATE TABLE  $(db_schema).MRXNW_ENG (
    LAT	nchar(3) NOT NULL,
    NWD	nvarchar(100) NOT NULL,
    CUI	nchar(8) NOT NULL,
    LUI	nvarchar(10) NOT NULL,
    SUI	nvarchar(10) NOT NULL
);

DROP TABLE $(db_schema).MRAUI;
CREATE TABLE  $(db_schema).MRAUI (
    AUI1	nvarchar(9) NOT NULL,
    CUI1	nchar(8) NOT NULL,
    VER	nvarchar(10) NOT NULL,
    REL	nvarchar(4),
    RELA	nvarchar(100),
    MAPREASON	nvarchar(4000) NOT NULL,
    AUI2	nvarchar(9) NOT NULL,
    CUI2	nchar(8) NOT NULL,
    MAPIN	nchar(1) NOT NULL
);

DROP TABLE $(db_schema).MRXW_BAQ;
CREATE TABLE  $(db_schema).MRXW_BAQ (
    LAT	nchar(3) NOT NULL,
    WD	nvarchar(200) NOT NULL,
    CUI	nchar(8) NOT NULL,
    LUI	nvarchar(10) NOT NULL,
    SUI	nvarchar(10) NOT NULL
);

DROP TABLE $(db_schema).MRXW_CZE;
CREATE TABLE  $(db_schema).MRXW_CZE (
    LAT	nchar(3) NOT NULL,
    WD	nvarchar(200) NOT NULL,
    CUI	nchar(8) NOT NULL,
    LUI	nvarchar(10) NOT NULL,
    SUI	nvarchar(10) NOT NULL
);

DROP TABLE $(db_schema).MRXW_DAN;
CREATE TABLE  $(db_schema).MRXW_DAN (
    LAT	nchar(3) NOT NULL,
    WD	nvarchar(200) NOT NULL,
    CUI	nchar(8) NOT NULL,
    LUI	nvarchar(10) NOT NULL,
    SUI	nvarchar(10) NOT NULL
);

DROP TABLE $(db_schema).MRXW_DUT;
CREATE TABLE  $(db_schema).MRXW_DUT (
    LAT	nchar(3) NOT NULL,
    WD	nvarchar(200) NOT NULL,
    CUI	nchar(8) NOT NULL,
    LUI	nvarchar(10) NOT NULL,
    SUI	nvarchar(10) NOT NULL
);

DROP TABLE $(db_schema).MRXW_ENG;
CREATE TABLE  $(db_schema).MRXW_ENG (
    LAT	nchar(3) NOT NULL,
    WD	nvarchar(200) NOT NULL,
    CUI	nchar(8) NOT NULL,
    LUI	nvarchar(10) NOT NULL,
    SUI	nvarchar(10) NOT NULL
);

DROP TABLE $(db_schema).MRXW_FIN;
CREATE TABLE  $(db_schema).MRXW_FIN (
    LAT	nchar(3) NOT NULL,
    WD	nvarchar(200) NOT NULL,
    CUI	nchar(8) NOT NULL,
    LUI	nvarchar(10) NOT NULL,
    SUI	nvarchar(10) NOT NULL
);

DROP TABLE $(db_schema).MRXW_FRE;
CREATE TABLE  $(db_schema).MRXW_FRE (
    LAT	nchar(3) NOT NULL,
    WD	nvarchar(200) NOT NULL,
    CUI	nchar(8) NOT NULL,
    LUI	nvarchar(10) NOT NULL,
    SUI	nvarchar(10) NOT NULL
);

DROP TABLE $(db_schema).MRXW_GER;
CREATE TABLE  $(db_schema).MRXW_GER (
    LAT	nchar(3) NOT NULL,
    WD	nvarchar(200) NOT NULL,
    CUI	nchar(8) NOT NULL,
    LUI	nvarchar(10) NOT NULL,
    SUI	nvarchar(10) NOT NULL
);

DROP TABLE $(db_schema).MRXW_HEB;
CREATE TABLE  $(db_schema).MRXW_HEB (
    LAT	nchar(3) NOT NULL,
    WD	nvarchar(200) NOT NULL,
    CUI	nchar(8) NOT NULL,
    LUI	nvarchar(10) NOT NULL,
    SUI	nvarchar(10) NOT NULL
);

DROP TABLE $(db_schema).MRXW_HUN;
CREATE TABLE  $(db_schema).MRXW_HUN (
    LAT	nchar(3) NOT NULL,
    WD	nvarchar(200) NOT NULL,
    CUI	nchar(8) NOT NULL,
    LUI	nvarchar(10) NOT NULL,
    SUI	nvarchar(10) NOT NULL
);

DROP TABLE $(db_schema).MRXW_ITA;
CREATE TABLE  $(db_schema).MRXW_ITA (
    LAT	nchar(3) NOT NULL,
    WD	nvarchar(200) NOT NULL,
    CUI	nchar(8) NOT NULL,
    LUI	nvarchar(10) NOT NULL,
    SUI	nvarchar(10) NOT NULL
);

DROP TABLE $(db_schema).MRXW_JPN;
CREATE TABLE  $(db_schema).MRXW_JPN (
    LAT char(3) NOT NULL,
    WD  varchar(500) NOT NULL,
    CUI char(8) NOT NULL,
    LUI varchar(10) NOT NULL,
    SUI varchar(10) NOT NULL
);

DROP TABLE $(db_schema).MRXW_KOR;
CREATE TABLE  $(db_schema).MRXW_KOR (
    LAT char(3) NOT NULL,
    WD  varchar(500) NOT NULL,
    CUI char(8) NOT NULL,
    LUI varchar(10) NOT NULL,
    SUI varchar(10) NOT NULL
);

DROP TABLE $(db_schema).MRXW_LAV;
CREATE TABLE  $(db_schema).MRXW_LAV (
    LAT char(3) NOT NULL,
    WD  varchar(200) NOT NULL,
    CUI char(8) NOT NULL,
    LUI varchar(10) NOT NULL,
    SUI varchar(10) NOT NULL
);

DROP TABLE $(db_schema).MRXW_NOR;
CREATE TABLE  $(db_schema).MRXW_NOR (
    LAT	nchar(3) NOT NULL,
    WD	nvarchar(200) NOT NULL,
    CUI	nchar(8) NOT NULL,
    LUI	nvarchar(10) NOT NULL,
    SUI	nvarchar(10) NOT NULL
);

DROP TABLE $(db_schema).MRXW_POR;
CREATE TABLE  $(db_schema).MRXW_POR (
    LAT	nchar(3) NOT NULL,
    WD	nvarchar(200) NOT NULL,
    CUI	nchar(8) NOT NULL,
    LUI	nvarchar(10) NOT NULL,
    SUI	nvarchar(10) NOT NULL
);

DROP TABLE $(db_schema).MRXW_RUS;
CREATE TABLE  $(db_schema).MRXW_RUS (
    LAT char(3) NOT NULL,
    WD  varchar(200) NOT NULL,
    CUI char(8) NOT NULL,
    LUI varchar(10) NOT NULL,
    SUI varchar(10) NOT NULL
);

DROP TABLE $(db_schema).MRXW_SCR;
CREATE TABLE  $(db_schema).MRXW_SCR (
    LAT char(3) NOT NULL,
    WD  varchar(200) NOT NULL,
    CUI char(8) NOT NULL,
    LUI varchar(10) NOT NULL,
    SUI varchar(10) NOT NULL
);

DROP TABLE $(db_schema).MRXW_SPA;
CREATE TABLE  $(db_schema).MRXW_SPA (
    LAT	nchar(3) NOT NULL,
    WD	nvarchar(200) NOT NULL,
    CUI	nchar(8) NOT NULL,
    LUI	nvarchar(10) NOT NULL,
    SUI	nvarchar(10) NOT NULL
);

DROP TABLE $(db_schema).MRXW_SWE;
CREATE TABLE  $(db_schema).MRXW_SWE (
    LAT	nchar(3) NOT NULL,
    WD	nvarchar(200) NOT NULL,
    CUI	nchar(8) NOT NULL,
    LUI	nvarchar(10) NOT NULL,
    SUI	nvarchar(10) NOT NULL
);

DROP TABLE $(db_schema).AMBIGSUI;
CREATE TABLE  $(db_schema).AMBIGSUI (
    SUI	nvarchar(10) NOT NULL,
    CUI	nchar(8) NOT NULL
);

DROP TABLE $(db_schema).AMBIGLUI;
CREATE TABLE  $(db_schema).AMBIGLUI (
    LUI	nvarchar(10) NOT NULL,
    CUI	nchar(8) NOT NULL
);

DROP TABLE $(db_schema).DELETEDCUI;
CREATE TABLE  $(db_schema).DELETEDCUI (
    PCUI	nchar(8) NOT NULL,
    PSTR	nvarchar(3000) NOT NULL
);

DROP TABLE $(db_schema).DELETEDLUI;
CREATE TABLE  $(db_schema).DELETEDLUI (
    PLUI	nvarchar(10) NOT NULL,
    PSTR	nvarchar(3000) NOT NULL
);

DROP TABLE $(db_schema).DELETEDSUI;
CREATE TABLE  $(db_schema).DELETEDSUI (
    PSUI	nvarchar(10) NOT NULL,
    LAT	nchar(3) NOT NULL,
    PSTR	nvarchar(3000) NOT NULL
);

DROP TABLE $(db_schema).MERGEDCUI;
CREATE TABLE  $(db_schema).MERGEDCUI (
    PCUI	nchar(8) NOT NULL,
    CUI	nchar(8) NOT NULL
);

DROP TABLE $(db_schema).MERGEDLUI;
CREATE TABLE  $(db_schema).MERGEDLUI (
    PLUI	nvarchar(10),
    LUI	nvarchar(10)
);
