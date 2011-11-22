select concat('label.', label, '.kernel.param2=', param2) 
from cv_best_svm 
where experiment = '@kernel.cv.experiment@' 
and param2 is not null; 