-- delete cmc corpus from corpus_doc and corpus_label
delete from corpus_label where corpus_name = 'cmc.2007';
delete from corpus_doc where corpus_name = 'cmc.2007';