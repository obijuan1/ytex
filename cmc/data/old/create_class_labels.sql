drop table CMCClassLabels;

create table CMCClassLabels (
	labelId int auto_increment not null primary key,
	code varchar(50) not null
);


CREATE UNIQUE INDEX IX_CMCClassLabels ON CMCClassLabels (code)
;

insert into CMCClassLabels (code)
select distinct code from CMCDocumentCode 
order by code;

select l.labelId, c.code, count(*)
from CMCClassLabels l 
inner join CMCDocumentCode c on l.code = c.code
group by l.labelId, c.code 
order by count(*) desc;

select documentId, 0 dummyClass
from CMCDocument
;

select d.documentId, d.code, case when c.code is not null then 1 else 0 end value
from
(
select documentId, code
from CMCDocument d, CMCClassLabels
) d 
left join CMCDocumentCode c 
on c.documentId = d.documentId and c.code = d.code
;
