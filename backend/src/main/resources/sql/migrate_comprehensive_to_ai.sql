-- Migrate an existing local database from the legacy comprehensive_* schema to AIview ai_* schema.
-- This script is intended for already-initialized environments; fresh environments use interview_agent.sql directly.

RENAME TABLE comprehensive_resume_history TO ai_resume_history;
ALTER TABLE ai_resume_history
  ADD COLUMN assessment_session_id varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'AIview测评流程实例ID' AFTER id,
  ADD KEY idx_assessment_session_id (assessment_session_id) USING BTREE;
ALTER TABLE ai_resume_history
  DROP COLUMN IF EXISTS skill_match,
  DROP COLUMN IF EXISTS experience_match,
  DROP COLUMN IF EXISTS education_match,
  DROP COLUMN IF EXISTS communication_skill,
  DROP COLUMN IF EXISTS leadership_skill;

RENAME TABLE comprehensive_question_history TO ai_question_history;
ALTER TABLE ai_question_history
  ADD COLUMN assessment_session_id varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'AIview测评流程实例ID' AFTER id,
  ADD KEY idx_assessment_session_id (assessment_session_id) USING BTREE;

RENAME TABLE comprehensive_scenario_history TO ai_interview_round_history;
ALTER TABLE ai_interview_round_history
  ADD COLUMN assessment_session_id varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'AIview测评流程实例ID' AFTER id,
  ADD COLUMN round_type varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '面试轮次(round_1/round_2/round_3)' AFTER assessment_session_id,
  ADD KEY idx_assessment_session_id (assessment_session_id) USING BTREE;
ALTER TABLE ai_interview_round_history
  DROP COLUMN IF EXISTS fluency_score,
  DROP COLUMN IF EXISTS emotion_score,
  DROP COLUMN IF EXISTS relevance_score,
  DROP COLUMN IF EXISTS adaptability_score;
UPDATE ai_interview_round_history
SET round_type = 'round_1'
WHERE round_type IS NULL;

CREATE TABLE IF NOT EXISTS ai_assessment_session (
  id bigint NOT NULL AUTO_INCREMENT,
  assessment_session_id varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'AIview测评流程实例ID',
  user_id bigint NOT NULL,
  job_id bigint NOT NULL,
  status varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'IN_PROGRESS/PASSED/FAILED',
  current_stage varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'resume/questions/round_1/round_2/round_3/completed',
  elimination_reason varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  final_summary text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  final_suggestion text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  start_time timestamp NULL DEFAULT NULL,
  end_time timestamp NULL DEFAULT NULL,
  created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id) USING BTREE,
  UNIQUE KEY uk_assessment_session_id (assessment_session_id) USING BTREE,
  KEY idx_assessment_user_job (user_id, job_id) USING BTREE,
  CONSTRAINT ai_assessment_session_ibfk_1 FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT ai_assessment_session_ibfk_2 FOREIGN KEY (job_id) REFERENCES job (id) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

DROP TABLE IF EXISTS comprehensive_report_history;
