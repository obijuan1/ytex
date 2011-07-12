insert into ref_document_type (document_type_id, document_type_name)
values (0, 'NOTE');

insert into ref_document_type (document_type_id, document_type_name)
values (1, 'PROGRESS_NOTE');

insert into ref_document_type (document_type_id, document_type_name)
values (2, 'RADIOLOGY');

insert into ref_document_type (document_type_id, document_type_name)
values (3, 'PATHOLOGY');

insert into ref_uima_type values(100 , 'ytex.vacs.uima.types.DocumentDate', 'ytex.uima.mapper.DocumentDateAnnotationMapper' );
insert into ref_uima_type values(101 , 'ytex.vacs.uima.types.DocumentTitle', 'ytex.uima.mapper.AnnotationMapper');
insert into ref_uima_type values(102 , 'ytex.vacs.uima.types.DocumentKey', 'ytex.uima.mapper.DocumentKeyAnnotationMapper');

insert into ref_segment_regex (segment_id, regex) values('CLINICAL_HISTORY', '\nClinical History\:\s*\r{0,1}\n');
insert into ref_segment_regex (segment_id, regex) values('REPORT', '\nReport\:\s*\r{0,1}\n');
insert into ref_segment_regex (segment_id, regex) values('IMPRESSION', '\nImpression\:\s*\r{0,1}\n');

