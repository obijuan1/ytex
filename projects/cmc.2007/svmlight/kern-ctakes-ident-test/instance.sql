	select d.doc_id, case when class = 0 then -1 else 1 end, doc_set = 'train', l.label_id
	from corpus_doc d
	inner join corpus_doc_anno a 
	    on a.corpus_name = d.corpus_name
	    and a.doc_id = d.doc_id
	inner join corpus_label l 
	    on l.corpus_name = a.corpus_name 
	    and l.label = a.label
	where d.corpus_name = 'cmc.2007';