DROP TABLE IF EXISTS `sp_db`.`context`;
CREATE TABLE  `sp_db`.`context` (
  `id` int(10) unsigned NOT NULL,
  `vertexId` int(10) unsigned NOT NULL,
  `text` varchar(500) NOT NULL,
  `word` varchar(50) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FK_context_1` (`vertexId`),
  CONSTRAINT `FK_context_1` FOREIGN KEY (`vertexId`) REFERENCES `vertex` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `sp_db`.`ontology`;
CREATE TABLE  `sp_db`.`ontology` (
  `id` int(10) unsigned NOT NULL,
  `name` varchar(45) NOT NULL,
  `hierarchyFile` varchar(200) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `sp_db`.`profile`;
CREATE TABLE  `sp_db`.`profile` (
  `id` int(10) unsigned NOT NULL,
  `time` datetime NOT NULL,
  `name` varchar(45) NOT NULL,
  `ontologyId` int(10) unsigned NOT NULL,
  `graph` longtext NOT NULL,
  `wordTokens` int(10) unsigned NOT NULL,
  `relWordTokens` int(10) unsigned NOT NULL,
  `totalCat` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_profile_1` (`ontologyId`),
  CONSTRAINT `FK_profile_1` FOREIGN KEY (`ontologyId`) REFERENCES `ontology` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `sp_db`.`vertex`;
CREATE TABLE  `sp_db`.`vertex` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `profileId` int(10) unsigned NOT NULL,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FK_vertex_1` (`profileId`),
  CONSTRAINT `FK_vertex_1` FOREIGN KEY (`profileId`) REFERENCES `profile` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=179 DEFAULT CHARSET=latin1;