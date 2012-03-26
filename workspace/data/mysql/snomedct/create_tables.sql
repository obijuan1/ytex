-- table definitions taken from SNOMED CT Technical Implementation Guide July 2011, section 7.2.1.3.2.
-- modified active flag to use bit instead of tinyint
-- modified effectiveTime to use date instead of datetime

drop table if exists sct2_concept;
drop table if exists sct2_description;
drop table if exists sct2_relationship;

CREATE TABLE `sct2_concept` (
	`id` BIGINT NOT NULL DEFAULT 0,
	`effectiveTime` DATE NOT NULL DEFAULT '0000-00-00',
	`active` bit NOT NULL DEFAULT 0,
	`moduleId` BIGINT NOT NULL DEFAULT 0,
	`definitionStatusId` BIGINT NOT NULL DEFAULT 0,
	PRIMARY KEY (`id`,`effectiveTime`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;


CREATE TABLE `sct2_description` (
	`id` BIGINT NOT NULL DEFAULT 0,
	`effectiveTime` DATE NOT NULL DEFAULT '0000-00-00',
	`active` bit NOT NULL DEFAULT 0,
	`moduleId` BIGINT NOT NULL DEFAULT 0,
	`conceptId` BIGINT NOT NULL DEFAULT 0,
	`languageCode` VARCHAR(3) NOT NULL DEFAULT '',
	`typeId` BIGINT NOT NULL DEFAULT 0,
	`term` VARCHAR(255) NOT NULL DEFAULT '',
	`caseSignificanceId` BIGINT NOT NULL DEFAULT 0,
	PRIMARY KEY (`id`,`effectiveTime`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `sct2_relationship` (
	`id` BIGINT NOT NULL DEFAULT 0,
	`effectiveTime` DATE NOT NULL DEFAULT '0000-00-00',
	`active` bit NOT NULL DEFAULT 0,
	`moduleId` BIGINT NOT NULL DEFAULT 0,
	`sourceId` BIGINT NOT NULL DEFAULT 0,
	`destinationId` BIGINT NOT NULL DEFAULT 0,
	`relationshipGroup` INT NOT NULL DEFAULT 0,
	`typeId` BIGINT NOT NULL DEFAULT 0,
	`characteristicTypeId` BIGINT NOT NULL DEFAULT 0,
	`modifierId` BIGINT NOT NULL DEFAULT 0,
	PRIMARY KEY (`id`,`effectiveTime`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
