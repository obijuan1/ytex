/*
 * semantic kernel uses the best hotspots from the cui experiment.
 * update the param1 parameter (which wasn't varied in the cross validation)
 * to the hotspot cutoff from the cui experiment. 
 */
update classifier_eval e
inner join cv_best_svm s 
	on e.label = s.label 
	and e.name = s.corpus_name
set e.param1 = s.param1
where e.name = '@kernel.name@'
and e.experiment = '@kernel.experiment@'
and s.experiment = '@kernel.param1.experiment@'
;