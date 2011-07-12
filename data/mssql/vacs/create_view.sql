-- 
create view $(db_schema).v_document
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
      substring(d.doc_text, title.span_begin+1, title.span_end-title.span_begin) doc_title,
      d.doc_text
from $(db_schema).document d
-- document date
left join 
      (
      select dt1.document_id, dt2.doc_date
      from $(db_schema).anno_base dt1 
      inner join $(db_schema).anno_docdate dt2 on dt1.anno_base_id = dt2.anno_base_id
      ) dt on dt.document_id = d.document_id
left join 
-- doc title
      (
      select tda.document_id, tda.span_begin, tda.span_end
      from $(db_schema).anno_base tda
      inner join $(db_schema).ref_uima_type u on tda.uima_type_id = u.uima_type_id 
      where u.uima_type_name = 'ytex.vacs.uima.types.DocumentTitle'
      ) title on title.document_id = d.document_id
left join 
-- doc key
      (
      select da.document_id, k.study_id, k.uid, k.site_id, k.document_type_id, t.document_type_name
      from $(db_schema).anno_base da
      inner join $(db_schema).anno_dockey k on k.anno_base_id = da.anno_base_id 
      left join $(db_schema).ref_document_type t on t.document_type_id = k.document_type_id
      ) k on k.document_id = d.document_id
;
go
