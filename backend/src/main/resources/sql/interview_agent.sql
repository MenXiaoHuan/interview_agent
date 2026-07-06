/*
 Navicat Premium Dump SQL

 Source Server         : ai
 Source Server Type    : MySQL
 Source Server Version : 90700 (9.7.0)
 Source Host           : localhost:3306
 Source Schema         : interview_agent

 Target Server Type    : MySQL
 Target Server Version : 90700 (9.7.0)
 File Encoding         : 65001

 Date: 06/05/2026 21:02:20
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_question_history
-- ----------------------------
DROP TABLE IF EXISTS `ai_question_history`;
CREATE TABLE `ai_question_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assessment_session_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'AIview测评流程实例ID',
  `user_id` bigint NOT NULL,
  `job_id` bigint NOT NULL,
  `start_time` timestamp NULL DEFAULT NULL,
  `end_time` timestamp NULL DEFAULT NULL,
  `total_time` int unsigned DEFAULT NULL COMMENT '总用时(秒)',
  `overall_score` tinyint unsigned DEFAULT NULL COMMENT '总体评分(0-100)',
  `ai_suggestions` json DEFAULT NULL COMMENT 'AI改进建议',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `job_id` (`job_id`) USING BTREE,
  KEY `idx_assessment_session_id` (`assessment_session_id`) USING BTREE,
  KEY `idx_user_job` (`user_id`,`job_id`) USING BTREE,
  CONSTRAINT `ai_question_history_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `ai_question_history_ibfk_2` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for ai_resume_history
-- ----------------------------
DROP TABLE IF EXISTS `ai_resume_history`;
CREATE TABLE `ai_resume_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assessment_session_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'AIview测评流程实例ID',
  `user_id` bigint NOT NULL,
  `job_id` bigint NOT NULL,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `file_size` int unsigned NOT NULL,
  `file_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `upload_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `overall_score` tinyint unsigned DEFAULT NULL COMMENT '总体评分(0-100)',
  `ai_analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT 'AI分析结果JSON',
  `ai_suggestions` json DEFAULT NULL COMMENT 'AI改进建议',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `job_id` (`job_id`) USING BTREE,
  KEY `idx_assessment_session_id` (`assessment_session_id`) USING BTREE,
  KEY `idx_user_job` (`user_id`,`job_id`) USING BTREE,
  CONSTRAINT `ai_resume_history_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `ai_resume_history_ibfk_2` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=63 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for ai_interview_round_history
-- ----------------------------
DROP TABLE IF EXISTS `ai_interview_round_history`;
CREATE TABLE `ai_interview_round_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assessment_session_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'AIview测评流程实例ID',
  `round_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '面试轮次(round_1/round_2/round_3)',
  `user_id` bigint NOT NULL,
  `job_id` bigint NOT NULL,
  `start_time` timestamp NULL DEFAULT NULL,
  `end_time` timestamp NULL DEFAULT NULL,
  `total_time` int unsigned DEFAULT NULL COMMENT '总用时(秒)',
  `overall_score` tinyint unsigned DEFAULT NULL COMMENT '总体评分(0-100)',
  `ai_assessment` json DEFAULT NULL COMMENT 'AI评估详情',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `job_id` (`job_id`) USING BTREE,
  KEY `idx_assessment_session_id` (`assessment_session_id`) USING BTREE,
  KEY `idx_user_job` (`user_id`,`job_id`) USING BTREE,
  CONSTRAINT `ai_interview_round_history_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `ai_interview_round_history_ibfk_2` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for ai_assessment_session
-- ----------------------------
DROP TABLE IF EXISTS `ai_assessment_session`;
CREATE TABLE `ai_assessment_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assessment_session_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'AIview测评流程实例ID',
  `user_id` bigint NOT NULL,
  `job_id` bigint NOT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'IN_PROGRESS/PASSED/FAILED',
  `current_stage` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'resume/questions/round_1/round_2/round_3/completed',
  `elimination_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `final_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `final_suggestion` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `start_time` timestamp NULL DEFAULT NULL,
  `end_time` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_assessment_session_id` (`assessment_session_id`) USING BTREE,
  KEY `idx_assessment_user_job` (`user_id`,`job_id`) USING BTREE,
  CONSTRAINT `ai_assessment_session_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `ai_assessment_session_ibfk_2` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for agent_conversation_session
-- ----------------------------
DROP TABLE IF EXISTS `agent_conversation_session`;
CREATE TABLE `agent_conversation_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '所属用户 ID',
  `agent_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Agent 标识',
  `chat_id` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '后端稳定会话 ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '会话标题',
  `preview` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '会话预览',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_chat` (`user_id`, `chat_id`) USING BTREE,
  UNIQUE KEY `uk_chat_id` (`chat_id`) USING BTREE,
  KEY `idx_user_agent` (`user_id`, `agent_key`) USING BTREE,
  KEY `idx_updated_at` (`updated_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='Agent 会话主表';

-- ----------------------------
-- Table structure for agent_conversation_message
-- ----------------------------
DROP TABLE IF EXISTS `agent_conversation_message`;
CREATE TABLE `agent_conversation_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `chat_id` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话 ID',
  `turn_no` int DEFAULT NULL COMMENT '对话轮次',
  `role` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'user/assistant/system',
  `content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_chat_turn` (`chat_id`, `turn_no`) USING BTREE,
  KEY `idx_chat_created` (`chat_id`, `created_at`) USING BTREE,
  CONSTRAINT `fk_agent_conversation_message_chat_id` FOREIGN KEY (`chat_id`) REFERENCES `agent_conversation_session` (`chat_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='Agent 会话消息表';

-- ----------------------------
-- Table structure for agent_conversation_event
-- ----------------------------
DROP TABLE IF EXISTS `agent_conversation_event`;
CREATE TABLE `agent_conversation_event` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `chat_id` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话 ID',
  `turn_no` int DEFAULT NULL COMMENT '关联对话轮次',
  `event_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '事件类型',
  `payload_json` json DEFAULT NULL COMMENT '事件结构化负载',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_chat_event` (`chat_id`, `event_type`) USING BTREE,
  KEY `idx_chat_created` (`chat_id`, `created_at`) USING BTREE,
  CONSTRAINT `fk_agent_conversation_event_chat_id` FOREIGN KEY (`chat_id`) REFERENCES `agent_conversation_session` (`chat_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='Agent 会话事件表';

-- ----------------------------
-- Table structure for agent_conversation_memory
-- ----------------------------
DROP TABLE IF EXISTS `agent_conversation_memory`;
CREATE TABLE `agent_conversation_memory` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `chat_id` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话 ID',
  `summary_hash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '压缩摘要来源哈希',
  `summary_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '历史摘要内容',
  `recent_turns_json` json DEFAULT NULL COMMENT '最近 3 轮对话原文',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_chat_id` (`chat_id`) USING BTREE,
  KEY `idx_updated_at` (`updated_at`) USING BTREE,
  CONSTRAINT `fk_agent_conversation_memory_chat_id` FOREIGN KEY (`chat_id`) REFERENCES `agent_conversation_session` (`chat_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='Agent 会话记忆表';

-- ----------------------------
-- Table structure for feedback
-- ----------------------------
DROP TABLE IF EXISTS `feedback`;
CREATE TABLE `feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '反馈类型：BUG/建议/其他',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '反馈标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '反馈内容',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/处理中/已完成',
  `reply` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '管理员回复内容',
  `reply_time` timestamp NULL DEFAULT NULL COMMENT '回复时间',
  `admin_id` bigint DEFAULT NULL COMMENT '处理管理员ID',
  `admin_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '处理管理员名称',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE,
  CONSTRAINT `feedback_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户反馈表';

-- ----------------------------
-- Table structure for blessing
-- ----------------------------
DROP TABLE IF EXISTS `blessing`;
CREATE TABLE `blessing` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '创建人用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '祝福语内容',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '祝福语类型',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/INACTIVE',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_blessing_status` (`status`) USING BTREE,
  KEY `idx_blessing_type` (`type`) USING BTREE,
  KEY `idx_blessing_created_at` (`created_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='祝福语表';

-- ----------------------------
-- Table structure for job
-- ----------------------------
DROP TABLE IF EXISTS `job`;
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

-- ----------------------------
-- Table structure for job_category
-- ----------------------------
DROP TABLE IF EXISTS `job_category`;
CREATE TABLE `job_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `level` tinyint NOT NULL COMMENT '层级：1-一级分类，2-二级分类',
  `parent_id` bigint DEFAULT NULL COMMENT '父分类ID',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_parent_id` (`parent_id`) USING BTREE,
  KEY `idx_level` (`level`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=411 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='岗位分类表';

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密后的密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '用户昵称',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '头像URL',
  `user_type` tinyint DEFAULT '1' COMMENT '用户类型：1-普通用户，2-会员用户，3-管理员',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '手机号',
  `gender` tinyint DEFAULT '1' COMMENT '性别：0-未知，1-男，2-女',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-正常，0-禁用',
  `last_login_at` timestamp NULL DEFAULT NULL COMMENT '最后登录时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `eye_care_mode` tinyint DEFAULT '0' COMMENT '护眼模式：0-关闭，1-开启',
  `surprise_mode` tinyint DEFAULT '0' COMMENT '惊喜模式：0-关闭，1-开启',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `username` (`username`) USING BTREE,
  KEY `idx_username` (`username`) USING BTREE,
  KEY `idx_user_type` (`user_type`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户表';


-- ----------------------------
-- Seed data for job categories and mainstream JDs
-- ----------------------------

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

LOCK TABLES `job_category` WRITE;
/*!40000 ALTER TABLE `job_category` DISABLE KEYS */;
INSERT INTO `job_category` (`id`, `name`, `level`, `parent_id`, `sort_order`, `status`, `created_at`, `updated_at`) VALUES (1,'人工智能',1,NULL,10,1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(2,'大数据',1,NULL,20,1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(3,'物联网',1,NULL,30,1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(4,'智能系统',1,NULL,40,1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(101,'机器学习',2,1,1,1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(102,'计算机视觉',2,1,2,1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(103,'自然语言处理',2,1,3,1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(104,'智能算法',2,1,4,1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(201,'数据开发',2,2,1,1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(202,'数据分析',2,2,2,1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(203,'数据架构',2,2,3,1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(204,'数据治理',2,2,4,1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(301,'嵌入式开发',2,3,1,1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(302,'物联网架构',2,3,2,1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(303,'传感器开发',2,3,3,1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(304,'物联网安全',2,3,4,1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(401,'系统架构',2,4,1,1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(402,'智能运维',2,4,2,1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(403,'自动化测试',2,4,3,1,'2025-05-20 11:48:17','2025-05-20 11:48:17'),(404,'智能产品',2,4,4,1,'2025-05-20 11:48:17','2025-05-20 11:48:17');
/*!40000 ALTER TABLE `job_category` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `job` WRITE;
/*!40000 ALTER TABLE `job` DISABLE KEYS */;
INSERT INTO `job` (`id`, `category_id`, `name`, `description`, `requirements`, `status`, `created_at`, `updated_at`) VALUES (1,101,'机器学习工程师','面向推荐、搜索、广告、风控、增长等核心业务场景，负责机器学习模型的建模、训练、评估、上线和持续优化，提升用户体验、业务效率和系统智能化水平。','岗位职责\n1. 负责机器学习模型在推荐、搜索、广告、风控或用户增长等业务场景中的设计、训练、评估和线上迭代。\n2. 深入理解业务目标，完成特征工程、样本构建、模型选择、离线评估、A/B 实验和效果归因。\n3. 优化模型效果、推理性能和稳定性，推动模型工程化部署和在线服务治理。\n4. 跟进业界机器学习、深度学习和大模型相关技术进展，结合业务问题完成技术落地。\n5. 与产品、工程、数据团队协作，建立可复用的算法能力和评估体系。\n\n任职要求\n1. 计算机、人工智能、数学、统计学等相关专业本科及以上学历，硕士优先。\n2. 熟悉常见机器学习算法和深度学习模型，理解特征工程、损失函数、优化方法和模型评估指标。\n3. 熟练使用 Python，熟悉 PyTorch、TensorFlow、Scikit-learn 等至少一种主流框架。\n4. 具备扎实的数据结构、算法、概率统计和工程编码能力。\n5. 有完整模型上线经验，理解训练、评估、部署、监控和迭代闭环。\n\n加分项\n1. 有推荐系统、搜索排序、广告投放、风控反作弊、用户画像等大规模业务经验。\n2. 有多模态、大模型微调、RAG、模型压缩或高性能推理经验。\n3. 在顶会、竞赛或开源项目中有高质量成果。',1,'2025-05-20 11:48:17','2026-07-06 02:21:45'),(2,102,'计算机视觉算法专家','负责计算机视觉核心算法研发与落地，覆盖图像理解、目标检测、分割、OCR、多模态理解和视觉生成等方向，支撑业务智能化产品能力建设。','岗位职责\n1. 负责目标检测、图像分类、实例分割、OCR、视频理解、视觉检索或多模态理解等算法研发。\n2. 针对业务场景进行数据清洗、标注规范设计、模型训练、效果评估和线上迭代。\n3. 优化视觉模型在准确率、召回率、推理延迟、显存占用和稳定性上的综合表现。\n4. 推动视觉算法服务化、平台化和工程化落地，沉淀可复用模型与工具链。\n5. 跟踪 CV、多模态和视觉大模型方向前沿技术，并完成业务验证和应用。\n\n任职要求\n1. 计算机、人工智能、自动化、电子信息等相关专业硕士及以上学历，博士优先。\n2. 熟悉 CNN、Transformer、检测、分割、OCR、跟踪、检索等视觉算法原理。\n3. 熟练掌握 Python 和 PyTorch/TensorFlow，具备良好的代码实现与调试能力。\n4. 有真实业务数据建模、模型评估、线上部署和效果迭代经验。\n5. 具备较强的问题抽象、实验设计和跨团队沟通能力。\n\n加分项\n1. 有大规模视觉识别、内容安全、工业质检、自动驾驶、AIGC 或多模态项目经验。\n2. 熟悉 TensorRT、ONNX、模型量化、蒸馏、剪枝等推理优化技术。\n3. 有 CVPR、ICCV、ECCV、NeurIPS 等论文或高影响力开源项目经验。',1,'2025-05-20 11:48:17','2026-07-06 02:21:45'),(3,103,'NLP算法工程师','负责自然语言处理和大语言模型相关算法研发，建设文本理解、知识问答、智能对话、信息抽取和内容生成等核心能力。','岗位职责\n1. 负责文本分类、信息抽取、语义匹配、搜索问答、对话系统、文本生成等 NLP 能力建设。\n2. 基于预训练模型和大语言模型完成微调、指令优化、评测集构建和线上效果迭代。\n3. 建设 Prompt、RAG、Agent、知识库、模型评估和安全治理等能力。\n4. 结合业务数据优化模型准确率、稳定性、可解释性和生成质量。\n5. 与产品、后端、数据团队协同推动 NLP 能力在业务系统中规模化落地。\n\n任职要求\n1. 计算机、人工智能、自然语言处理等相关专业本科及以上学历，硕士优先。\n2. 熟悉 Transformer、BERT、GPT、Embedding、Rerank、Seq2Seq 等模型原理。\n3. 熟练使用 Python 和 PyTorch/TensorFlow，具备模型训练、调参和工程部署经验。\n4. 熟悉主流大模型使用方式，理解微调、RAG、Prompt Engineering 和评估方法。\n5. 具备扎实的数据结构、算法基础和论文阅读复现能力。\n\n加分项\n1. 有搜索推荐、智能客服、企业知识库、代码智能、内容安全或多轮对话项目经验。\n2. 有大模型 SFT、DPO、LoRA、向量数据库、Agent 框架落地经验。\n3. 有 ACL、EMNLP、NAACL、NeurIPS 等论文或高质量开源贡献。',1,'2025-05-20 11:48:17','2026-07-06 02:21:45'),(4,201,'大数据开发工程师','负责企业级大数据平台和数据链路建设，支撑离线数仓、实时计算、数据服务、指标体系和数据应用的稳定交付。','岗位职责\n1. 负责离线和实时数据链路开发，包括数据采集、清洗、建模、计算、调度和质量监控。\n2. 参与数据仓库、湖仓一体、数据中台和数据服务体系建设。\n3. 优化 Spark、Flink、Hive、Kafka 等任务的性能、稳定性和资源成本。\n4. 建设可复用的数据模型、指标口径和数据开发规范，提升数据交付效率。\n5. 支撑业务分析、推荐算法、风控、增长等场景的数据需求。\n\n任职要求\n1. 计算机、软件工程、数据科学等相关专业本科及以上学历。\n2. 熟练掌握 Java/Scala/Python 至少一种语言，具备良好的工程编码能力。\n3. 熟悉 Hadoop、Spark、Flink、Hive、Kafka、HBase、ClickHouse 等常见大数据组件。\n4. 理解数仓分层、维度建模、实时计算、任务调度和数据质量治理。\n5. 具备较强的问题排查能力，能定位数据延迟、倾斜、资源瓶颈和链路异常。\n\n加分项\n1. 有 PB 级数据处理、实时数仓、湖仓一体或数据平台建设经验。\n2. 熟悉 Doris、Iceberg、Hudi、Delta Lake、Airflow、DolphinScheduler 等技术。\n3. 有数据资产治理、指标平台或数据服务 API 建设经验。',1,'2025-05-20 11:48:17','2026-07-06 02:21:45'),(5,202,'数据分析师','负责业务经营分析、用户增长分析、产品策略分析和数据洞察输出，通过数据驱动业务决策、产品优化和增长策略落地。','岗位职责\n1. 负责业务核心指标体系建设，完成日常经营分析、专题分析和策略复盘。\n2. 深入理解业务流程，通过数据发现问题、定位原因、提出可落地的优化建议。\n3. 支持产品、运营、增长、商业化等团队进行实验设计、A/B 测试和效果评估。\n4. 建设数据看板、分析报表和自动化监控，提升业务团队数据使用效率。\n5. 推动指标口径统一和分析方法沉淀，形成可复用的数据分析框架。\n\n任职要求\n1. 统计学、数学、计算机、经济学、管理科学等相关专业本科及以上学历。\n2. 熟练掌握 SQL，能独立完成复杂数据提取、清洗和分析。\n3. 熟悉 Python/R、Excel、Tableau、PowerBI、FineBI 等分析或可视化工具。\n4. 具备扎实的统计分析、逻辑推理、实验设计和业务拆解能力。\n5. 能用清晰的业务语言表达分析结论，并推动决策落地。\n\n加分项\n1. 有互联网产品、用户增长、推荐策略、商业化、供应链或风控分析经验。\n2. 熟悉因果推断、漏斗分析、留存分析、归因分析和用户分层方法。\n3. 有数据产品化、指标平台或自助分析体系建设经验。',1,'2025-05-20 11:48:17','2026-07-06 02:21:45'),(6,203,'数据架构师','负责企业级数据架构规划、数据平台技术选型和核心数据模型设计，支撑高并发、大规模、多业务线的数据生产与消费。','岗位职责\n1. 负责企业数据架构、数据模型、数据标准和数据平台演进规划。\n2. 设计离线数仓、实时数仓、湖仓一体、数据服务和数据治理整体方案。\n3. 牵头解决跨业务域数据一致性、指标统一、链路稳定性和性能成本问题。\n4. 制定数据开发规范、建模规范、质量规范和权限安全规范。\n5. 指导团队完成关键项目技术方案评审、架构设计和落地验收。\n\n任职要求\n1. 计算机、软件工程、数据科学等相关专业本科及以上学历，8 年以上数据相关经验。\n2. 深入理解分布式计算、数仓建模、实时计算、数据湖、数据治理和数据服务。\n3. 熟悉 Hadoop、Spark、Flink、Kafka、Hive、Doris、ClickHouse、Iceberg 等技术生态。\n4. 具备复杂系统架构设计能力，能平衡性能、成本、稳定性、安全和可扩展性。\n5. 具备技术影响力和跨团队推进能力，能沉淀平台化方案。\n\n加分项\n1. 主导过大型互联网或集团级数据中台、湖仓一体、指标平台建设。\n2. 熟悉云原生数据平台、元数据管理、血缘分析、数据权限和数据资产治理。\n3. 有高并发数据服务、实时决策或 AI 数据底座建设经验。',1,'2025-05-20 11:48:17','2026-07-06 02:21:45'),(7,301,'嵌入式软件工程师','负责智能硬件、物联网设备或边缘计算设备的嵌入式软件开发，覆盖驱动、系统、中间件、通信协议和稳定性优化。','岗位职责\n1. 负责嵌入式 Linux/RTOS 平台上的驱动、系统服务、中间件和应用开发。\n2. 完成设备通信、数据采集、低功耗、OTA、日志诊断和异常恢复等能力建设。\n3. 配合硬件、结构、测试和云端团队完成整机联调、量产验证和问题定位。\n4. 优化设备启动速度、资源占用、功耗、稳定性和长期运行可靠性。\n5. 编写设计文档、调试工具和自动化测试脚本，提升研发交付质量。\n\n任职要求\n1. 计算机、电子信息、自动化、通信等相关专业本科及以上学历。\n2. 熟练掌握 C/C++，理解数据结构、操作系统、网络通信和多线程编程。\n3. 熟悉 Linux 驱动、设备树、Bootloader、文件系统或 RTOS 开发。\n4. 熟悉 UART、I2C、SPI、CAN、BLE、Wi-Fi、MQTT 等常见接口和协议。\n5. 具备示波器、逻辑分析仪、GDB、串口日志等调试经验。\n\n加分项\n1. 有智能家居、机器人、车载、工业网关、可穿戴或边缘 AI 设备经验。\n2. 熟悉 Yocto、OpenWrt、FreeRTOS、Zephyr、Android HAL 等平台。\n3. 有量产项目经验，理解工厂测试、可靠性测试和 OTA 灰度发布。',1,'2025-05-20 11:48:17','2026-07-06 02:21:45'),(8,302,'物联网架构师','负责物联网平台整体架构设计，打通设备接入、消息通信、规则引擎、设备管理、边缘计算和云端服务，支撑海量设备稳定运行。','岗位职责\n1. 负责物联网云平台、边缘平台和设备接入架构设计。\n2. 设计设备身份认证、消息通道、影子模型、规则引擎、OTA、监控告警等核心能力。\n3. 解决海量设备连接、高并发消息、弱网重连、数据一致性和平台扩展性问题。\n4. 制定设备接入规范、协议规范、安全规范和平台技术演进路线。\n5. 指导团队完成关键系统设计评审、性能优化和稳定性治理。\n\n任职要求\n1. 计算机、通信、电子信息等相关专业本科及以上学历，5 年以上相关经验。\n2. 熟悉 MQTT、CoAP、HTTP、WebSocket、TCP/IP、BLE、Modbus 等协议。\n3. 熟悉 Java/Go/C++ 至少一种后端语言，理解微服务、消息队列、缓存和数据库设计。\n4. 有高并发、高可用、分布式系统设计经验，能处理大规模设备接入问题。\n5. 理解物联网安全、设备生命周期管理和边缘计算基本架构。\n\n加分项\n1. 主导过百万级设备接入平台、工业互联网平台或智能硬件云平台。\n2. 熟悉 Kubernetes、Service Mesh、时序数据库、流式计算和云厂商 IoT 服务。\n3. 有边云协同、数字孪生或设备数据智能分析经验。',1,'2025-05-20 11:48:17','2026-07-06 02:21:45'),(9,304,'物联网安全专家','负责物联网设备、通信链路、云平台和数据安全体系建设，覆盖安全架构、漏洞治理、攻防评估、合规和安全运营。','岗位职责\n1. 负责物联网设备、固件、通信协议、边缘节点和云平台的安全评估与架构设计。\n2. 建设设备身份认证、密钥管理、安全启动、固件签名、OTA 安全和数据加密方案。\n3. 开展漏洞挖掘、渗透测试、固件逆向、协议分析和安全风险闭环治理。\n4. 制定物联网安全规范、研发安全流程和安全基线，推动产品安全合规。\n5. 支撑安全事件响应、风险监控和攻防演练，提升整体安全水位。\n\n任职要求\n1. 信息安全、计算机、通信、电子等相关专业本科及以上学历。\n2. 熟悉网络安全、系统安全、密码学基础和常见攻防技术。\n3. 熟悉 MQTT、CoAP、BLE、Wi-Fi、TCP/IP 等物联网协议安全风险。\n4. 具备固件分析、二进制漏洞、Web/API 安全或云平台安全经验。\n5. 能独立输出安全评估报告、修复方案和安全架构建议。\n\n加分项\n1. 有智能硬件、车联网、工业互联网或边缘计算安全经验。\n2. 熟悉 TEE、安全芯片、证书体系、零信任或设备可信执行环境。\n3. 有 CVE、SRC 高危漏洞、攻防竞赛或安全标准落地经验。',1,'2025-05-20 11:48:17','2026-07-06 02:21:45'),(10,401,'系统架构师','负责大型智能系统、业务平台或技术中台的整体架构设计，支撑高并发、高可用、可扩展和可演进的业务系统建设。','岗位职责\n1. 负责核心系统架构设计、技术选型、容量规划、稳定性治理和性能优化。\n2. 牵头复杂业务系统的领域建模、服务拆分、接口设计、数据架构和演进路线。\n3. 解决高并发、分布式事务、缓存一致性、服务治理、容灾和成本优化问题。\n4. 推动工程规范、架构评审、技术债治理和平台化能力建设。\n5. 指导团队完成关键项目落地，提升研发效率和系统长期可维护性。\n\n任职要求\n1. 计算机相关专业本科及以上学历，8 年以上后端或架构相关经验。\n2. 精通 Java/Go/C++ 至少一种语言，理解 JVM、并发编程、网络和数据库原理。\n3. 熟悉微服务、消息队列、缓存、分布式存储、搜索、网关和配置中心等组件。\n4. 具备大型系统架构设计经验，能平衡业务复杂度、稳定性、性能和成本。\n5. 具备技术领导力、跨团队沟通能力和复杂问题拆解能力。\n\n加分项\n1. 主导过千万级用户、高并发交易、推荐搜索、AI 平台或企业级中台架构。\n2. 熟悉云原生、Kubernetes、Service Mesh、可观测性和 DevOps 体系。\n3. 有从单体到微服务、从业务系统到平台化的架构演进经验。',1,'2025-05-20 11:48:17','2026-07-06 02:21:45'),(11,402,'智能运维工程师','负责大规模基础设施、业务系统和 AI 平台的智能运维能力建设，提升系统稳定性、交付效率、故障自愈和成本治理水平。','岗位职责\n1. 负责监控告警、日志链路、容量管理、自动化发布、故障自愈和运维平台建设。\n2. 建设 SLO/SLA、可观测性、故障演练、根因分析和应急响应机制。\n3. 使用自动化、脚本化和平台化手段提升系统运维效率，降低人工操作风险。\n4. 参与高可用架构、容灾方案、资源成本优化和稳定性专项治理。\n5. 探索 AIOps 能力，包括异常检测、告警收敛、智能诊断和预测性维护。\n\n任职要求\n1. 计算机相关专业本科及以上学历，熟悉 Linux、网络、存储和操作系统原理。\n2. 熟练使用 Shell/Python/Go 至少一种语言进行自动化开发。\n3. 熟悉 Kubernetes、Docker、Prometheus、Grafana、ELK/Loki、CI/CD 等工具链。\n4. 理解微服务、负载均衡、缓存、数据库和消息队列的运维特点。\n5. 具备较强故障定位、应急处理、复盘分析和稳定性治理能力。\n\n加分项\n1. 有大规模 Kubernetes 集群、在线业务稳定性或云平台运维经验。\n2. 熟悉 Terraform、Ansible、ArgoCD、GitOps 或 FinOps 成本治理。\n3. 有 AIOps、混沌工程、容量预测或自动化故障恢复项目经验。',1,'2025-05-20 11:48:17','2026-07-06 02:21:45'),(12,403,'自动化测试工程师','负责质量保障体系和自动化测试平台建设，覆盖接口、UI、性能、稳定性、CI/CD 和质量数据分析，提升研发交付质量。','岗位职责\n1. 负责 Web、移动端、后端服务或 AI 系统的测试方案设计、测试执行和质量风险评估。\n2. 建设接口自动化、UI 自动化、性能测试、稳定性测试和持续集成质量门禁。\n3. 参与需求评审、技术评审和上线评审，推动缺陷预防和质量左移。\n4. 通过测试平台、脚本工具和数据分析提升测试效率与问题发现能力。\n5. 跟踪线上质量指标，参与故障复盘并推动流程和技术改进。\n\n任职要求\n1. 计算机相关专业本科及以上学历，具备扎实的软件测试理论和工程能力。\n2. 熟练掌握 Python/Java/JavaScript 至少一种语言，能独立开发测试工具或自动化框架。\n3. 熟悉 HTTP、数据库、Linux、日志分析、Mock、CI/CD 和常见测试框架。\n4. 有接口测试、自动化测试、性能测试或稳定性测试实战经验。\n5. 具备良好的问题定位、沟通推动和质量风险判断能力。\n\n加分项\n1. 熟悉 Pytest、JUnit、Playwright、Cypress、Selenium、JMeter、Locust 等工具。\n2. 有微服务、云原生、AI 应用、大数据平台或复杂业务系统测试经验。\n3. 有质量平台、精准测试、流量回放、测试数据管理或效能度量建设经验。',1,'2025-05-20 11:48:17','2026-07-06 02:21:45'),(13,404,'智能产品经理','负责 AI/智能化产品的规划、设计和商业化落地，围绕用户场景、模型能力、数据闭环和产品体验打造可规模化的智能产品。','岗位职责\n1. 负责智能产品的市场洞察、用户研究、需求分析、产品规划和路线图设计。\n2. 将 AI、算法或大模型能力转化为可用、可衡量、可迭代的产品方案。\n3. 设计核心业务流程、交互体验、指标体系、实验方案和增长策略。\n4. 协同算法、研发、设计、运营和商业团队推动产品从 0 到 1 或持续迭代。\n5. 跟踪产品数据和用户反馈，持续优化模型效果、用户体验和业务价值。\n\n任职要求\n1. 本科及以上学历，计算机、人工智能、数据科学、心理学、管理等背景优先。\n2. 具备优秀的用户洞察、需求抽象、产品设计和逻辑表达能力。\n3. 理解机器学习、大模型、推荐、搜索、智能对话或数据产品的基本原理和边界。\n4. 熟悉 PRD、原型设计、数据分析、A/B 实验和项目管理方法。\n5. 具备强跨团队协作能力，能在不确定环境中推动复杂项目落地。\n\n加分项\n1. 有 AI Agent、智能助手、企业知识库、推荐系统、智能硬件或数据产品经验。\n2. 有大厂产品、B 端 SaaS、平台型产品或商业化产品成功案例。\n3. 熟悉 Prompt、RAG、模型评测、AI 安全和人机交互体验设计。',1,'2025-05-20 11:48:17','2026-07-06 02:21:45'),(14,303,'传感器开发工程师','负责传感器选型、硬件调试、驱动适配、信号处理和可靠性验证，支撑智能硬件、工业设备或物联网终端的感知能力建设。','岗位职责\n1. 负责温湿度、压力、惯性、光学、声学、气体、视觉等传感器的选型、评估和集成。\n2. 完成传感器驱动适配、数据采集、标定校准、滤波算法和异常检测开发。\n3. 配合硬件、嵌入式、结构和测试团队完成原理图评审、样机调试和量产验证。\n4. 建设传感器测试方案、可靠性验证方案和数据分析流程。\n5. 跟踪传感器器件、感知算法和边缘智能技术趋势，推动产品性能优化。\n\n任职要求\n1. 电子信息、自动化、测控、仪器仪表、通信等相关专业本科及以上学历。\n2. 熟悉常见传感器工作原理、信号链路、采样、滤波、校准和误差分析。\n3. 熟悉 I2C、SPI、UART、ADC、CAN 等接口，具备基本硬件调试能力。\n4. 能使用示波器、逻辑分析仪、万用表等工具定位硬件和信号问题。\n5. 具备 C/C++ 或 Python 基础，能完成数据处理、调试脚本或驱动适配。\n\n加分项\n1. 有智能硬件、机器人、车载、工业检测、可穿戴或环境监测产品经验。\n2. 熟悉 IMU 融合、传感器标定、低功耗设计、EMC/可靠性测试。\n3. 有量产导入、供应商协同或传感器选型评估经验。',1,'2025-05-25 01:44:40','2026-07-06 02:21:45'),(15,204,'数据治理专家','负责企业数据治理体系建设，围绕数据标准、数据质量、元数据、主数据、数据安全和资产运营提升数据可信度与使用效率。','岗位职责\n1. 负责数据标准、指标口径、元数据、主数据、数据质量和数据资产管理体系建设。\n2. 推动跨业务线数据治理专项，解决口径不一致、质量不稳定、资产不可见等问题。\n3. 建设数据质量规则、监控告警、血缘分析、资产目录和数据权限管理流程。\n4. 协同数据开发、业务、产品和安全团队推动治理规范落地。\n5. 定期输出数据治理成效、问题复盘和数据资产运营分析。\n\n任职要求\n1. 计算机、数据科学、统计学、信息管理等相关专业本科及以上学历。\n2. 熟悉数据治理方法论，理解 DAMA、数据标准、元数据、主数据和数据质量管理。\n3. 熟悉 SQL、数据仓库、数据建模、ETL/ELT 和常见数据平台工具。\n4. 具备较强的跨团队沟通、流程设计、问题推进和文档沉淀能力。\n5. 能结合业务场景制定可落地的治理规则和评估指标。\n\n加分项\n1. 有大型企业数据治理、数据中台、指标平台或数据资产平台建设经验。\n2. 熟悉数据安全、隐私合规、权限分级分类和敏感数据识别。\n3. 熟悉 Atlas、DataHub、Amundsen、DolphinScheduler、DQC 等工具或同类平台。',1,'2025-05-25 01:46:26','2026-07-06 02:21:45'),(16,104,'智能算法工程师','负责智能算法在业务场景中的研究、开发和工程化落地，覆盖优化算法、预测模型、决策智能、推荐排序和智能系统能力建设。','岗位职责\n1. 负责面向业务问题的算法建模、特征构建、模型训练、评估和线上迭代。\n2. 结合业务目标设计预测、推荐、排序、优化、调度或决策算法方案。\n3. 优化算法效果、稳定性、可解释性和工程性能，推动算法服务化落地。\n4. 建设实验评估、A/B 测试、模型监控和效果归因机制。\n5. 跟踪机器学习、强化学习、运筹优化、大模型和智能决策方向前沿技术。\n\n任职要求\n1. 数学、计算机、人工智能、自动化、统计学等相关专业本科及以上学历，硕士优先。\n2. 熟悉机器学习、深度学习、优化算法、概率统计和常见模型评估方法。\n3. 熟练使用 Python，熟悉 PyTorch/TensorFlow/Scikit-learn 等算法开发工具。\n4. 具备良好的算法实现、数据分析、实验设计和工程落地能力。\n5. 能从业务问题中抽象建模目标，并通过数据和实验验证算法价值。\n\n加分项\n1. 有推荐排序、智能调度、预测决策、风控、供应链优化或强化学习项目经验。\n2. 熟悉大模型、Agent、多模态、图学习、因果推断或运筹优化。\n3. 有高质量论文、竞赛获奖、开源项目或复杂业务算法落地案例。',1,'2025-05-25 01:46:26','2026-07-06 02:21:45');
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

-- End seed data for job categories and mainstream JDs
SET FOREIGN_KEY_CHECKS = 1;
