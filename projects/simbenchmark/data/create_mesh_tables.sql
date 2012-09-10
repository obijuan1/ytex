drop table if exists mesh_concept;
drop table if exists mesh_treenumber;
drop table if exists mesh_hier;
drop table if exists mesh_freq;

CREATE TABLE `mesh_concept` (
	descriptorUI CHAR(7) NOT NULL,
	conceptUI CHAR(8) NOT NULL,
	conceptUMLSUI CHAR(8) NULL,
	conceptString varchar(300) NOT NULL,
	preferredConcept bit not null default 0,
	PRIMARY KEY (`descriptorUI`,`conceptUI`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `mesh_treenumber` (
	descriptorUI CHAR(7) NOT NULL,
	treeNumber VARCHAR(50) NOT NULL,
	PRIMARY KEY (`descriptorUI`,`treeNumber`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

create table mesh_hier (
    parUI char(7) not null,
    chdUI char(7) not null,
    rel char(5) null comment 'one of pharm, tree, or head',
    primary key (parUI, chdUI, rel)
) ENGINE=MyISAM DEFAULT CHARSET=utf8
;

create table mesh_freq (
	mesh_freq_id int auto_increment not null primary key,
	year tinyint,
	source varchar(30),
	freq int,
	term varchar(300),
	cui char(8),
	code char(7)
) ENGINE=MyISAM DEFAULT CHARSET=utf8
;