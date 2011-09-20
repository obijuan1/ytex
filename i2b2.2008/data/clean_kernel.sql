delete from kernel_eval 
where experiment in ('kern-cuiword-lin', 'kern-cuiword-filteredlin')
;

delete i
from kernel_eval_instance i
left join kernel_eval e on e.kernel_eval_id = i.kernel_eval_id
where e.kernel_eval_id is null
;
