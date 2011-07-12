-- TODO utf-8/nvarchar ?

CREATE TABLE umls_ms_2009 (
  cui varchar2(10) NOT NULL,
  fword varchar2(80) NOT NULL,
  text clob,
  code varchar2(45) NOT NULL,
  sourcetype varchar2(45) NOT NULL,
  tui varchar2(4) NOT NULL
);


CREATE TABLE umls_snomed_map (
  cui varchar2(10) NOT NULL,
  code varchar2(45) NOT NULL
);

