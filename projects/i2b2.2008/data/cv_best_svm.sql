-- MySQL dump 10.13  Distrib 5.5.8, for Win64 (x86)
--
-- Host: localhost    Database: ytex
-- ------------------------------------------------------
-- Server version	5.5.8

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `cv_best_svm`
--

DROP TABLE IF EXISTS `cv_best_svm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cv_best_svm` (
  `corpus_name` varchar(50) NOT NULL,
  `label` varchar(50) NOT NULL,
  `experiment` varchar(50) NOT NULL DEFAULT '',
  `f1` double DEFAULT NULL,
  `kernel` int(11) DEFAULT NULL,
  `cost` double DEFAULT NULL,
  `weight` varchar(50) DEFAULT NULL,
  `param1` double DEFAULT NULL,
  `param2` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`corpus_name`,`label`,`experiment`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='best svm params based on cv';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cv_best_svm`
--

LOCK TABLES `cv_best_svm` WRITE;
/*!40000 ALTER TABLE `cv_best_svm` DISABLE KEYS */;
INSERT INTO `cv_best_svm` VALUES ('i2b2.2008','Asthma','word',0.963,0,0.0001,NULL,1,NULL),('i2b2.2008','CAD','word',0.947,0,0.1,NULL,7,NULL),('i2b2.2008','CHF','word',0.893,0,0.01,NULL,7,NULL),('i2b2.2008','Depression','word',0.975,0,0.001,NULL,8,NULL),('i2b2.2008','Diabetes','word',0.954,0,0.1,NULL,12,NULL),('i2b2.2008','Gallstones','word',0.962,0,0.00001,NULL,4,NULL),('i2b2.2008','GERD','word',0.895,0,0.1,NULL,3,NULL),('i2b2.2008','Gout','word',0.988,0,0.00001,NULL,3,NULL),('i2b2.2008','Hypercholesterolemia','word',0.912,0,0.01,NULL,12,NULL),('i2b2.2008','Hypertension','word',0.924,0,0.1,NULL,3,NULL),('i2b2.2008','Hypertriglyceridemia','word',0.927,0,0.001,NULL,7,NULL),('i2b2.2008','OA','word',0.897,0,0.001,NULL,5,NULL),('i2b2.2008','Obesity','word',0.973,0,0.00001,NULL,3,NULL),('i2b2.2008','OSA','word',0.963,0,0.1,NULL,4,NULL),('i2b2.2008','PVD','word',0.888,0,0.01,NULL,1,NULL),('i2b2.2008','VI','word',0.682,0,0.1,NULL,2,NULL),('i2b2.2008','VI','imputed',0.718,0,0.1,NULL,7,NULL),('i2b2.2008','PVD','imputed',0.883,0,0.1,NULL,2,NULL),('i2b2.2008','Obesity','imputed',0.973,0,0.00001,NULL,1,NULL),('i2b2.2008','OSA','imputed',0.96,0,0.1,NULL,6,NULL),('i2b2.2008','OA','imputed',0.917,0,0.1,NULL,6,NULL),('i2b2.2008','Hypertriglyceridemia','imputed',0.929,0,0.001,NULL,5,NULL),('i2b2.2008','Hypertension','imputed',0.928,0,0.0001,NULL,2,NULL),('i2b2.2008','Hypercholesterolemia','imputed',0.92,0,0.01,NULL,5,NULL),('i2b2.2008','Gout','imputed',0.991,0,0.00001,NULL,5,NULL),('i2b2.2008','GERD','imputed',0.903,0,0.1,NULL,1,NULL),('i2b2.2008','Gallstones','imputed',0.962,0,0.00001,NULL,1,NULL),('i2b2.2008','Diabetes','imputed',0.97,0,0.1,NULL,8,NULL),('i2b2.2008','Depression','imputed',0.974,0,0.0001,NULL,1,NULL),('i2b2.2008','VI','cui',0.766,0,0.1,NULL,6,NULL),('i2b2.2008','PVD','cui',0.901,0,0.1,NULL,2,NULL),('i2b2.2008','Obesity','cui',0.973,0,0.00001,NULL,1,NULL),('i2b2.2008','OSA','cui',0.969,0,0.1,NULL,2,NULL),('i2b2.2008','OA','cui',0.923,0,0.1,NULL,6,NULL),('i2b2.2008','Hypertriglyceridemia','cui',0.932,0,0.00001,NULL,5,NULL),('i2b2.2008','Hypertension','cui',0.926,0,0.1,NULL,2,NULL),('i2b2.2008','Hypercholesterolemia','cui',0.92,0,0.01,NULL,5,NULL),('i2b2.2008','Gout','cui',0.991,0,0.00001,NULL,5,NULL),('i2b2.2008','GERD','cui',0.901,0,0.0001,NULL,1,NULL),('i2b2.2008','Gallstones','cui',0.961,0,0.00001,NULL,1,NULL),('i2b2.2008','Diabetes','cui',0.971,0,0.1,NULL,8,NULL),('i2b2.2008','Depression','cui',0.977,0,0.001,NULL,1,NULL),('i2b2.2008','CHF','imputed',0.898,0,0.01,NULL,3,NULL),('i2b2.2008','VI','superlin',0.765,4,0.1,NULL,6,'1'),('i2b2.2008','PVD','superlin',0.897,4,0.1,NULL,2,'1'),('i2b2.2008','OSA','superlin',0.969,4,0.1,NULL,2,'1'),('i2b2.2008','Obesity','superlin',0.973,4,0.000001,NULL,1,'1'),('i2b2.2008','OA','superlin',0.922,4,0.1,NULL,6,'1'),('i2b2.2008','Hypertriglyceridemia','superlin',0.932,4,0.00001,NULL,5,'1'),('i2b2.2008','Hypertension','superlin',0.925,4,0.1,NULL,2,'1'),('i2b2.2008','Hypercholesterolemia','superlin',0.922,4,0.1,NULL,5,'1'),('i2b2.2008','Gout','superlin',0.991,4,0.000001,NULL,5,'1'),('i2b2.2008','GERD','superlin',0.901,4,0.0001,NULL,1,'1'),('i2b2.2008','Gallstones','superlin',0.962,4,0.000001,NULL,1,'1'),('i2b2.2008','Diabetes','superlin',0.973,4,0.1,NULL,8,'1'),('i2b2.2008','Depression','superlin',0.977,4,0.01,NULL,1,'1'),('i2b2.2008','CHF','superlin',0.903,4,0.01,NULL,3,'1'),('i2b2.2008','CAD','superlin',0.947,4,0.0001,NULL,2,'1'),('i2b2.2008','Asthma','superlin',0.963,4,0.0001,NULL,1,'1'),('i2b2.2008','Asthma','dotkern',0.963,4,0.000001,NULL,1,NULL),('i2b2.2008','CAD','dotkern',0.952,4,0.1,NULL,6,NULL),('i2b2.2008','CHF','dotkern',0.906,4,0.01,NULL,3,NULL),('i2b2.2008','Depression','dotkern',0.981,4,0.01,NULL,1,NULL),('i2b2.2008','Diabetes','dotkern',0.965,4,0.1,NULL,10,NULL),('i2b2.2008','Gallstones','dotkern',0.962,4,0.000001,NULL,1,NULL),('i2b2.2008','GERD','dotkern',0.904,4,0.1,NULL,1,NULL),('i2b2.2008','Gout','dotkern',0.991,4,0.000001,NULL,5,NULL),('i2b2.2008','Hypercholesterolemia','dotkern',0.92,4,0.01,NULL,5,NULL),('i2b2.2008','Hypertension','dotkern',0.922,4,0.1,NULL,2,NULL),('i2b2.2008','Hypertriglyceridemia','dotkern',0.926,4,0.00001,NULL,10,NULL),('i2b2.2008','OA','dotkern',0.9,4,0.1,NULL,8,NULL),('i2b2.2008','Obesity','dotkern',0.973,4,0.000001,NULL,1,NULL),('i2b2.2008','OSA','dotkern',0.971,4,0.1,NULL,2,NULL),('i2b2.2008','PVD','dotkern',0.901,4,0.01,NULL,2,NULL),('i2b2.2008','VI','dotkern',0.757,4,0.1,NULL,6,NULL),('i2b2.2008','VI','lin',0.754,4,0.1,NULL,6,NULL),('i2b2.2008','PVD','lin',0.86,4,0.01,NULL,2,NULL),('i2b2.2008','OSA','lin',0.967,4,0.1,NULL,2,NULL),('i2b2.2008','Obesity','lin',0.973,4,0.000001,NULL,1,NULL),('i2b2.2008','OA','lin',0.886,4,0.0001,NULL,6,NULL),('i2b2.2008','Hypertriglyceridemia','lin',0.934,4,0.0001,NULL,5,NULL),('i2b2.2008','Hypertension','lin',0.926,4,0.0001,NULL,2,NULL),('i2b2.2008','Hypercholesterolemia','lin',0.914,4,0.01,NULL,5,NULL),('i2b2.2008','Gout','lin',0.991,4,0.000001,NULL,5,NULL),('i2b2.2008','GERD','lin',0.904,4,0.1,NULL,1,NULL),('i2b2.2008','Gallstones','lin',0.963,4,1,NULL,1,NULL),('i2b2.2008','Diabetes','lin',0.964,4,0.01,NULL,8,NULL),('i2b2.2008','Depression','lin',0.976,4,0.01,NULL,1,NULL),('i2b2.2008','CHF','lin',0.908,4,0.01,NULL,3,NULL),('i2b2.2008','CAD','lin',0.937,4,0.01,NULL,2,NULL),('i2b2.2008','Asthma','lin',0.963,4,0.00001,NULL,1,NULL),('i2b2.2008','CAD','imputed',0.938,0,0.01,NULL,2,NULL),('i2b2.2008','Asthma','imputed',0.963,0,0.0001,NULL,1,NULL),('i2b2.2008','CAD','cui',0.948,0,0.01,NULL,2,NULL),('i2b2.2008','CHF','cui',0.903,0,0.01,NULL,3,NULL),('i2b2.2008','Asthma','cui',0.963,0,0.0001,NULL,1,NULL);
/*!40000 ALTER TABLE `cv_best_svm` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2012-05-24 10:04:15
