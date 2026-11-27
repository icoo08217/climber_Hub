-- Apply once after taking a backup. Adds source identity and administrator verification metadata to gym.
ALTER TABLE gym
  ADD COLUMN external_source VARCHAR(20) NOT NULL DEFAULT 'ADMIN' COMMENT '암장 데이터 출처 (ADMIN, CURATED, OSM)' AFTER pricing_info,
  ADD COLUMN external_id VARCHAR(255) DEFAULT NULL COMMENT '출처 내부에서 암장을 식별하는 외부 ID' AFTER external_source,
  ADD COLUMN verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '암장 검증 상태 (PENDING, VERIFIED, REJECTED)' AFTER external_id,
  ADD COLUMN active BIT(1) NOT NULL DEFAULT b'1' COMMENT '서비스 노출 및 운영 활성 여부' AFTER verification_status,
  ADD COLUMN verified_at DATETIME(6) DEFAULT NULL COMMENT '관리자 검증 완료 일시' AFTER active,
  ADD CONSTRAINT uk_gym_external_source_id UNIQUE (external_source, external_id),
  ADD KEY idx_gym_verification_active (verification_status, active),
  ADD CONSTRAINT chk_gym_external_source CHECK (external_source IN ('ADMIN', 'CURATED', 'OSM')),
  ADD CONSTRAINT chk_gym_verification_status CHECK (verification_status IN ('PENDING', 'VERIFIED', 'REJECTED'));
