-- Apply once after taking a backup. Adds shared post media metadata and one-level comment threads.
CREATE TABLE post_media (
  media_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '게시글 미디어 식별 번호',
  post_id BIGINT NOT NULL COMMENT '미디어가 첨부된 게시글 식별 번호',
  media_type VARCHAR(10) NOT NULL COMMENT '미디어 종류 (IMAGE, VIDEO)',
  media_url VARCHAR(1000) NOT NULL COMMENT 'Cloudinary HTTPS 제공 URL',
  public_id VARCHAR(255) NOT NULL COMMENT 'Cloudinary 파일 삭제·관리용 공개 식별자',
  original_name VARCHAR(255) NOT NULL COMMENT '업로드 당시 원본 파일명',
  content_type VARCHAR(100) NOT NULL COMMENT '검증된 MIME 타입',
  file_size BIGINT NOT NULL COMMENT '파일 크기 (바이트)',
  display_order INT NOT NULL COMMENT '게시글 내 표시 순서 (0부터 시작)',
  created_at DATETIME(6) DEFAULT NULL COMMENT '생성 일시',
  created_by VARCHAR(255) DEFAULT NULL COMMENT '생성자',
  updated_at DATETIME(6) DEFAULT NULL COMMENT '최종 수정 일시',
  updated_by VARCHAR(255) DEFAULT NULL COMMENT '최종 수정자',
  PRIMARY KEY (media_id),
  CONSTRAINT uk_post_media_public_id UNIQUE (public_id),
  CONSTRAINT uk_post_media_order UNIQUE (post_id, display_order),
  CONSTRAINT fk_post_media_post FOREIGN KEY (post_id) REFERENCES post(post_id) ON DELETE CASCADE,
  CONSTRAINT chk_post_media_type CHECK (media_type IN ('IMAGE', 'VIDEO')),
  CONSTRAINT chk_post_media_size CHECK (file_size > 0),
  CONSTRAINT chk_post_media_order CHECK (display_order >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='게시글에 첨부된 Cloudinary 이미지·동영상 메타데이터';

CREATE TABLE comments (
  comment_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '댓글 식별 번호',
  post_id BIGINT NOT NULL COMMENT '댓글이 작성된 게시글 식별 번호',
  member_id BIGINT NOT NULL COMMENT '댓글 작성 회원 식별 번호',
  parent_comment_id BIGINT DEFAULT NULL COMMENT '부모 댓글 식별 번호 (NULL이면 최상위 댓글)',
  content VARCHAR(1000) NOT NULL COMMENT '댓글 내용 (최대 1000자)',
  created_at DATETIME(6) DEFAULT NULL COMMENT '생성 일시',
  created_by VARCHAR(255) DEFAULT NULL COMMENT '생성자',
  updated_at DATETIME(6) DEFAULT NULL COMMENT '최종 수정 일시',
  updated_by VARCHAR(255) DEFAULT NULL COMMENT '최종 수정자',
  PRIMARY KEY (comment_id),
  KEY idx_comments_post_created (post_id, created_at),
  KEY idx_comments_member (member_id),
  KEY idx_comments_parent (parent_comment_id),
  CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES post(post_id) ON DELETE CASCADE,
  CONSTRAINT fk_comments_member FOREIGN KEY (member_id) REFERENCES members(member_id),
  CONSTRAINT fk_comments_parent FOREIGN KEY (parent_comment_id) REFERENCES comments(comment_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='게시글 댓글 및 한 단계 대댓글';
