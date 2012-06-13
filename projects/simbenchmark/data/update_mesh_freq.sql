create index ix_term on mesh_freq(term);

drop table if exists tmp_msh;
create temporary table tmp_msh as select cui, code, cast(str as char(300)) str from umls2011ab.mrconso where sab = 'MSH';
create index ix_str on tmp_msh(str);

update mesh_freq f 
inner join tmp_msh m on f.term = m.str 
set f.cui = m.cui,
f.code = m.code
;
