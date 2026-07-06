CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(16) NOT NULL COMMENT '用户账号',
  `password` varchar(255) NOT NULL COMMENT '加密后的密码',
  `nickname` varchar(50) DEFAULT NULL COMMENT '用户昵称',
  `avatar_url` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `user_type` tinyint DEFAULT '1' COMMENT '用户类型：1-普通用户，2-会员用户，3-管理员',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `gender` tinyint DEFAULT '1' COMMENT '性别：0-未知，1-男，2-女',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-正常，0-禁用',
  `last_login_at` timestamp NULL DEFAULT NULL COMMENT '最后登录时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `eye_care_mode` tinyint DEFAULT '0' COMMENT '护眼模式：0-关闭，1-开启',
  `surprise_mode` tinyint DEFAULT '0' COMMENT '惊喜模式：0-关闭，1-开启',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_username` (`username`),
  KEY `idx_user_type` (`user_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

CREATE TABLE `job_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `level` tinyint NOT NULL COMMENT '层级：1-一级分类，2-二级分类',
  `parent_id` bigint DEFAULT NULL COMMENT '父分类ID',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='岗位分类表';

CREATE TABLE `job` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category_id` bigint NOT NULL COMMENT '所属二级分类ID',
  `name` varchar(100) NOT NULL COMMENT '岗位名称',
  `description` text COMMENT '岗位描述',
  `requirements` text COMMENT '岗位要求',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `category_id` (`category_id`),
  CONSTRAINT `job_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `job_category` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='岗位表';

CREATE TABLE `ai_assessment_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assessment_session_id` varchar(64) NOT NULL COMMENT 'AIview测评流程实例ID',
  `user_id` bigint NOT NULL,
  `job_id` bigint NOT NULL,
  `status` varchar(32) NOT NULL COMMENT 'IN_PROGRESS/PASSED/FAILED',
  `current_stage` varchar(32) NOT NULL COMMENT 'resume/questions/round_1/round_2/round_3/completed',
  `elimination_reason` varchar(255) DEFAULT NULL,
  `final_summary` text,
  `final_suggestion` text,
  `start_time` timestamp NULL DEFAULT NULL,
  `end_time` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_assessment_session_id` (`assessment_session_id`),
  KEY `idx_assessment_user_job` (`user_id`,`job_id`),
  CONSTRAINT `ai_assessment_session_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `ai_assessment_session_ibfk_2` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AIview测评流程实例表';

CREATE TABLE `ai_resume_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assessment_session_id` varchar(64) DEFAULT NULL COMMENT 'AIview测评流程实例ID',
  `user_id` bigint NOT NULL,
  `job_id` bigint NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_size` int unsigned NOT NULL,
  `file_type` varchar(20) NOT NULL,
  `upload_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `overall_score` tinyint unsigned DEFAULT NULL COMMENT '总体评分(0-100)',
  `ai_analysis` text COMMENT 'AI分析结果JSON',
  `ai_suggestions` json DEFAULT NULL COMMENT 'AI改进建议',
  PRIMARY KEY (`id`),
  KEY `job_id` (`job_id`),
  KEY `idx_assessment_session_id` (`assessment_session_id`),
  KEY `idx_user_job` (`user_id`,`job_id`),
  CONSTRAINT `ai_resume_history_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `ai_resume_history_ibfk_2` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI简历评测历史表';

CREATE TABLE `ai_question_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assessment_session_id` varchar(64) DEFAULT NULL COMMENT 'AIview测评流程实例ID',
  `user_id` bigint NOT NULL,
  `job_id` bigint NOT NULL,
  `start_time` timestamp NULL DEFAULT NULL,
  `end_time` timestamp NULL DEFAULT NULL,
  `total_time` int unsigned DEFAULT NULL COMMENT '总用时(秒)',
  `overall_score` tinyint unsigned DEFAULT NULL COMMENT '总体评分(0-100)',
  `ai_suggestions` json DEFAULT NULL COMMENT 'AI改进建议',
  PRIMARY KEY (`id`),
  KEY `job_id` (`job_id`),
  KEY `idx_assessment_session_id` (`assessment_session_id`),
  KEY `idx_user_job` (`user_id`,`job_id`),
  CONSTRAINT `ai_question_history_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `ai_question_history_ibfk_2` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI试题作答历史表';

CREATE TABLE `ai_interview_round_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assessment_session_id` varchar(64) DEFAULT NULL COMMENT 'AIview测评流程实例ID',
  `round_type` varchar(32) DEFAULT NULL COMMENT '面试轮次(round_1/round_2/round_3)',
  `user_id` bigint NOT NULL,
  `job_id` bigint NOT NULL,
  `start_time` timestamp NULL DEFAULT NULL,
  `end_time` timestamp NULL DEFAULT NULL,
  `total_time` int unsigned DEFAULT NULL COMMENT '总用时(秒)',
  `overall_score` tinyint unsigned DEFAULT NULL COMMENT '总体评分(0-100)',
  `ai_assessment` json DEFAULT NULL COMMENT 'AI评估详情',
  PRIMARY KEY (`id`),
  KEY `job_id` (`job_id`),
  KEY `idx_assessment_session_id` (`assessment_session_id`),
  KEY `idx_user_job` (`user_id`,`job_id`),
  CONSTRAINT `ai_interview_round_history_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `ai_interview_round_history_ibfk_2` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI面试轮次历史表';

CREATE TABLE `agent_conversation_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '所属用户 ID',
  `agent_key` varchar(64) NOT NULL COMMENT 'Agent 标识',
  `chat_id` varchar(80) NOT NULL COMMENT '后端稳定会话 ID',
  `title` varchar(255) DEFAULT NULL COMMENT '会话标题',
  `preview` varchar(500) DEFAULT NULL COMMENT '会话预览',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_chat` (`user_id`, `chat_id`),
  UNIQUE KEY `uk_chat_id` (`chat_id`),
  KEY `idx_user_agent` (`user_id`, `agent_key`),
  KEY `idx_updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Agent 会话主表';

CREATE TABLE `agent_conversation_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `chat_id` varchar(80) NOT NULL COMMENT '会话 ID',
  `turn_no` int DEFAULT NULL COMMENT '对话轮次',
  `role` varchar(16) NOT NULL COMMENT 'user/assistant/system',
  `content` mediumtext NOT NULL COMMENT '消息内容',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_chat_turn` (`chat_id`, `turn_no`),
  KEY `idx_chat_created` (`chat_id`, `created_at`),
  CONSTRAINT `fk_agent_conversation_message_chat_id` FOREIGN KEY (`chat_id`) REFERENCES `agent_conversation_session` (`chat_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Agent 会话消息表';

CREATE TABLE `agent_conversation_event` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `chat_id` varchar(80) NOT NULL COMMENT '会话 ID',
  `turn_no` int DEFAULT NULL COMMENT '关联对话轮次',
  `event_type` varchar(64) NOT NULL COMMENT '事件类型',
  `payload_json` json DEFAULT NULL COMMENT '事件结构化负载',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_chat_event` (`chat_id`, `event_type`),
  KEY `idx_chat_created` (`chat_id`, `created_at`),
  CONSTRAINT `fk_agent_conversation_event_chat_id` FOREIGN KEY (`chat_id`) REFERENCES `agent_conversation_session` (`chat_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Agent 会话事件表';

CREATE TABLE `agent_conversation_memory` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `chat_id` varchar(80) NOT NULL COMMENT '会话 ID',
  `summary_hash` varchar(64) DEFAULT NULL COMMENT '压缩摘要来源哈希',
  `summary_content` text COMMENT '历史摘要内容',
  `recent_turns_json` json DEFAULT NULL COMMENT '最近 3 轮对话原文',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_chat_id` (`chat_id`),
  KEY `idx_updated_at` (`updated_at`),
  CONSTRAINT `fk_agent_conversation_memory_chat_id` FOREIGN KEY (`chat_id`) REFERENCES `agent_conversation_session` (`chat_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Agent 会话记忆表';

CREATE TABLE `feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` varchar(20) NOT NULL COMMENT '反馈类型：BUG/建议/其他',
  `title` varchar(100) NOT NULL COMMENT '反馈标题',
  `content` text NOT NULL COMMENT '反馈内容',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/处理中/已完成',
  `reply` text COMMENT '管理员回复内容',
  `reply_time` timestamp NULL DEFAULT NULL COMMENT '回复时间',
  `admin_id` bigint DEFAULT NULL COMMENT '处理管理员ID',
  `admin_name` varchar(100) DEFAULT NULL COMMENT '处理管理员名称',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `feedback_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户反馈表';

CREATE TABLE `blessing` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '创建人用户ID',
  `content` text NOT NULL COMMENT '祝福语内容',
  `type` varchar(50) DEFAULT NULL COMMENT '祝福语类型',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/INACTIVE',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_blessing_status` (`status`),
  KEY `idx_blessing_type` (`type`),
  KEY `idx_blessing_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='祝福语表';

INSERT INTO `job_category` (`id`, `name`, `level`, `parent_id`, `sort_order`, `status`) VALUES
(101,'人工智能',2,NULL,101,1),(102,'机器学习',2,NULL,102,1),(103,'自然语言处理',2,NULL,103,1),(104,'智能算法',2,NULL,104,1),
(201,'大数据开发',2,NULL,201,1),(202,'数据分析',2,NULL,202,1),(203,'数据架构',2,NULL,203,1),(204,'数据治理',2,NULL,204,1),
(301,'嵌入式开发',2,NULL,301,1),(302,'物联网架构',2,NULL,302,1),(303,'传感器开发',2,NULL,303,1),(304,'物联网安全',2,NULL,304,1),
(401,'系统架构',2,NULL,401,1),(402,'智能运维',2,NULL,402,1),(403,'自动化测试',2,NULL,403,1),(404,'智能产品',2,NULL,404,1);

INSERT INTO `job` (`id`, `category_id`, `name`, `description`, `requirements`, `status`) VALUES
(1,101,'人工智能工程师','负责将机器学习、大模型与智能系统能力落地到真实业务场景。','岗位职责\n1. 设计并交付 AI 模型和智能应用。\n2. 建设模型评测、数据闭环和线上监控。\n\n任职要求\n1. 熟悉 Python 和主流机器学习框架。\n2. 理解大模型、RAG、Prompt 和模型评估。',1),
(2,102,'机器学习工程师','负责特征工程、模型训练、效果评估和算法服务化。','岗位职责\n1. 构建分类、预测、推荐或排序模型。\n2. 推动模型上线、监控和持续迭代。\n\n任职要求\n1. 熟悉机器学习算法和实验方法。\n2. 具备数据分析与工程实现能力。',1),
(3,103,'自然语言处理工程师','负责文本理解、对话系统、信息抽取和大模型应用。','岗位职责\n1. 建设文本分类、问答、抽取和对话能力。\n2. 优化 prompt、RAG 和模型评测链路。\n\n任职要求\n1. 熟悉 NLP 基础任务和 Transformer 架构。\n2. 有大模型应用或微调经验优先。',1),
(4,201,'大数据开发工程师','负责离线数仓、实时计算和数据链路建设。','岗位职责\n1. 开发 Spark、Flink、Hive、Kafka 等数据任务。\n2. 优化任务性能、稳定性和资源成本。\n\n任职要求\n1. 熟悉 Java/Scala/Python 至少一种语言。\n2. 理解数仓分层、维度建模和数据质量。',1),
(5,202,'数据分析师','负责业务分析、指标体系、实验评估和数据洞察。','岗位职责\n1. 建设业务指标和分析报表。\n2. 输出专题分析并推动业务决策。\n\n任职要求\n1. 熟练掌握 SQL 和常用分析工具。\n2. 具备统计分析和业务拆解能力。',1),
(6,203,'数据架构师','负责企业数据架构、数据模型和数据平台演进规划。','岗位职责\n1. 设计数仓、湖仓一体和数据服务方案。\n2. 制定建模、质量、安全和权限规范。\n\n任职要求\n1. 熟悉分布式计算和数据治理体系。\n2. 有大型数据平台架构经验。',1),
(7,301,'嵌入式软件工程师','负责智能硬件和物联网设备的软件开发。','岗位职责\n1. 开发驱动、系统服务和设备通信能力。\n2. 完成低功耗、OTA、日志诊断和异常恢复。\n\n任职要求\n1. 熟悉 C/C++、Linux 或 RTOS。\n2. 理解 UART、I2C、SPI、CAN、BLE 等协议。',1),
(8,302,'物联网架构师','负责设备接入、消息通信、规则引擎和边缘协同架构。','岗位职责\n1. 设计物联网云平台和边缘平台。\n2. 解决海量设备连接和高并发消息问题。\n\n任职要求\n1. 熟悉 MQTT、CoAP、WebSocket 和 TCP/IP。\n2. 有分布式系统和高可用架构经验。',1),
(9,304,'物联网安全专家','负责设备、通信链路、云平台和数据安全体系建设。','岗位职责\n1. 建设设备认证、密钥管理和 OTA 安全方案。\n2. 开展漏洞挖掘、协议分析和安全治理。\n\n任职要求\n1. 熟悉网络安全、系统安全和常见攻防技术。\n2. 有固件分析或云平台安全经验。',1),
(10,401,'系统架构师','负责大型业务系统和技术中台的整体架构设计。','岗位职责\n1. 设计高并发、高可用和可演进系统。\n2. 推动服务治理、稳定性治理和技术债治理。\n\n任职要求\n1. 精通 Java/Go/C++ 至少一种语言。\n2. 熟悉微服务、缓存、消息队列和数据库设计。',1),
(11,402,'智能运维工程师','负责监控告警、自动化发布、故障自愈和 AIOps 能力。','岗位职责\n1. 建设可观测性、容量管理和应急响应机制。\n2. 推动自动化运维和稳定性治理。\n\n任职要求\n1. 熟悉 Linux、Kubernetes、Prometheus、ELK/Loki。\n2. 熟练使用 Shell/Python/Go 自动化开发。',1),
(12,403,'自动化测试工程师','负责质量保障体系和自动化测试平台建设。','岗位职责\n1. 设计接口、UI、性能和稳定性测试方案。\n2. 建设 CI/CD 质量门禁和测试工具。\n\n任职要求\n1. 熟悉 Python/Java/JavaScript 至少一种语言。\n2. 熟悉 HTTP、数据库、Mock 和常见测试框架。',1),
(13,404,'智能产品经理','负责 AI 产品规划、设计、指标体系和商业化落地。','岗位职责\n1. 将模型能力转化为可用产品方案。\n2. 协同算法、研发、设计和运营推动落地。\n\n任职要求\n1. 具备用户洞察、需求抽象和产品设计能力。\n2. 理解大模型、推荐、搜索或智能对话边界。',1),
(14,303,'传感器开发工程师','负责传感器选型、驱动适配、信号处理和可靠性验证。','岗位职责\n1. 完成传感器集成、采集、标定和滤波。\n2. 配合硬件和测试团队完成样机调试。\n\n任职要求\n1. 熟悉常见传感器原理和接口协议。\n2. 具备基础硬件调试和数据处理能力。',1),
(15,204,'数据治理专家','负责数据标准、质量、元数据、主数据和数据安全治理。','岗位职责\n1. 建设数据治理体系和质量规则。\n2. 推动口径统一、血缘分析和权限管理。\n\n任职要求\n1. 熟悉 DAMA、数据标准和数据质量管理。\n2. 熟悉 SQL、数仓建模和治理工具。',1),
(16,104,'智能算法工程师','负责预测、推荐、排序、优化和智能决策算法落地。','岗位职责\n1. 面向业务问题完成建模、训练和评估。\n2. 建设实验评估、模型监控和效果归因。\n\n任职要求\n1. 熟悉机器学习、优化算法和概率统计。\n2. 熟练使用 Python 和主流算法框架。',1);
