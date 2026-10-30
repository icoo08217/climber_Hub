-- 2026-10-30: table and column comments only; preserve existing definitions.

ALTER TABLE `gym`
  COMMENT = '클라이밍장 정보',
  MODIFY COLUMN `latitude` double DEFAULT NULL COMMENT '위도',
  MODIFY COLUMN `longitude` double DEFAULT NULL COMMENT '경도',
  MODIFY COLUMN `created_at` datetime(6) DEFAULT NULL COMMENT '생성 일시',
  MODIFY COLUMN `gym_id` bigint NOT NULL AUTO_INCREMENT COMMENT '암장 식별 번호',
  MODIFY COLUMN `updated_at` datetime(6) DEFAULT NULL COMMENT '최종 수정 일시',
  MODIFY COLUMN `address` varchar(255) NOT NULL COMMENT '암장 주소',
  MODIFY COLUMN `created_by` varchar(255) DEFAULT NULL COMMENT '생성자',
  MODIFY COLUMN `name` varchar(255) NOT NULL COMMENT '암장 이름',
  MODIFY COLUMN `open_hours` varchar(255) DEFAULT NULL COMMENT '운영 시간 안내',
  MODIFY COLUMN `phone` varchar(255) DEFAULT NULL COMMENT '암장 연락처',
  MODIFY COLUMN `updated_by` varchar(255) DEFAULT NULL COMMENT '최종 수정자',
  MODIFY COLUMN `description` text COMMENT '암장 소개',
  MODIFY COLUMN `pricing_info` text COMMENT '이용 요금 안내';

ALTER TABLE `members`
  COMMENT = '회원 정보',
  MODIFY COLUMN `created_at` datetime(6) DEFAULT NULL COMMENT '생성 일시',
  MODIFY COLUMN `member_id` bigint NOT NULL AUTO_INCREMENT COMMENT '회원 식별 번호 (내부 기본 키)',
  MODIFY COLUMN `login_id` varchar(50) NOT NULL COMMENT '로그인 ID (소문자, 숫자, 밑줄)',
  MODIFY COLUMN `updated_at` datetime(6) DEFAULT NULL COMMENT '최종 수정 일시',
  MODIFY COLUMN `created_by` varchar(255) DEFAULT NULL COMMENT '생성자',
  MODIFY COLUMN `email` varchar(255) NOT NULL COMMENT '회원 이메일 (중복 불가)',
  MODIFY COLUMN `name` varchar(255) NOT NULL COMMENT '이름 또는 닉네임',
  MODIFY COLUMN `password` varchar(255) NOT NULL COMMENT '비밀번호 해시 (BCrypt)',
  MODIFY COLUMN `updated_by` varchar(255) DEFAULT NULL COMMENT '최종 수정자',
  MODIFY COLUMN `role` enum('ADMIN','USER') NOT NULL COMMENT '회원 권한 (ADMIN: 관리자, USER: 일반 회원)';

ALTER TABLE `post`
  COMMENT = '게시글',
  MODIFY COLUMN `created_at` datetime(6) DEFAULT NULL COMMENT '생성 일시',
  MODIFY COLUMN `gym_id` bigint DEFAULT NULL COMMENT '소속 암장 식별 번호 (전체 게시글은 NULL 가능)',
  MODIFY COLUMN `member_id` bigint DEFAULT NULL COMMENT '작성 회원 식별 번호',
  MODIFY COLUMN `post_id` bigint NOT NULL AUTO_INCREMENT COMMENT '게시글 식별 번호',
  MODIFY COLUMN `updated_at` datetime(6) DEFAULT NULL COMMENT '최종 수정 일시',
  MODIFY COLUMN `created_by` varchar(255) DEFAULT NULL COMMENT '생성자',
  MODIFY COLUMN `title` varchar(255) NOT NULL COMMENT '게시글 제목',
  MODIFY COLUMN `updated_by` varchar(255) DEFAULT NULL COMMENT '최종 수정자',
  MODIFY COLUMN `content` tinytext NOT NULL COMMENT '게시글 본문',
  MODIFY COLUMN `post_type` enum('GLOBAL_NOTICE','GLOBAL_POST','GYM_NOTICE','GYM_POST') NOT NULL COMMENT '게시글 유형 (GLOBAL_NOTICE: 전체 공지, GLOBAL_POST: 전체 일반, GYM_NOTICE: 암장 공지, GYM_POST: 암장 일반)';

ALTER TABLE `reservation`
  COMMENT = '회원 예약',
  MODIFY COLUMN `reservation_date` date NOT NULL COMMENT '예약 날짜',
  MODIFY COLUMN `created_at` datetime(6) DEFAULT NULL COMMENT '생성 일시',
  MODIFY COLUMN `member_id` bigint NOT NULL COMMENT '예약 회원 식별 번호',
  MODIFY COLUMN `reservation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '예약 식별 번호',
  MODIFY COLUMN `timeslot_id` bigint NOT NULL COMMENT '예약 시간대 식별 번호',
  MODIFY COLUMN `updated_at` datetime(6) DEFAULT NULL COMMENT '최종 수정 일시',
  MODIFY COLUMN `created_by` varchar(255) DEFAULT NULL COMMENT '생성자',
  MODIFY COLUMN `updated_by` varchar(255) DEFAULT NULL COMMENT '최종 수정자',
  MODIFY COLUMN `status` enum('CANCEL','DONE','READY') NOT NULL COMMENT '예약 상태 (READY: 대기, CANCEL: 취소, DONE: 완료)';

ALTER TABLE `timeslot`
  COMMENT = '암장별 예약 가능 시간대',
  MODIFY COLUMN `capacity` int NOT NULL COMMENT '예약 가능 정원',
  MODIFY COLUMN `end_time` time(6) NOT NULL COMMENT '종료 시간',
  MODIFY COLUMN `start_time` time(6) NOT NULL COMMENT '시작 시간',
  MODIFY COLUMN `created_at` datetime(6) DEFAULT NULL COMMENT '생성 일시',
  MODIFY COLUMN `gym_id` bigint NOT NULL COMMENT '소속 암장 식별 번호',
  MODIFY COLUMN `timeslot_id` bigint NOT NULL AUTO_INCREMENT COMMENT '시간대 식별 번호',
  MODIFY COLUMN `updated_at` datetime(6) DEFAULT NULL COMMENT '최종 수정 일시',
  MODIFY COLUMN `created_by` varchar(255) DEFAULT NULL COMMENT '생성자',
  MODIFY COLUMN `updated_by` varchar(255) DEFAULT NULL COMMENT '최종 수정자',
  MODIFY COLUMN `day_of_week` enum('FRIDAY','MONDAY','SATURDAY','SUNDAY','THURSDAY','TUESDAY','WEDNESDAY') NOT NULL COMMENT '예약 가능 요일 (MONDAY~SUNDAY)';
