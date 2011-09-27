/* insert documents - combine clinical history and impression in a single text field */
insert into corpus_doc (corpus_name, doc_id, doc_set, doc_text)
select 'cmc.2007', documentId, documentSet, concat('==clinical_history==\n\n', clinicalHistory, '\n\n==impression==\n\n', impression)
from cmcdocument
;

/* insert labels */
insert into corpus_doc_anno (corpus_doc_id, label, class)
select corpus_doc_id, d.code, case when cd.code is not null then 1 else 0 end
from
(
    select cd.corpus_doc_id, d.documentId, d.documentSet, l.*
    from cmcdocument d
    inner join corpus_doc cd on cd.doc_id = d.documentId and cd.corpus_name = 'cmc.2007'
    , cmcclasslabels l
) d
left join cmcdocumentcode cd on d.documentId = cd.documentId and d.code = cd.code
;

/* insert label ids */      
insert into corpus_label (corpus_name, label, label_id)
select 'cmc.2007', code, labelId
from cmcclasslabels;