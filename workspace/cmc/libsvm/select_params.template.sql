-- generate properties file which specifies optimal params per class for svm-train
select concat(label, '.cost=', min_cost) prop
from libsvm_cv_best
union
select concat(label, '.weight=', weight)
from libsvm_cv_best
union
select concat(label, '.scut=', scutThreshold)
from libsvm_cv_best
where scutThreshold > 0
and experiment = '@EXPERIMENT@'
;

