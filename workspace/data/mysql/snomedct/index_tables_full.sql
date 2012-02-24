alter table sct2f_description 
add KEY `sct2f_description_concept` (`conceptId`);

alter table sct2f_relationship 
add KEY `sct2f_relationship_source` (`sourceId`,`characteristicTypeId`,`typeId`,`destinationId`);

alter table sct2f_relationship 
add KEY `sct2f_relationship_dest` (`destinationId`,`characteristicTypeId`,`typeId`);
