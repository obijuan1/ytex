create index ix_term on mesh_freq(term);

/* fix bogus mesh codes */
update mesh_freq set code = null where length(code) = 3;
update mesh_freq f inner join mesh_concept c on c.conceptString = f.term and f.code is null
set f.code = c.descriptorUI
;

/* set the umls cui to the mesh preferred cui */
update mesh_freq f inner join mesh_concept c on f.code = c.descriptorUI and c.preferredConcept = 1
set f.cui = c.conceptUMLSUI
;

create index ix_code on mesh_freq(code);
create index ix_cui on mesh_freq(cui);
