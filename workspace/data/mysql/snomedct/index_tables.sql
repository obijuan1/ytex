alter table sct2_description
add KEY `sct2_description_concept` (`conceptId`);

alter table sct2_description 
add KEY `sct2_description_term` (`term`);

alter table sct2_relationship 
add KEY `sct2_relationship_source` (`sourceId`,`characteristicTypeId`,`typeId`,`destinationId`);

alter table sct2_relationship 
add KEY `sct2_relationship_dest` (`destinationId`,`characteristicTypeId`,`typeId`);
