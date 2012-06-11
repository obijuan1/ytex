select concat('u:', chdUI, ' v:', parUI, ' d:1 s:mesh')
from mesh_hier 
union
select concat('u:', parUI, ' v:', chdUI, ' d:1 s:mesh')
from mesh_hier 
;