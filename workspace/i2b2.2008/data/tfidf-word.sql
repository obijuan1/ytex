delete from tfidf_doclength where name = 'i2b2.2008-word';
delete from tfidf_docfreq where name = 'i2b2.2008-word';

insert into tfidf_doclength (name, instance_id, length)
select 'i2b2.2008-word', s.*
from
(
  select k.uid, count(*)
  from ytex.document d
	inner join ytex.anno_base ak on ak.document_id = d.document_id
	inner join ytex.anno_dockey k on ak.anno_base_id = k.anno_base_id
	inner join ytex.anno_base an on an.document_id = ak.document_id
	inner join ytex.anno_word_token w on w.anno_base_id = an.anno_base_id
	where canonical_form is not null
	and analysis_batch = 'i2b2.2008'
	and w.canonical_form not in (select stopword from ytex.stopword)
  group by k.uid
) s;


insert into tfidf_docfreq (name, term, numdocs)
select 'i2b2.2008-word', s.*
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
		and analysis_batch = 'i2b2.2008'
		and w.canonical_form not in (select stopword from ytex.stopword)
	  	group by k.uid, w.canonical_form
	) s
	group by canonical_form
) s
;


insert into tfidf_termfreq (name, instance_id, term, freq)
select 'i2b2.2008-word', s.*
from
(
  select k.uid, canonical_form, count(*)
  from ytex.document d
	inner join ytex.anno_base ak on ak.document_id = d.document_id
	inner join ytex.anno_dockey k on ak.anno_base_id = k.anno_base_id
	inner join ytex.anno_base an on an.document_id = ak.document_id
	inner join ytex.anno_word_token w on w.anno_base_id = an.anno_base_id
	where canonical_form is not null
	and analysis_batch = 'i2b2.2008'
	and w.canonical_form not in (select stopword from ytex.stopword)
  group by k.uid, canonical_form
) s
;
