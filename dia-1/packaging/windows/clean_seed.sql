-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: local
-- ------------------------------------------------------
-- Server version	8.0.40

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `local`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `local` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `local`;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `CATEGORY_ID` bigint NOT NULL,
  `CATEGORY_NAME` varchar(100) NOT NULL,
  `DESCRIPTION` varchar(255) DEFAULT NULL,
  `ASSOCIATED_FIELDS` varchar(255) DEFAULT NULL,
  `PARENT_CATEGORY_ID` bigint DEFAULT NULL,
  PRIMARY KEY (`CATEGORY_ID`),
  KEY `FK_PARENT_CATEGORY` (`PARENT_CATEGORY_ID`),
  CONSTRAINT `FK_PARENT_CATEGORY` FOREIGN KEY (`PARENT_CATEGORY_ID`) REFERENCES `categories` (`CATEGORY_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (1,'Diamond','Diamond category',NULL,NULL),(2,'Open Setting','Jewelry worn on the ears.','productName,price,stockQuantity,categoryId,imageUrl,gross,net,vilandiCt,diamondsCt,beadsCt,pearlsGm,otherStonesCt,others',NULL),(3,'Plain Gold','Plain Gold category',NULL,NULL),(4,'Vilandi','Jewelry made of interlinked metal links.','productName,price,stockQuantity,categoryId,imageUrl,designNo,net,stones,beadsCt,pearlsGm,ssPearlCt,realStone,remarks',NULL),(5,'Jadtar','Jadtar category',NULL,NULL),(6,'Jadtar Register','Description for Jadtar Register','productName,price,stockQuantity,categoryId,imageUrl,designNo,gross,net,stones,beadsCt,pearlsGm,ssPearlCt,realStone,remarks',5),(7,'Jadtar Halfsets','Halfset under Jadtar','productName,price,stockQuantity,categoryId,imageUrl,designNo,gross,net,stones,beadsCt,pearlsGm,ssPearlCt,realStone,remarks',5),(8,'Jadtar Bangles / Bracelets','Bangles under Jadtar','productName,price,stockQuantity,categoryId,imageUrl,designNo,gross,net,stones,beadsCt,pearlsGm,ssPearlCt,realStone,remarks',5),(9,'Vilandi Halfsets','Halfset under Vilandi','productName,price,stockQuantity,categoryId,imageUrl,designNo,net,stones,beadsCt,pearlsGm,ssPearlCt,realStone,remarks',4),(10,'Vilandi Bangles / Bracelets','Bangles under Vilandi','productName,price,stockQuantity,categoryId,imageUrl,designNo,net,stones,beadsCt,pearlsGm,ssPearlCt,realStone,remarks',4),(11,'Diamond Bangles / Bracelets','Diamond bangles or bracelets','productName,price,stockQuantity,categoryId,imageUrl,designNo,net,pcs,diaWeight,remarks',1),(12,'Diamond Pendants / Pendant Sets','Diamond pendants or pendant sets','productName,price,stockQuantity,categoryId,imageUrl,designNo,net,pcs,diaWeight,remarks',1),(13,'Diamond Halfsets','Halfset under Diamond category','productName,price,stockQuantity,categoryId,imageUrl,designNo,net,pcs,diaWeight,remarks',1),(14,'OS Halfsets','Open Setting Halfsets','productName,price,stockQuantity,categoryId,imageUrl,gross,net,vilandiCt,diamondsCt,beadsCt,pearlsGm,otherStonesCt,others',2),(15,'OS Bangles / Bracelets','Open Setting Bangles / Bracelets','productName,price,stockQuantity,categoryId,imageUrl,gross,net,vilandiCt,diamondsCt,beadsCt,pearlsGm,otherStonesCt,others',2),(16,'PG Bangles / Bracelets','Plain Gold Bangles or Bracelets','productName,price,stockQuantity,categoryId,imageUrl,designNo,net',3),(17,'Only Earrings','Jadtar-style earrings','productName,price,stockQuantity,categoryId,imageUrl,designNo,gross,net,stones,beadsCt,pearlsGm,ssPearlCt,realStone,remarks',5),(18,'Chains','Rigid bracelets worn around the wrist.','productName,price,stockQuantity,categoryId,imageUrl,designNo,net',3),(19,'Diamond Earrings','A decorative item worn on the nose.','productName,price,stockQuantity,categoryId,imageUrl,designNo,net,pcs,diaWeight,remarks',1),(20,'Diamond Rings','A piece of jewelry worn around the neck.','productName,price,stockQuantity,categoryId,imageUrl,net,pcs,diaWeight,remarks',1);
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `enquiry_log`
--

DROP TABLE IF EXISTS `enquiry_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `enquiry_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `design_no` varchar(50) DEFAULT NULL,
  `customer_name` varchar(255) DEFAULT NULL,
  `image_url` varchar(500) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `product_snapshot` json DEFAULT NULL,
  `price` double DEFAULT NULL,
  `price_with_fields` double DEFAULT NULL,
  `estimate_snapshot` json DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `enquiry_log`
--

LOCK TABLES `enquiry_log` WRITE;
/*!40000 ALTER TABLE `enquiry_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `enquiry_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` int NOT NULL AUTO_INCREMENT,
  `category_id` bigint NOT NULL,
  `order_id` varchar(20) NOT NULL,
  `is_assigned` tinyint(1) DEFAULT '0',
  `assigned_product_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_id` (`order_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_assigned_product_id` (`assigned_product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9792 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `product_id` bigint NOT NULL AUTO_INCREMENT,
  `parent_category_id` bigint DEFAULT NULL,
  `sub_category_id` bigint DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  `item` varchar(100) NOT NULL,
  `net` decimal(10,3) DEFAULT NULL,
  `pcs` int DEFAULT NULL,
  `dia_weight` decimal(10,2) DEFAULT NULL,
  `remarks` varchar(255) DEFAULT NULL,
  `gross` decimal(10,3) DEFAULT NULL,
  `vilandi_ct` decimal(10,2) DEFAULT NULL,
  `diamonds_ct` decimal(10,2) DEFAULT NULL,
  `beads_ct` decimal(10,2) DEFAULT NULL,
  `pearls_gm` decimal(10,3) DEFAULT NULL,
  `other_stones_ct` decimal(10,2) DEFAULT NULL,
  `others` varchar(255) DEFAULT NULL,
  `design_no` varchar(100) DEFAULT NULL,
  `diamond_weight` decimal(10,2) DEFAULT NULL,
  `stones` decimal(10,2) DEFAULT NULL,
  `ss_pearl_ct` decimal(10,2) DEFAULT NULL,
  `real_stone` decimal(10,2) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `stock_quantity` int DEFAULT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `st_rate` decimal(10,2) DEFAULT NULL,
  `bd_rate` decimal(10,2) DEFAULT NULL,
  `prl_rate` decimal(10,2) DEFAULT NULL,
  `ss_rate` decimal(10,2) DEFAULT NULL,
  `fitting` varchar(50) DEFAULT NULL,
  `mozonite` varchar(50) DEFAULT NULL,
  `vilandi_rate` decimal(10,2) DEFAULT NULL,
  `m_rate` decimal(10,2) DEFAULT NULL,
  `create_date_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_date_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `labour_gm` decimal(10,3) DEFAULT NULL,
  `labour_all` decimal(10,2) DEFAULT NULL,
  `labour_p` decimal(10,2) DEFAULT NULL,
  `karat` decimal(10,2) DEFAULT NULL,
  `other_stones_rate` decimal(10,2) DEFAULT NULL,
  `diamond_rate` decimal(10,2) DEFAULT NULL,
  `order_ref_id` int DEFAULT NULL,
  `custom_fields` json DEFAULT NULL COMMENT 'Stores additional custom fields as JSON key-value pairs',
  `price_with_fields` decimal(10,2) DEFAULT NULL,
  `qr_code_path` varchar(255) DEFAULT NULL,
  `verification_status` tinyint NOT NULL DEFAULT '-1' COMMENT '0 = Unverified, 1 = Verified, -1 = Not Set',
  `verification_date` datetime DEFAULT NULL COMMENT 'Date of verification',
  PRIMARY KEY (`product_id`),
  UNIQUE KEY `order_ref_id` (`order_ref_id`),
  KEY `CATEGORY_ID` (`category_id`),
  KEY `fk_product_parent_category` (`parent_category_id`),
  KEY `fk_product_sub_category` (`sub_category_id`),
  CONSTRAINT `fk_product_parent_category` FOREIGN KEY (`parent_category_id`) REFERENCES `categories` (`CATEGORY_ID`),
  CONSTRAINT `fk_product_sub_category` FOREIGN KEY (`sub_category_id`) REFERENCES `categories` (`CATEGORY_ID`),
  CONSTRAINT `products_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`CATEGORY_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=217 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rate_history`
--

DROP TABLE IF EXISTS `rate_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rate_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `rate_id` bigint DEFAULT NULL,
  `commodity` varchar(50) NOT NULL,
  `old_price` decimal(10,2) DEFAULT NULL,
  `new_price` decimal(10,2) NOT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rate_history`
--

LOCK TABLES `rate_history` WRITE;
/*!40000 ALTER TABLE `rate_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `rate_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rates`
--

DROP TABLE IF EXISTS `rates`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rates` (
  `ID` bigint NOT NULL AUTO_INCREMENT,
  `COMMODITY` varchar(50) NOT NULL,
  `PRICE` decimal(10,2) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rates`
--

LOCK TABLES `rates` WRITE;
/*!40000 ALTER TABLE `rates` DISABLE KEYS */;
INSERT INTO `rates` VALUES (3,'diamond',60000.00),(4,'gst',3.00),(5,'silver',250000.00),(11,'10.00',56000.00),(12,'14.00',84000.00),(13,'18.00',106400.00),(14,'22.00',128340.00),(15,'24.00',140000.00);
/*!40000 ALTER TABLE `rates` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `ROLE_ID` bigint NOT NULL,
  `ROLE_NAME` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ROLE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'ROLE_ADMIN'),(2,'ROLE_EDITOR'),(3,'ROLE_VIEWER');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sales_log`
--

DROP TABLE IF EXISTS `sales_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `design_no` varchar(50) DEFAULT NULL,
  `order_id` varchar(50) DEFAULT NULL,
  `customer_name` varchar(255) DEFAULT NULL,
  `image_url` varchar(500) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `product_snapshot` json DEFAULT NULL,
  `price` double DEFAULT NULL,
  `price_with_fields` double DEFAULT NULL,
  `estimate_snapshot` json DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sales_log`
--

LOCK TABLES `sales_log` WRITE;
/*!40000 ALTER TABLE `sales_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `sales_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_roles`
--

DROP TABLE IF EXISTS `user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_roles` (
  `USER_ID` bigint NOT NULL,
  `ROLE_ID` bigint NOT NULL,
  PRIMARY KEY (`USER_ID`,`ROLE_ID`),
  KEY `ROLE_ID` (`ROLE_ID`),
  CONSTRAINT `user_roles_ibfk_1` FOREIGN KEY (`USER_ID`) REFERENCES `users` (`USER_ID`),
  CONSTRAINT `user_roles_ibfk_2` FOREIGN KEY (`ROLE_ID`) REFERENCES `roles` (`ROLE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_roles`
--

LOCK TABLES `user_roles` WRITE;
/*!40000 ALTER TABLE `user_roles` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `USER_ID` bigint NOT NULL AUTO_INCREMENT,
  `USER_NAME` varchar(255) DEFAULT NULL,
  `PASSWORD` varchar(255) DEFAULT NULL,
  `security_question` varchar(255) NOT NULL,
  `security_answer` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`USER_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `verification_config`
--

DROP TABLE IF EXISTS `verification_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `verification_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `verification_days` int NOT NULL DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `verification_config`
--

LOCK TABLES `verification_config` WRITE;
/*!40000 ALTER TABLE `verification_config` DISABLE KEYS */;
INSERT INTO `verification_config` VALUES (1,10,'2025-10-03 19:23:35','2025-11-16 07:50:04');
/*!40000 ALTER TABLE `verification_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'local'
--

--
-- Dumping routines for database 'local'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-12 21:07:06
