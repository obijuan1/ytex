create index ix_term on mesh_freq(term);

update mesh_freq set code = null where length(code) = 3;

update mesh_freq f inner join mesh_concept c on c.conceptString = f.term and f.code is null
set f.code = c.descriptorUI
;

update mesh_freq f inner join @umls.schema@.MRCONSO c on f.code = c.code and c.sab = 'msh'
set f.cui = c.cui
;


