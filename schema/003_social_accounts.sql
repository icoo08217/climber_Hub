-- Back up the database first. Apply once to the existing climber_hub schema.
ALTER TABLE members
  MODIFY COLUMN login_id VARCHAR(50) NULL COMMENT '로그인 ID (소문자, 숫자, 밑줄; 소셜 전용 회원은 NULL)',
  MODIFY COLUMN password VARCHAR(255) NULL COMMENT '비밀번호 해시 (BCrypt; 소셜 전용 회원은 NULL)',
  MODIFY COLUMN email VARCHAR(255) NULL COMMENT '회원 이메일 (중복 불가; 미제공 시 NULL)';

CREATE TABLE social_accounts (
  social_account_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '소셜 계정 연결 식별 번호',
  member_id BIGINT NOT NULL COMMENT '연결된 내부 회원 식별 번호',
  provider VARCHAR(20) NOT NULL COMMENT '인증 제공자 (KAKAO, NAVER, GOOGLE)',
  provider_user_id VARCHAR(255) COLLATE utf8mb4_bin NOT NULL COMMENT '인증 제공자가 발급한 사용자 식별자',
  created_at DATETIME(6) DEFAULT NULL COMMENT '생성 일시',
  created_by VARCHAR(255) DEFAULT NULL COMMENT '생성자',
  updated_at DATETIME(6) DEFAULT NULL COMMENT '최종 수정 일시',
  updated_by VARCHAR(255) DEFAULT NULL COMMENT '최종 수정자',
  PRIMARY KEY (social_account_id),
  CONSTRAINT uk_social_provider_user UNIQUE (provider, provider_user_id),
  CONSTRAINT uk_social_member_provider UNIQUE (member_id, provider),
  CONSTRAINT fk_social_member FOREIGN KEY (member_id) REFERENCES members(member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='회원별 소셜 로그인 계정 연결';
