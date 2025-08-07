-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: fastfood_database
-- ------------------------------------------------------
-- Server version	8.0.41

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
-- Current Database: `fastfood_database`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `fastfood_database` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `fastfood_database`;

--
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin` (
  `id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` VALUES (1,'admin@gmail.com','admin123',NULL);
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chi_tiet_don_hang`
--

DROP TABLE IF EXISTS `chi_tiet_don_hang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chi_tiet_don_hang` (
  `ma_don_hang` varchar(20) DEFAULT NULL,
  `ma_san_pham` int DEFAULT NULL,
  `so_luong` int NOT NULL,
  KEY `ma_don_hang` (`ma_don_hang`),
  KEY `ma_san_pham` (`ma_san_pham`),
  CONSTRAINT `chi_tiet_don_hang_ibfk_1` FOREIGN KEY (`ma_don_hang`) REFERENCES `don_hang` (`ma_don_hang`),
  CONSTRAINT `chi_tiet_don_hang_ibfk_2` FOREIGN KEY (`ma_san_pham`) REFERENCES `san_pham` (`ma_san_pham`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chi_tiet_don_hang`
--

LOCK TABLES `chi_tiet_don_hang` WRITE;
/*!40000 ALTER TABLE `chi_tiet_don_hang` DISABLE KEYS */;
INSERT INTO `chi_tiet_don_hang` VALUES ('DH1741431403734',13,1),('DH1741431441774',5,1),('DH1741431494560',8,1),('DH1741431548271',16,1),('DH1742811192428',5,1),('DH1742811802995',5,1);
/*!40000 ALTER TABLE `chi_tiet_don_hang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `diem`
--

DROP TABLE IF EXISTS `diem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `diem` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `diem_tich_luy` int NOT NULL DEFAULT '0',
  `ngay_cap_nhat` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `diem_ibfk_1` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `diem`
--

LOCK TABLES `diem` WRITE;
/*!40000 ALTER TABLE `diem` DISABLE KEYS */;
INSERT INTO `diem` VALUES (3,3,11500,'2025-03-24 17:23:23'),(4,0,0,'2025-03-08 03:19:16');
/*!40000 ALTER TABLE `diem` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `don_hang`
--

DROP TABLE IF EXISTS `don_hang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `don_hang` (
  `ma_don_hang` varchar(20) NOT NULL,
  `tong_tien` double NOT NULL,
  `phuong_thuc_thanh_toan` varchar(50) NOT NULL,
  `thoi_gian` datetime DEFAULT CURRENT_TIMESTAMP,
  `user_id` int NOT NULL,
  `status` varchar(20) DEFAULT 'active',
  PRIMARY KEY (`ma_don_hang`),
  KEY `don_hang_ibfk_1` (`user_id`),
  CONSTRAINT `don_hang_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `don_hang`
--

LOCK TABLES `don_hang` WRITE;
/*!40000 ALTER TABLE `don_hang` DISABLE KEYS */;
INSERT INTO `don_hang` VALUES ('DH1741431403734',30000,'Thanh toán khi nhận hàng','2025-03-08 17:56:43',3,'active'),('DH1741431441774',10000,'Thanh toán khi nhận hàng','2025-03-08 17:57:21',3,'active'),('DH1741431494560',25000,'Thanh toán khi nhận hàng','2025-03-08 17:58:14',3,'active'),('DH1741431548271',30000,'Thanh toán khi nhận hàng','2025-03-08 17:59:08',3,'active'),('DH1742811192428',10000,'Chuyển khoản ngân hàng','2025-03-24 17:13:12',3,'active'),('DH1742811802995',10000,'Ví điện tử','2025-03-24 17:23:23',3,'active');
/*!40000 ALTER TABLE `don_hang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gio_hang`
--

DROP TABLE IF EXISTS `gio_hang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gio_hang` (
  `ma_gio_hang` int NOT NULL AUTO_INCREMENT,
  `ma_san_pham` int NOT NULL,
  `so_luong` int NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`ma_gio_hang`),
  KEY `gio_hang_ibfk_1` (`ma_san_pham`),
  KEY `gio_hang_ibfk_2` (`user_id`),
  CONSTRAINT `gio_hang_ibfk_1` FOREIGN KEY (`ma_san_pham`) REFERENCES `san_pham` (`ma_san_pham`),
  CONSTRAINT `gio_hang_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gio_hang`
--

LOCK TABLES `gio_hang` WRITE;
/*!40000 ALTER TABLE `gio_hang` DISABLE KEYS */;
INSERT INTO `gio_hang` VALUES (24,1,1,3);
/*!40000 ALTER TABLE `gio_hang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `loai_san_pham`
--

DROP TABLE IF EXISTS `loai_san_pham`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `loai_san_pham` (
  `ma_loai_san_pham` varchar(10) NOT NULL,
  `ten_loai` varchar(100) NOT NULL,
  PRIMARY KEY (`ma_loai_san_pham`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `loai_san_pham`
--

LOCK TABLES `loai_san_pham` WRITE;
/*!40000 ALTER TABLE `loai_san_pham` DISABLE KEYS */;
INSERT INTO `loai_san_pham` VALUES ('L001','Snack'),('L002','Kẹo'),('L003','Bánh tráng'),('L004','Mứt'),('L005','Bánh ngọt'),('L006','Chocolate'),('L007','Khô'),('L008','Nước ngọt'),('L009','Kem'),('L010','Khác');
/*!40000 ALTER TABLE `loai_san_pham` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `san_pham`
--

DROP TABLE IF EXISTS `san_pham`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `san_pham` (
  `ma_san_pham` int NOT NULL AUTO_INCREMENT,
  `ma_loai_san_pham` varchar(10) NOT NULL,
  `ten` varchar(255) NOT NULL,
  `gia` double NOT NULL,
  `hinh_anh` varchar(255) DEFAULT NULL,
  `so_luong` int NOT NULL DEFAULT '0',
  `noi_bat` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`ma_san_pham`),
  KEY `ma_loai_san_pham` (`ma_loai_san_pham`),
  CONSTRAINT `san_pham_ibfk_1` FOREIGN KEY (`ma_loai_san_pham`) REFERENCES `loai_san_pham` (`ma_loai_san_pham`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `san_pham`
--

LOCK TABLES `san_pham` WRITE;
/*!40000 ALTER TABLE `san_pham` DISABLE KEYS */;
INSERT INTO `san_pham` VALUES (1,'L001','Snack vị tôm',15000,'snack_tom.png',50,1),(2,'L001','Snack khoai tây',20000,'snack_khoai_tay.png',50,0),(3,'L001','Snack vị tôm cay',18000,'Oishi_tom_cay.png',50,0),(4,'L001','Snack phồng tôm',15000,'Oishi_phong_tom.png',50,0),(5,'L002','Kẹo dẻo',10000,'keo_deo.png',47,1),(6,'L002','Kẹo cứng',12000,'keo_cung.png',50,0),(7,'L002','Kẹo Dynamite',35000,'dynamite_candy.png',50,0),(8,'L002','Kẹo xoài muối ớt',25000,'keo-huong-xoai-nhan-muoi-ot-alpenliebe-goi-87g-202011231000137354.png',49,0),(9,'L003','Bánh tráng trộn',25000,'banh_trang_tron.png',50,0),(10,'L003','Bánh tráng cuốn tôm',25000,'Banh_trang_cuon_tom.png',50,0),(11,'L003','Bánh tráng tóp mỡ',35000,'Banh_trang_top_mo.png',50,0),(12,'L003','Bánh tráng sa tế',20000,'Banh_trang_sa_te.png',50,0),(13,'L004','Mứt gừng',30000,'mut_gung.png',49,1),(14,'L004','Mứt dừa',35000,'mut-dua-soi.png',50,0),(15,'L004','Mứt bí',25000,'Mut_bi.jpg',50,0),(16,'L004','Mứt chuối',30000,'Mut_chuoi.png',49,0),(17,'L005','Bánh cupcake',35000,'Cupcake1.png',50,0),(18,'L005','Bánh táo',65000,'Banh_tao.png',50,0),(19,'L005','Bánh matcha',70000,'Banh_matcha.png',50,0),(20,'L005','Bánh trứng',15000,'Banh_trung.png',50,0),(21,'L006','Chocolate sữa',40000,'chocolate_sua.png',50,0),(22,'L006','Chocolate Milka',80000,'Chocolate_Milka.png',50,0),(23,'L006','Chocolate Dairy Milk',60000,'Chocolate_Dairy.jpg',50,0),(24,'L006','Chocolate Ferrero Rocher',120000,'Chocola_ferrero.png',50,0),(25,'L007','Khô bò',50000,'kho_bo.png',50,0),(26,'L007','Khô gà lá chanh',90000,'Kho_ga_la_chanh.png',50,0),(27,'L007','Khô mực',120000,'Kho_muc.png',50,0),(28,'L007','Khô trâu',150000,'Kho_trau.png',50,0),(29,'L008','Nước ngọt có ga',15000,'nuoc_ngot_co_ga.png',50,0),(30,'L008','Nước tăng lực Monster',56000,'Monster.png',50,0),(31,'L008','Nước tăng lực Mountain Dew',25000,'Mountaindew.png',50,0),(32,'L008','Nước Ice+ hương Đào',12000,'Ice+Dao.png',50,0),(33,'L009','Kem dâu',20000,'kem_dau.png',50,0),(34,'L009','Kem việt quất',20000,'Kem_viet_quat.png',50,0),(35,'L009','Kem matcha',20000,'Kem_matcha.png',50,0),(36,'L009','Kem socola',20000,'Kem_socola.png',50,0),(37,'L010','Khác - Bắp rang',18000,'bap_rang.png',50,0),(38,'L010','Khác - Chân gà xả tắc',60000,'Chan_ga_sa_tac.png',50,0),(39,'L010','Khác - Cá viên chiên',40000,'Ca_vien_chien.png',50,0),(40,'L010','Khác - Xúc xích nướng',20000,'Xuc_xich_nuong.png',50,1);
/*!40000 ALTER TABLE `san_pham` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'khachhang@gmail.com','kh123',NULL,NULL),(2,'user1@example.com','user123',NULL,NULL),(3,'user@gmail.com','123456','Tím','361 Lê Đại Hành P11 Q11 TPHCM');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-03-25 18:32:52
