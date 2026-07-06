-- MySQL dump 10.13  Distrib 8.0.46, for Linux (aarch64)
--
-- Host: localhost    Database: interview_agent
-- ------------------------------------------------------
-- Server version	8.0.46

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
-- Table structure for table `job`
--

DROP TABLE IF EXISTS `job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category_id` bigint NOT NULL COMMENT '所属二级分类ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '岗位名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '岗位描述',
  `requirements` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '岗位要求',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_category_id` (`category_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  CONSTRAINT `job_ibfk` FOREIGN KEY (`category_id`) REFERENCES `job_category` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='岗位表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `job`
--

LOCK TABLES `job` WRITE;
/*!40000 ALTER TABLE `job` DISABLE KEYS */;
INSERT INTO `job` VALUES (1,101,'机器学习工程师','负责公司核心算法的设计、开发和优化，构建高效的机器学习模型，解决实际业务问题。','1. 计算机相关专业硕士及以上学历\r\n1. 精通机器学习算法原理和应用\r\n2. 熟练掌握Python、TensorFlow/PyTorch等深度学习框架\r\n3. 具有模型优化和部署经验\r\n4. 良好的算法实现能力和问题解决能力',1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(2,102,'计算机视觉算法专家','负责图像识别、目标检测等视觉算法研发，推进视觉技术在实际场景中的应用。','1. 计算机视觉或相关领域博士优先\r\n2. 精通目标检测、图像分割等经典算法\r\n3. 熟练使用主流深度学习框架\r\n4. 有大规模视觉算法落地经验\r\n5. 能独立负责重要项目的架构设计',1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(3,103,'NLP算法工程师','负责自然语言处理相关算法研发，解决文本理解、对话系统等技术难题。','1. 自然语言处理或相关领域硕士以上学历\r\n2. 精通NLP基础算法和预训练模型\r\n3. 熟悉Transformer、BERT等模型原理\r\n4. 有大规模语言模型开发经验\r\n5. 良好的论文阅读和算法实现能力',1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(4,201,'大数据开发工程师','负责公司大数据平台的设计、开发和维护，确保数据处理的高效性和可靠性。','1. 本科及以上学历，计算机相关专业\r\n2. 精通Hadoop、Spark等大数据框架\r\n3. 熟练使用HBase、Hive等数据仓库工具\r\n4. 具备良好的数据结构和算法基础\r\n5. 有大规模数据处理经验',1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(5,202,'数据分析师','负责业务数据分析，提供决策支持，发现数据价值，推动数据驱动的业务增长。','1. 统计学或相关专业本科以上学历\r\n2. 精通SQL、Python等数据分析工具\r\n3. 熟练使用数据可视化工具\r\n4. 具备强大的数据洞察能力\r\n5. 良好的业务理解能力和沟通能力',1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(6,203,'数据架构师','负责公司数据架构的规划和设计，确保数据平台的可扩展性和性能。','1. 8年以上相关工作经验\r\n2. 精通分布式系统设计\r\n3. 熟悉主流云平台架构\r\n4. 具备强大的技术规划能力\r\n5. 丰富的大规模数据处理经验',1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(7,301,'嵌入式软件工程师','负责物联网设备的嵌入式软件开发，确保设备的稳定性和性能。','1. 本科及以上学历，计算机或电子相关专业\r\n2. 精通C/C++编程语言\r\n3. 熟悉常见嵌入式操作系统\r\n4. 具备硬件调试能力\r\n5. 有物联网产品开发经验',1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(8,302,'物联网架构师','负责物联网平台架构设计，制定技术方案，解决系统难题。','1. 计算机相关专业本科以上学历\r\n2. 精通物联网协议和标准\r\n3. 熟悉云平台开发\r\n4. 具备系统架构设计能力\r\n5. 有大规模物联网项目经验',1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(9,304,'物联网安全专家','负责物联网安全架构设计和实施，保障设备和数据安全。','1. 信息安全相关专业背景\r\n2. 精通网络协议和安全标准\r\n3. 熟悉常见攻击手段和防御方法\r\n4. 具备安全架构设计能力\r\n5. 有物联网安全项目经验',1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(10,401,'系统架构师','负责智能系统整体架构设计，把控技术方向，解决系统难题。','1. 计算机相关专业本科以上学历\r\n2. 精通分布式系统设计\r\n3. 熟悉微服务架构\r\n4. 具备强大的架构设计能力\r\n5. 丰富的大型系统设计经验',1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(11,402,'智能运维工程师','负责智能运维平台开发，实现系统监控、告警和自动化运维。','1. 本科及以上学历\r\n2. 精通Linux系统管理\r\n3. 熟悉自动化运维工具\r\n4. 具备问题排查和解决能力\r\n5. 有大规模集群运维经验',1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(12,403,'自动化测试工程师','负责自动化测试框架开发，提高测试效率，保障系统质量。','1. 计算机相关专业本科以上学历\r\n2. 精通自动化测试框架\r\n3. 熟练掌握Python等脚本语言\r\n4. 具备性能测试经验\r\n5. 良好的问题分析能力',1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(13,404,'智能产品经理','负责智能产品的规划和设计，把控产品方向，提升用户体验。','1. 本科及以上学历\r\n2. 具备产品设计能力\r\n3. 熟悉人工智能技术\r\n4. 良好的沟通协调能力\r\n5. 有智能产品管理经验',1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(14,303,' 传感器开发工程师',' 负责传感器相关开发工作，保障传感器性能和功能实现。','1. 相关专业本科及以上学历\r\n2. 熟悉传感器原理和开发流程\r\n3. 具备一定的电路设计能力\r\n4. 掌握相关测试和调试方法\r\n5. 有传感器开发项目经验 ',1,'2025-05-25 01:44:40','2025-05-25 04:00:10'),(15,204,'数据治理专家','负责企业数据治理工作，优化数据质量，制定数据标准。','1. 计算机、统计学等相关专业本科及以上学历\r\n2. 熟悉数据治理流程和工具\r\n3. 具备数据分析和处理能力\r\n4. 掌握数据库管理知识\r\n5. 有数据治理项目经验',1,'2025-05-25 01:46:26','2025-05-25 04:00:21'),(16,104,'智能算法工程师','负责智能算法的研究与开发，提升算法性能和效率。','1. 数学、计算机科学等相关专业本科及以上学历\r\n2. 精通常见智能算法原理\r\n3. 熟练使用Python或其他算法开发语言\r\n4. 具备良好的数学基础和逻辑思维\r\n5. 有智能算法项目开发经验',1,'2025-05-25 01:46:26','2025-05-25 04:00:16');
/*!40000 ALTER TABLE `job` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-06  2:19:37
