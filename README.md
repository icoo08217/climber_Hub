## 프로젝트 개요: 암장 예약 관리 시스템
### 🎯 목표
>사용자는 암장 선택 후, 시간대를 선택해서 예약하고 관리자는 이를 확인 및 승인/거절할 수 있는 시스템 

### 📦 주요 기능
#### [사용자 기능]
- 회원가입 / 로그인 (JWT 기반)
- 예약 가능한 암장, 날짜 및 시간 확인
- 예약 등록 / 수정 / 취소
- 예약 내역 조회

#### [관리자 기능]
- 전체 예약 관리 목록 확인 (필터링: 날짜 / 예약 상태 등)
- 예약 승인 / 거절 / 수정 처리
- 예약 통계 확인 (선택적)

### 📆 UI 구성 예시
- 메인 페이지: 예약 버튼, 나의 예약 보기
- 캘린더/시간 선택 UI: 날짜 클릭 → 예약 가능한 시간 리스트 출력
- 관리자 페이지: 전체 예약 목록, 승인/거절 버튼

### 🛠️ 프로젝트 구성 정보 (Project Configuration)
이 프로젝트는 start.spring.io를 통해 아래와 같은 설정으로 생성되었습니다.

#### 프론트엔드
| 항목 (Field) | 설정값 (Value) |
|---|---|
| React Version | (미정)|
| Next.js| (미정)|

#### 백엔드
| 항목 (Field) | 설정값 (Value) |
|---|---|
| Project | Maven Project |
| Group ID | com.climbers |
| Artifact ID | hub |
| Java Version | 17 |
| pring Boot Version | 3.3.2 |
| Packaging | Jar |
| Package Name | com.climbers.hub |

### ✅ 핵심 의존성 (Core Dependencies)
프로젝트 초기 설정 시 포함된 핵심 라이브러리는 다음과 같습니다.

Spring Web: MVC 패턴을 기반으로 웹 애플리케이션 및 RESTful API를 구축합니다.

Spring Data JPA: SQL을 직접 사용하지 않고, 객체(Entity) 중심으로 데이터베이스와 상호작용합니다.

Spring Security: 사용자의 인증(Authentication)과 인가(Authorization)를 처리하여 애플리케이션을 보호합니다.

Lombok: @Getter, @Setter, @Builder 등의 어노테이션으로 반복적인 코드를 자동 생성하여 생산성을 높입니다.

MySQL Driver: Spring 애플리케이션이 MySQL 데이터베이스와 통신할 수 있도록 연결합니다.

### 구조
TDD - 테스트 주도 개발형식으로 테스트 코드를 작성하면서 해당 기능단위 테스트 코드 작성을 지향.

### 도메인
1. Member (회원)
2. Gym (암장) 
3. Post (게시글)
4. Reservation (예약)
5. TimeSlot (예약에서 사용될 시간대)

### ERD
추후 ERD 다이어그램 추가 예정

### API List (예시)
| Method | URI | Description |
|--------|-----|----|
| GET | /reservations | 내 예약 조회 |

### 🚀 확장 아이디어
- 암장 예약 시스템(예약 생성, 조회, 취소 API)
- 점주를 위한 기능 (특정 암장의 전체 예약 현황을 조회하는 API)
- 커뮤니티 기능
- 크루 시스템 -> 크루 안의 어느 시간에만 활성화되어있는 모임 생성
- 페스티벌
- 이벤트 게시판
- 댓글
- 리뷰
- 별점
- 셋팅 일정 표기
- 실시간 채팅 기능 (암장 관리자 <> 사용자)

### 💡 개발 순서 추천 (WBS)
1. 🔐 사용자 인증 (Spring Security + JWT)
2. 📆 예약 기능 (백엔드 API → 프론트 예약 폼 연결)
3. ⚙️ 관리자 승인/거절 기능 
4. 🖥️ 프론트엔드 UI 연결 (캘린더, 시간 선택 등)
5. 📊 마이페이지 및 관리 페이지 
6. 🔔 알림 / 예외 처리 / UX 개선

### Commit Convention
```
feat : 새로운 기능 추가
fix : 버그 수정
docs : 문서 수정
style : 스타일(style)만 변경(들여쓰기 같은 포맷이나 세미콜론을 빼먹은 경우) / (코드 수정 없이)
refactor : 코드 리팩토링
test : Test 관련한 코드의 추가, 수정
chore : 설정을 변경 (코드 수정 없이)
```
