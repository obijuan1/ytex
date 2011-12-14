-- MySQL dump 10.11
--
-- Host: ristretto.med.yale.edu    Database: ytex2
-- ------------------------------------------------------
-- Server version	5.1.41-3ubuntu12.7

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
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
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
) ENGINE=MyISAM COMMENT='best svm params based on cv';
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `cv_best_svm`
--

LOCK TABLES `cv_best_svm` WRITE;
/*!40000 ALTER TABLE `cv_best_svm` DISABLE KEYS */;
INSERT INTO `cv_best_svm` VALUES ('i2b2.2008','Asthma','word',0.963,0,0.0001,NULL,1,NULL),('i2b2.2008','CAD','word',0.947,0,0.1,NULL,7,NULL),('i2b2.2008','CHF','word',0.893,0,0.01,NULL,7,NULL),('i2b2.2008','Depression','word',0.975,0,0.001,NULL,8,NULL),('i2b2.2008','Diabetes','word',0.954,0,0.1,NULL,12,NULL),('i2b2.2008','Gallstones','word',0.962,0,1e-05,NULL,4,NULL),('i2b2.2008','GERD','word',0.895,0,0.1,NULL,3,NULL),('i2b2.2008','Gout','word',0.988,0,1e-05,NULL,3,NULL),('i2b2.2008','Hypercholesterolemia','word',0.912,0,0.01,NULL,12,NULL),('i2b2.2008','Hypertension','word',0.924,0,0.1,NULL,3,NULL),('i2b2.2008','Hypertriglyceridemia','word',0.927,0,0.001,NULL,7,NULL),('i2b2.2008','OA','word',0.897,0,0.001,NULL,5,NULL),('i2b2.2008','Obesity','word',0.973,0,1e-05,NULL,3,NULL),('i2b2.2008','OSA','word',0.963,0,0.1,NULL,4,NULL),('i2b2.2008','PVD','word',0.888,0,0.01,NULL,1,NULL),('i2b2.2008','VI','word',0.682,0,0.1,NULL,2,NULL),('i2b2.2008','Obesity','imputed',0.973,0,1e-05,NULL,1,NULL),('i2b2.2008','OSA','imputed',0.965,0,0.1,NULL,1,NULL),('i2b2.2008','Depression','imputed',0.976,0,0.01,NULL,1,NULL),('i2b2.2008','VI','imputed',0.74,0,0.1,NULL,6,NULL),('i2b2.2008','PVD','imputed',0.896,0,0.1,NULL,2,NULL),('i2b2.2008','OA','imputed',0.877,0,0.1,NULL,8,NULL),('i2b2.2008','Hypertriglyceridemia','imputed',0.924,0,0.1,NULL,9,NULL),('i2b2.2008','Hypertension','imputed',0.926,0,0.1,NULL,2,NULL),('i2b2.2008','Hypercholesterolemia','imputed',0.919,0,0.01,NULL,5,NULL),('i2b2.2008','Gout','imputed',0.991,0,1e-05,NULL,5,NULL),('i2b2.2008','GERD','imputed',0.903,0,0.0001,NULL,1,NULL),('i2b2.2008','Gallstones','imputed',0.962,0,1e-05,NULL,1,NULL),('i2b2.2008','Diabetes','imputed',0.964,0,0.1,NULL,7,NULL),('i2b2.2008','CAD','imputed',0.941,0,0.1,NULL,5,NULL),('i2b2.2008','CHF','imputed',0.9,0,0.01,NULL,3,NULL),('i2b2.2008','CAD','cui',0.952,0,0.1,NULL,6,NULL),('i2b2.2008','VI','cui',0.757,0,0.1,NULL,6,NULL),('i2b2.2008','PVD','cui',0.901,0,0.01,NULL,2,NULL),('i2b2.2008','OSA','cui',0.971,0,0.1,NULL,2,NULL),('i2b2.2008','Obesity','cui',0.973,0,1e-05,NULL,1,NULL),('i2b2.2008','OA','cui',0.9,0,0.1,NULL,8,NULL),('i2b2.2008','Hypertriglyceridemia','cui',0.926,0,1e-05,NULL,10,NULL),('i2b2.2008','Hypertension','cui',0.922,0,0.1,NULL,2,NULL),('i2b2.2008','Hypercholesterolemia','cui',0.92,0,0.01,NULL,5,NULL),('i2b2.2008','Gout','cui',0.991,0,1e-05,NULL,5,NULL),('i2b2.2008','GERD','cui',0.904,0,0.1,NULL,1,NULL),('i2b2.2008','Gallstones','cui',0.962,0,1e-05,NULL,1,NULL),('i2b2.2008','Diabetes','cui',0.965,0,0.1,NULL,10,NULL),('i2b2.2008','Depression','cui',0.981,0,0.01,NULL,1,NULL),('i2b2.2008','CHF','cui',0.906,0,0.01,NULL,3,NULL),('i2b2.2008','Asthma','cui',0.963,0,0.0001,NULL,1,NULL),('i2b2.2008','Asthma','imputed',0.963,0,1e-05,NULL,1,NULL),('i2b2.2008','Asthma','superlin',0.963,4,1e-06,NULL,1,'1'),('i2b2.2008','CAD','superlin',0.951,4,0.1,NULL,6,'5'),('i2b2.2008','CHF','superlin',0.909,4,0.01,NULL,3,'1'),('i2b2.2008','Depression','superlin',0.976,4,0.01,NULL,1,'1'),('i2b2.2008','Diabetes','superlin',0.969,4,0.1,NULL,10,'1'),('i2b2.2008','Gallstones','superlin',0.962,4,1e-06,NULL,1,'1'),('i2b2.2008','GERD','superlin',0.904,4,0.0001,NULL,1,'3'),('i2b2.2008','Gout','superlin',0.991,4,1e-06,NULL,5,'3'),('i2b2.2008','Hypercholesterolemia','superlin',0.925,4,0.01,NULL,5,'3'),('i2b2.2008','Hypertension','superlin',0.925,4,0.1,NULL,2,'5'),('i2b2.2008','Hypertriglyceridemia','superlin',0.923,4,0.0001,NULL,10,'3'),('i2b2.2008','OA','superlin',0.862,4,0.1,NULL,8,'3'),('i2b2.2008','Obesity','superlin',0.973,4,1e-06,NULL,1,'10'),('i2b2.2008','OSA','superlin',0.965,4,0.01,NULL,2,'1'),('i2b2.2008','PVD','superlin',0.896,4,0.01,NULL,2,'5'),('i2b2.2008','VI','superlin',0.751,4,0.1,NULL,6,'5'),('i2b2.2008','Asthma','dotkern',0.963,4,1e-06,NULL,1,NULL),('i2b2.2008','CAD','dotkern',0.952,4,0.1,NULL,6,NULL),('i2b2.2008','CHF','dotkern',0.906,4,0.01,NULL,3,NULL),('i2b2.2008','Depression','dotkern',0.981,4,0.01,NULL,1,NULL),('i2b2.2008','Diabetes','dotkern',0.965,4,0.1,NULL,10,NULL),('i2b2.2008','Gallstones','dotkern',0.962,4,1e-06,NULL,1,NULL),('i2b2.2008','GERD','dotkern',0.904,4,0.1,NULL,1,NULL),('i2b2.2008','Gout','dotkern',0.991,4,1e-06,NULL,5,NULL),('i2b2.2008','Hypercholesterolemia','dotkern',0.92,4,0.01,NULL,5,NULL),('i2b2.2008','Hypertension','dotkern',0.922,4,0.1,NULL,2,NULL),('i2b2.2008','Hypertriglyceridemia','dotkern',0.926,4,1e-05,NULL,10,NULL),('i2b2.2008','OA','dotkern',0.9,4,0.1,NULL,8,NULL),('i2b2.2008','Obesity','dotkern',0.973,4,1e-06,NULL,1,NULL),('i2b2.2008','OSA','dotkern',0.971,4,0.1,NULL,2,NULL),('i2b2.2008','PVD','dotkern',0.901,4,0.01,NULL,2,NULL),('i2b2.2008','VI','dotkern',0.757,4,0.1,NULL,6,NULL),('i2b2.2008','Asthma','lin',0.963,4,1e-06,NULL,1,NULL),('i2b2.2008','CAD','lin',0.94,4,0.1,NULL,6,NULL),('i2b2.2008','CHF','lin',0.903,4,0.01,NULL,3,NULL),('i2b2.2008','Depression','lin',0.979,4,0.01,NULL,1,NULL),('i2b2.2008','Diabetes','lin',0.965,4,0.1,NULL,10,NULL),('i2b2.2008','Gallstones','lin',0.963,4,0.001,NULL,1,NULL),('i2b2.2008','GERD','lin',0.903,4,0.1,NULL,1,NULL),('i2b2.2008','Gout','lin',0.991,4,1e-06,NULL,5,NULL),('i2b2.2008','Hypercholesterolemia','lin',0.911,4,0.001,NULL,5,NULL),('i2b2.2008','Hypertension','lin',0.923,4,0.1,NULL,2,NULL),('i2b2.2008','Hypertriglyceridemia','lin',0.925,4,1e-05,NULL,10,NULL),('i2b2.2008','OA','lin',0.864,4,0.1,NULL,8,NULL),('i2b2.2008','Obesity','lin',0.973,4,1e-06,NULL,1,NULL),('i2b2.2008','OSA','lin',0.962,4,0.1,NULL,2,NULL),('i2b2.2008','PVD','lin',0.894,4,0.1,NULL,2,NULL),('i2b2.2008','VI','lin',0.746,4,0.1,NULL,6,NULL);
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

-- Dump completed on 2011-12-06 19:13:47
