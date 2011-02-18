-- generate properties file which specifies optimal params per class for svm-train
select concat(label, '.cost=', min_cost) prop
from libsvm_cv_best
union
select concat(label, '.weight=', weight)
from libsvm_cv_best
union
select concat(label, '.scut=', 
	case
		when scutThreshold > 0 then scutThreshold
		else 0.5
	end)
from libsvm_cv_best
union
select concat(label, '.degree=', degree)
from libsvm_cv_best
union
select concat(label, '.gamma=', gamma)
from libsvm_cv_best
union
select concat(label, '.kernel=', kernel)
from libsvm_cv_best
where experiment = '@EXPERIMENT@'
and kernel = best_kernel
;

