update classifier_eval e
inner join i2b2_2008_cv_best b on b.label = e.label and b.experiment = 'bag-impcuiword'
set e.param1 = b.param1
where e.experiment = 'kern-cuiword-filteredlin' and e.name = 'i2b2.2008'
;