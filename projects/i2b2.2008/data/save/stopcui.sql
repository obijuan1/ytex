create table stopcui (
    cui char(8) primary key
) engine = myisam;

insert into stopcui
select code	
from		
(		
	select code, count(*) cc		
	from		
	(		
		select distinct d.docId, code	
		from i2b2_2008_doc d 	
		/* get ytex document */	
		inner join document yd 	
			on yd.uid = d.docId
			and yd.analysis_batch = 'i2b2.2008'
		/* get cuis in document */	
		inner join anno_base ab 	
			on ab.document_id = yd.document_id
		inner join anno_ontology_concept c	
		    on c.anno_base_id = ab.anno_base_id	
	) s group by code		
) s where cc > 1200
;
