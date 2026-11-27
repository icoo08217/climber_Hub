-- Apply once after taking a backup. Stores the current Cloudinary profile image reference for each member.
ALTER TABLE members
  ADD COLUMN profile_image_url VARCHAR(1000) DEFAULT NULL COMMENT '회원 프로필 사진 HTTPS URL' AFTER password,
  ADD COLUMN profile_image_public_id VARCHAR(255) DEFAULT NULL COMMENT '프로필 사진 삭제용 Cloudinary 공개 ID' AFTER profile_image_url;
