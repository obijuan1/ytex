create view v_docdate
as
      select dt1.document_id, dt2.doc_date
      from anno_base dt1
      inner join anno_docdate dt2 on dt1.anno_base_id = dt2.anno_base_id
;

create view v_dockey
as
      select da.document_id, k.study_id, k.uid, k.site_id, k.document_type_id, t.document_type_name
      from anno_base da
      inner join anno_dockey k on k.anno_base_id = da.anno_base_id
      left join ref_document_type t on t.document_type_id = k.document_type_id
;

create view v_doctitle
as
      select tda.document_id, tda.span_begin, tda.span_end, substring(d.doc_text, tda.span_begin+1, tda.span_end-tda.span_begin) doc_title
      from document d
      inner join anno_base tda
      inner join ref_uima_type u on tda.uima_type_id = u.uima_type_id
      where u.uima_type_name = 'ytex.vacs.uima.types.DocumentTitle'
;

create view v_document
as
select
      d.analysis_batch,
      k.study_id,
      d.document_id,
      k.uid,
      k.document_type_id,
      k.document_type_name,
      k.site_id,
      dt.doc_date,
      doc_title,
      d.doc_text
from document d
left join v_docdate dt on d.document_id = dt.document_id
left join v_dockey k on d.document_id = k.document_id
left join v_doctitle t on d.document_id = t.document_id
;