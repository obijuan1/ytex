CREATE TABLE MRCONSO (
    CUI	char(8) NOT NULL,
    LAT	char(3) NOT NULL,
    TS	char(1) NOT NULL,
    LUI	varchar2(10) NOT NULL,
    STT	varchar2(3) NOT NULL,
    SUI	varchar2(10) NOT NULL,
    ISPREF	char(1) NOT NULL,
    AUI	varchar2(9) NOT NULL primary key,
    SAUI	varchar2(50),
    SCUI	varchar2(50),
    SDUI	varchar2(50),
    SAB	varchar2(20) NOT NULL,
    TTY	varchar2(20) NOT NULL,
    CODE	varchar2(50) NOT NULL,
    STR	varchar2(3000) NOT NULL,
    SRL	int NOT NULL,
    SUPPRESS	char(1) NOT NULL,
    CVF	int
) ;


CREATE TABLE MRSTY (
    CUI	char(8) NOT NULL,
    TUI	char(4) NOT NULL,
    STN	varchar2(100) NOT NULL,
    STY	varchar2(50) NOT NULL,
    ATUI varchar2(11) NOT NULL,
    CVF	int 
);

