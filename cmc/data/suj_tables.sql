create index IX_norm_term_id on suj_concept(norm_term_id);
create index IX_lexical_unit_id on suj_norm_term (lexical_unit_id);
create index IX_document_id on suj_lexical_unit(document_id);
