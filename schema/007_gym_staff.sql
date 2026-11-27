-- Apply once after taking a backup. Adds per-gym staff ownership without changing USER/ADMIN roles.
CREATE TABLE gym_staff (
  gym_staff_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '암장 운영 권한 식별 번호',
  member_id BIGINT NOT NULL COMMENT '운영 권한을 가진 회원 식별 번호',
  gym_id BIGINT NOT NULL COMMENT '운영 대상 암장 식별 번호',
  gym_role VARCHAR(20) NOT NULL COMMENT '암장별 역할 (OWNER)',
  created_at DATETIME(6) DEFAULT NULL COMMENT '생성 일시',
  created_by VARCHAR(255) DEFAULT NULL COMMENT '생성자',
  updated_at DATETIME(6) DEFAULT NULL COMMENT '최종 수정 일시',
  updated_by VARCHAR(255) DEFAULT NULL COMMENT '최종 수정자',
  PRIMARY KEY (gym_staff_id),
  CONSTRAINT uk_gym_staff_member_gym UNIQUE (member_id, gym_id),
  KEY idx_gym_staff_gym_role (gym_id, gym_role),
  CONSTRAINT fk_gym_staff_member FOREIGN KEY (member_id) REFERENCES members(member_id),
  CONSTRAINT fk_gym_staff_gym FOREIGN KEY (gym_id) REFERENCES gym(gym_id) ON DELETE CASCADE,
  CONSTRAINT chk_gym_staff_role CHECK (gym_role IN ('OWNER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='회원의 암장별 운영 권한';
