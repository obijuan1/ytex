select concat(label, '.kernel=0')
from i2b2_best_libsvm where experiment = 'bocuis'
union
select concat(label, '.cost=', cost) cost
from i2b2_best_libsvm where experiment = 'bocuis'
;