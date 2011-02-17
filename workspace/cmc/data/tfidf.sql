insert into tfidf_doclength (name, instance_id, length)
select 'cmc-word', s.*
from
(
  select k.uid, count(*)
  from ytex.document d
	inner join ytex.anno_base ak on ak.document_id = d.document_id
	inner join ytex.anno_dockey k on ak.anno_base_id = k.anno_base_id
	inner join ytex.anno_base an on an.document_id = ak.document_id
	inner join ytex.anno_word_token w on w.anno_base_id = an.anno_base_id
	where canonical_form is not null
	and analysis_batch = 'cmc-word'
	and w.canonical_form not in (select stopword from stopword)
  group by k.uid
) s;


insert into tfidf_docfreq (name, term, numdocs)
select 'cmc-word', s.*
from
(
	select canonical_form, count(*)
	from
	(
	  	select distinct w.canonical_form, k.uid
		from ytex.document d
		inner join ytex.anno_base ak on ak.document_id = d.document_id
		inner join ytex.anno_dockey k on ak.anno_base_id = k.anno_base_id
		inner join ytex.anno_base an on an.document_id = ak.document_id
		inner join ytex.anno_word_token w on w.anno_base_id = an.anno_base_id
		where canonical_form is not null
		and analysis_batch = 'cmc-word'
		and w.canonical_form not in (select stopword from stopword)
	  	group by k.uid, w.canonical_form
	) s
	group by canonical_form
) s
;
