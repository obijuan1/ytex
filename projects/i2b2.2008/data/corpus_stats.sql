select
  a.disease,
  sum(d.documentSet = 'train' and a.judgement='N') train_n,
  sum(d.documentSet = 'train' and a.judgement='Y') train_y,
  sum(d.documentSet = 'train' and a.judgement='Q') train_q,
  sum(d.documentSet = 'test' and a.judgement='N') test_n,
  sum(d.documentSet = 'test' and a.judgement='Y') test_y,
  sum(d.documentSet = 'test' and a.judgement='Q') test_q
from i2b2_2008_anno a
inner join i2b2_2008_doc d on d.docId = a.docId
where a.source = 'intuitive'
group by a.disease
;
