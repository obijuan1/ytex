delete from tfidf_doclength where name = 'i2b2.2008-ncuiword';
delete from tfidf_docfreq where name = 'i2b2.2008-ncuiword';
delete from tfidf_termfreq where name = 'i2b2.2008-ncuiword';

drop table namedentity_to_word;

create table namedentity_to_word (
  named_entity_id int,
  word_id int,
  key ix_word_id (word_id),
  primary key pk_neword (named_entity_id, word_id)
);

insert into namedentity_to_word (named_entity_id, word_id)
  select an.anno_base_id, aw.anno_base_id
  from ytex.document d
	inner join ytex.anno_base aw on aw.document_id = d.document_id
	inner join ytex.anno_word_token w on w.anno_base_id = aw.anno_base_id
	inner join ytex.anno_base an
		on an.document_id = d.document_id
		and an.uima_type_id = 8
		and an.span_begin <= aw.span_begin
		and an.span_end >= aw.span_end
	where canonical_form is not null
	and analysis_batch = 'i2b2.2008'
	and w.canonical_form not in (select stopword from ytex.stopword)
;


insert into tfidf_doclength (name, instance_id, length)
select 'i2b2.2008-ncuiword', s.*
from
(
	select k.uid, count(*)
	from ytex.document d
	inner join ytex.anno_base ak on ak.document_id = d.document_id
	inner join ytex.anno_dockey k on ak.anno_base_id = k.anno_base_id
	inner join ytex.anno_base aw on aw.document_id = d.document_id
	inner join ytex.anno_word_token w on w.anno_base_id = aw.anno_base_id
	/* exclude words contained within a named entity */
	left join namedentity_to_word nw on nw.word_id = aw.anno_base_id
	where canonical_form is not null
	and analysis_batch = 'i2b2.2008'
	and w.canonical_form not in (select stopword from ytex.stopword)
	and nw.named_entity_id is null
	group by k.uid
) s;


insert into tfidf_docfreq (name, term, numdocs)
select 'i2b2.2008-ncuiword', s.*
from
(
	select canonical_form, count(*)
	from
	(
	  	select distinct canonical_form, k.uid
		from ytex.document d
		inner join ytex.anno_base ak on ak.document_id = d.document_id
		inner join ytex.anno_dockey k on ak.anno_base_id = k.anno_base_id
		inner join ytex.anno_base aw on aw.document_id = d.document_id
		inner join ytex.anno_word_token w on w.anno_base_id = aw.anno_base_id
		/* exclude words contained within a named entity */
		left join namedentity_to_word nw on nw.word_id = aw.anno_base_id
		where canonical_form is not null
		and analysis_batch = 'i2b2.2008'
		and w.canonical_form not in (select stopword from ytex.stopword)
		and nw.named_entity_id is null
	) s
	group by canonical_form
) s
;

insert into tfidf_termfreq (name, instance_id, term, freq)
select 'i2b2.2008-ncuiword', s.*
from
(
	select k.uid, canonical_form, count(*)
	from ytex.document d
	inner join ytex.anno_base ak on ak.document_id = d.document_id
	inner join ytex.anno_dockey k on ak.anno_base_id = k.anno_base_id
	inner join ytex.anno_base aw on aw.document_id = d.document_id
	inner join ytex.anno_word_token w on w.anno_base_id = aw.anno_base_id
	/* exclude words contained within a named entity */
	left join namedentity_to_word nw on nw.word_id = aw.anno_base_id
	where canonical_form is not null
	and analysis_batch = 'i2b2.2008'
	and w.canonical_form not in (select stopword from ytex.stopword)
	and nw.named_entity_id is null
	group by k.uid, canonical_form
) s
;
