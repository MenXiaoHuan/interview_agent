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
-- Table structure for choice_answer
-- ----------------------------
DROP TABLE IF EXISTS `choice_answer`;
CREATE TABLE `choice_answer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `record_id` bigint NOT NULL COMMENT '面试记录ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `user_answer` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户答案',
  `is_correct` tinyint(1) NOT NULL COMMENT '是否正确',
  `score` int NOT NULL COMMENT '得分',
  `answer_time` int DEFAULT NULL COMMENT '答题用时(秒)',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_record_id` (`record_id`) USING BTREE,
  KEY `idx_question_id` (`question_id`) USING BTREE,
  CONSTRAINT `choice_answer_ibfk_1` FOREIGN KEY (`record_id`) REFERENCES `choice_question_record` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `choice_answer_ibfk_2` FOREIGN KEY (`question_id`) REFERENCES `choice_question` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=1271 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='面试答案记录表';

-- ----------------------------
-- Table structure for choice_question
-- ----------------------------
DROP TABLE IF EXISTS `choice_question`;
CREATE TABLE `choice_question` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `job_id` bigint NOT NULL COMMENT '关联岗位ID',
  `question` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题目内容',
  `options` json NOT NULL COMMENT '选项，JSON数组格式',
  `correct_answer` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '正确答案',
  `explanation` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '答案解析',
  `difficulty` tinyint DEFAULT '2' COMMENT '难度：1-简单，2-中等，3-困难',
  `score` int DEFAULT '10' COMMENT '题目分值',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_job_id` (`job_id`) USING BTREE,
  KEY `idx_difficulty` (`difficulty`) USING BTREE,
  CONSTRAINT `choice_question_ibfk` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `choice_question_ibfk_1` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=527 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='面试题库表';

-- ----------------------------
-- Table structure for choice_question_record
-- ----------------------------
DROP TABLE IF EXISTS `choice_question_record`;
CREATE TABLE `choice_question_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `job_id` bigint NOT NULL COMMENT '岗位ID',
  `total_score` int NOT NULL COMMENT '总分',
  `correct_count` int NOT NULL COMMENT '正确题数',
  `correct_rate` decimal(5,2) NOT NULL COMMENT '正确率',
  `duration` int DEFAULT NULL COMMENT '答题时长(秒)',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-进行中，2-已完成，3-已取消',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `evaluation_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'SPECIAL' COMMENT '评估类型：SPECIAL-专项测评，COMPREHENSIVE-综合测评',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_job_id` (`job_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  CONSTRAINT `choice_question_record_ibfk_1` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=128 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='面试记录表';

-- ----------------------------
-- Table structure for comprehensive_question_history
-- ----------------------------
DROP TABLE IF EXISTS `comprehensive_question_history`;
CREATE TABLE `comprehensive_question_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `job_id` bigint NOT NULL,
  `start_time` timestamp NULL DEFAULT NULL,
  `end_time` timestamp NULL DEFAULT NULL,
  `total_time` int unsigned DEFAULT NULL COMMENT '总用时(秒)',
  `overall_score` tinyint unsigned DEFAULT NULL COMMENT '总体评分(0-100)',
  `ai_suggestions` json DEFAULT NULL COMMENT 'AI改进建议',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `job_id` (`job_id`) USING BTREE,
  KEY `idx_user_job` (`user_id`,`job_id`) USING BTREE,
  CONSTRAINT `comprehensive_question_history_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `comprehensive_question_history_ibfk_2` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for comprehensive_report_history
-- ----------------------------
DROP TABLE IF EXISTS `comprehensive_report_history`;
CREATE TABLE `comprehensive_report_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `job_id` bigint NOT NULL,
  `overall_score` decimal(5,2) DEFAULT NULL COMMENT '综合评分分数',
  `resume_score` decimal(5,2) DEFAULT NULL COMMENT '简历评分分数',
  `question_score` decimal(5,2) DEFAULT NULL COMMENT '试题评分分数',
  `scenario_score` decimal(5,2) DEFAULT NULL COMMENT '场景评分分数',
  `strength_analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '优势分析',
  `improvement_analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '改进建议',
  `learning_route` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '学习路线',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `job_id` (`job_id`) USING BTREE,
  KEY `idx_user_job` (`user_id`,`job_id`) USING BTREE,
  CONSTRAINT `comprehensive_report_history_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `comprehensive_report_history_ibfk_2` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for comprehensive_resume_history
-- ----------------------------
DROP TABLE IF EXISTS `comprehensive_resume_history`;
CREATE TABLE `comprehensive_resume_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `job_id` bigint NOT NULL,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `file_size` int unsigned NOT NULL,
  `file_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `upload_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `overall_score` tinyint unsigned DEFAULT NULL COMMENT '总体评分(0-100)',
  `skill_match` tinyint unsigned DEFAULT NULL COMMENT '技能匹配度(0-100)',
  `experience_match` tinyint unsigned DEFAULT NULL COMMENT '经验匹配度(0-100)',
  `education_match` tinyint unsigned DEFAULT NULL COMMENT '教育背景匹配度(0-100)',
  `communication_skill` tinyint unsigned DEFAULT NULL COMMENT '沟通能力(0-100)',
  `leadership_skill` tinyint unsigned DEFAULT NULL COMMENT '领导力(0-100)',
  `ai_analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT 'AI分析结果JSON',
  `ai_suggestions` json DEFAULT NULL COMMENT 'AI改进建议',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `job_id` (`job_id`) USING BTREE,
  KEY `idx_user_job` (`user_id`,`job_id`) USING BTREE,
  CONSTRAINT `comprehensive_resume_history_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `comprehensive_resume_history_ibfk_2` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=63 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for comprehensive_scenario_history
-- ----------------------------
DROP TABLE IF EXISTS `comprehensive_scenario_history`;
CREATE TABLE `comprehensive_scenario_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `job_id` bigint NOT NULL,
  `start_time` timestamp NULL DEFAULT NULL,
  `end_time` timestamp NULL DEFAULT NULL,
  `total_time` int unsigned DEFAULT NULL COMMENT '总用时(秒)',
  `overall_score` tinyint unsigned DEFAULT NULL COMMENT '总体评分(0-100)',
  `fluency_score` tinyint unsigned DEFAULT NULL COMMENT '语音流畅度(0-100)',
  `emotion_score` tinyint unsigned DEFAULT NULL COMMENT '情感表达(0-100)',
  `relevance_score` tinyint unsigned DEFAULT NULL COMMENT '内容相关性(0-100)',
  `adaptability_score` tinyint unsigned DEFAULT NULL COMMENT '应变能力(0-100)',
  `ai_assessment` json DEFAULT NULL COMMENT 'AI评估详情',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `job_id` (`job_id`) USING BTREE,
  KEY `idx_user_job` (`user_id`,`job_id`) USING BTREE,
  CONSTRAINT `comprehensive_scenario_history_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `comprehensive_scenario_history_ibfk_2` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

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
-- Table structure for agent_conversation_session
-- ----------------------------
DROP TABLE IF EXISTS `agent_conversation_session`;
CREATE TABLE `agent_conversation_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '所属用户 ID',
  `agent_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Agent 标识',
  `session_id` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '前端会话 ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '会话标题',
  `preview` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '会话预览',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_session` (`user_id`, `session_id`) USING BTREE,
  UNIQUE KEY `uk_session_id` (`session_id`) USING BTREE,
  KEY `idx_user_agent` (`user_id`, `agent_key`) USING BTREE,
  KEY `idx_updated_at` (`updated_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='Agent 会话主表';

-- ----------------------------
-- Table structure for agent_conversation_message
-- ----------------------------
DROP TABLE IF EXISTS `agent_conversation_message`;
CREATE TABLE `agent_conversation_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话 ID',
  `turn_no` int DEFAULT NULL COMMENT '对话轮次',
  `role` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'user/assistant/system',
  `content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_session_turn` (`session_id`, `turn_no`) USING BTREE,
  KEY `idx_session_created` (`session_id`, `created_at`) USING BTREE,
  CONSTRAINT `fk_agent_conversation_message_session_id` FOREIGN KEY (`session_id`) REFERENCES `agent_conversation_session` (`session_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='Agent 会话消息表';

-- ----------------------------
-- Table structure for agent_conversation_event
-- ----------------------------
DROP TABLE IF EXISTS `agent_conversation_event`;
CREATE TABLE `agent_conversation_event` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话 ID',
  `turn_no` int DEFAULT NULL COMMENT '关联对话轮次',
  `event_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '事件类型',
  `payload_json` json DEFAULT NULL COMMENT '事件结构化负载',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_session_event` (`session_id`, `event_type`) USING BTREE,
  KEY `idx_session_created` (`session_id`, `created_at`) USING BTREE,
  CONSTRAINT `fk_agent_conversation_event_session_id` FOREIGN KEY (`session_id`) REFERENCES `agent_conversation_session` (`session_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='Agent 会话事件表';

-- ----------------------------
-- Table structure for agent_conversation_memory
-- ----------------------------
DROP TABLE IF EXISTS `agent_conversation_memory`;
CREATE TABLE `agent_conversation_memory` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话 ID',
  `summary_hash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '压缩摘要来源哈希',
  `summary_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '历史摘要内容',
  `recent_turns_json` json DEFAULT NULL COMMENT '最近 3 轮对话原文',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_session_id` (`session_id`) USING BTREE,
  KEY `idx_updated_at` (`updated_at`) USING BTREE,
  CONSTRAINT `fk_agent_conversation_memory_session_id` FOREIGN KEY (`session_id`) REFERENCES `agent_conversation_session` (`session_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='Agent 会话记忆表';

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
-- Table structure for resume_analysis_history
-- ----------------------------
DROP TABLE IF EXISTS `resume_analysis_history`;
CREATE TABLE `resume_analysis_history` (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `job_id` bigint DEFAULT NULL COMMENT '职位ID',
  `resume_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '简历内容',
  `score` int DEFAULT NULL COMMENT '评分',
  `advantages` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '优势',
  `disadvantages` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '不足',
  `improvement_suggestions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '改进建议',
  `professional_skills` int DEFAULT NULL COMMENT '专业技能评分',
  `project_execution` int DEFAULT NULL COMMENT '项目执行评分',
  `innovation` int DEFAULT NULL COMMENT '创新能力评分',
  `communication` int DEFAULT NULL COMMENT '沟通能力评分',
  `adaptability` int DEFAULT NULL COMMENT '适应能力评分',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '更新时间',
  `evaluation_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SPECIAL' COMMENT '评估类型：SPECIAL-专项测评，COMPREHENSIVE-综合测评',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='简历分析历史表';

-- ----------------------------
-- Table structure for resume_historical_copy
-- ----------------------------
DROP TABLE IF EXISTS `resume_historical_copy`;
CREATE TABLE `resume_historical_copy` (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `resume_history_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '简历历史关联ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '内容',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='简历历史副本表';

-- ----------------------------
-- Table structure for scenario_analysis
-- ----------------------------
DROP TABLE IF EXISTS `scenario_analysis`;
CREATE TABLE `scenario_analysis` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `total_score` int NOT NULL COMMENT '总分',
  `score1` int NOT NULL COMMENT '第一题得分',
  `suggestion1` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '第一题建议',
  `score2` int NOT NULL COMMENT '第二题得分',
  `suggestion2` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '第二题建议',
  `score3` int NOT NULL COMMENT '第三题得分',
  `suggestion3` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '第三题建议',
  `score4` int NOT NULL COMMENT '第四题得分',
  `suggestion4` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '第四题建议',
  `score5` int NOT NULL COMMENT '第五题得分',
  `suggestion5` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '第五题建议',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `evaluation_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'SPECIAL' COMMENT '评估类型：SPECIAL-专项测评，COMPREHENSIVE-综合测评',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='场景分析表';

-- ----------------------------
-- Table structure for scenario_question
-- ----------------------------
DROP TABLE IF EXISTS `scenario_question`;
CREATE TABLE `scenario_question` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `job_id` bigint NOT NULL COMMENT '关联岗位ID',
  `question` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题目内容',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_job_id` (`job_id`) USING BTREE,
  CONSTRAINT `scenario_question_ibfk_1` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=174 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='面试场景题库表';

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

SET FOREIGN_KEY_CHECKS = 1;
