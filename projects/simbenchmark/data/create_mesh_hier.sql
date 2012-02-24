

-- conditions:
-- treenumber parent substring of child
-- child treenumber length = parent treenumber length + 4
-- child <> parent
insert into mesh_hier (parUI, chdUI)
select parUI, chdUI, 'tree'
from
(
	select distinct p.descriptorUI parUI, c.descriptorUI chdUI
	from mesh_treenumber p
	inner join mesh_treenumber c 
	    on left(c.treenumber, CHAR_LENGTH(p.treenumber)) = p.treenumber 
	    and c.descriptorUI <> p.descriptorUI
	    and char_length(p.treenumber) + 4 = char_length(c.treenumber)
) s
;