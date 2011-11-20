select cast(concat('label', label, '_train_data.kernel.evalLines=-t ', kernel, ' -q -b 1 -c ', cost) as char(200))
from cv_best_svm 
where experiment = '@kernel.cv.experiment@'
and corpus_name = '@kernel.name@'
order by cast(label as decimal(2,0))
;

