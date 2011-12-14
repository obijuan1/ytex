select cast(concat('label.', label, '.export.cutoff=', param1) as char(100))
from cv_best_svm where experiment = 'superlincutoff'

union

select cast(concat('label.', label, '.', param1, '.export.param2.list=', param2) as char(100))
from cv_best_svm where experiment = 'superlincutoff'

;

