select concat('u:', cui1, ' v:', cui2, ' d:1 s:', sab)
from MRREL
where cui1 not in ('C1274012', 'C1274013', 'C1276325', 'C1274014', 'C1274015', 'C1274021', 'C1443286', 'C1274012', 'C2733115')
and cui2 not in ('C1274012', 'C1274013', 'C1276325', 'C1274014', 'C1274015', 'C1274021', 'C1443286', 'C1274012', 'C2733115')
and sab in ('SNOMEDCT', 'MSH' , 'CSP', 'AOD')
;
