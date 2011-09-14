DROP TABLE IF EXISTS `ytex`.`suj_concept`;
CREATE TABLE  `ytex`.`suj_concept` (
  `concept_id` int(11) NOT NULL AUTO_INCREMENT,
  `norm_term_id` int(11) DEFAULT NULL,
  `cui` char(8) DEFAULT NULL,
  PRIMARY KEY (`concept_id`),
  KEY `IX_norm_term_id` (`norm_term_id`)
) ENGINE=MyISAM AUTO_INCREMENT=119201 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `ytex`.`suj_lexical_unit`;
CREATE TABLE  `ytex`.`suj_lexical_unit` (
  `lexical_unit_id` int(11) NOT NULL AUTO_INCREMENT,
  `document_id` int(11) NOT NULL,
  `clinicalObs` bit(1) DEFAULT b'0',
  `lexUnit` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`lexical_unit_id`),
  KEY `IX_document_id` (`document_id`)
) ENGINE=MyISAM AUTO_INCREMENT=48689 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `ytex`.`suj_norm_term`;
CREATE TABLE  `ytex`.`suj_norm_term` (
  `norm_term_id` int(11) NOT NULL AUTO_INCREMENT,
  `lexical_unit_id` int(11) NOT NULL,
  `normTerm` varchar(256) DEFAULT NULL,
  `num_concepts` int(11) DEFAULT NULL,
  PRIMARY KEY (`norm_term_id`),
  KEY `IX_lexical_unit_id` (`lexical_unit_id`),
  KEY `normTerm` (`normTerm`)
) ENGINE=MyISAM AUTO_INCREMENT=52677 DEFAULT CHARSET=utf8;

