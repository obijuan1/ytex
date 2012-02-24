drop table if exists mesh_concept;
drop table if exists mesh_treenumber;

CREATE TABLE `mesh_concept` (
	descriptorUI CHAR(7) NOT NULL,
	conceptUI CHAR(8) NOT NULL,
	conceptUMLSUI CHAR(8) NULL,
	conceptString varchar(200) NOT NULL,
	PRIMARY KEY (`descriptorUI`,`conceptUI`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `mesh_treenumber` (
	descriptorUI CHAR(7) NOT NULL,
	treeNumber VARCHAR(50) NOT NULL,
	PRIMARY KEY (`descriptorUI`,`treeNumber`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

drop table if exists mesh_hier;

create table mesh_hier (
    parUI char(7) not null,
    chdUI char(7) not null,
    rel char(5) null comment 'one of pharm, tree, or head',
    primary key (parUI, chdUI, rel)
) ENGINE=MyISAM DEFAULT CHARSET=utf8
;