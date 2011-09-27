	select i.instance_id instance_id, case when class = 0 then -1 else 1 end class, i.train, l.label_id label, f.fold, f.run
	from cv_fold f
	inner join cv_fold_instance i 
	    on f.cv_fold_id = i.cv_fold_id
	inner join corpus_doc_anno a 
	    on a.corpus_name = f.corpus_name
	    and a.doc_id = i.instance_id
	    and a.label = f.label 
	inner join corpus_label l 
	    on l.corpus_name = f.corpus_name 
	    and l.label = f.label
	where f.corpus_name = 'cmc.2007'
	
	union

	/* 
	add held-out test data as unlabeled to each fold 
	*/	
	select d.doc_id, 0, 1, l.label_id, f.fold, f.run
	from corpus_doc d
	inner join cv_fold f 
	    on d.corpus_name = f.corpus_name
	inner join corpus_label l 
	    on l.corpus_name = f.corpus_name 
	    and l.label = f.label
	where d.doc_set = 'test' 
	and d.corpus_name = 'cmc.2007'
;