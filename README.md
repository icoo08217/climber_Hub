## 🛠️ 프로젝트 구성 정보 (Project Configuration)
이 프로젝트는 start.spring.io를 통해 아래와 같은 설정으로 생성되었습니다.

| 항목 (Field) | 설정값 (Value) |
|---|---|
| Project | Maven Project |
| Group ID | com.climbers |
| Artifact ID | hub |
| Java Version | 17 |
| pring Boot Version | 3.3.2 |
| Packaging | Jar |
| Package Name | com.climbers.hub |

Sheets로 내보내기
<br>

## ✅ 핵심 의존성 (Core Dependencies)
프로젝트 초기 설정 시 포함된 핵심 라이브러리는 다음과 같습니다.

Spring Web: MVC 패턴을 기반으로 웹 애플리케이션 및 RESTful API를 구축합니다.

Spring Data JPA: SQL을 직접 사용하지 않고, 객체(Entity) 중심으로 데이터베이스와 상호작용합니다.

Spring Security: 사용자의 인증(Authentication)과 인가(Authorization)를 처리하여 애플리케이션을 보호합니다.

Lombok: @Getter, @Setter, @Builder 등의 어노테이션으로 반복적인 코드를 자동 생성하여 생산성을 높입니다.

MySQL Driver: Spring 애플리케이션이 MySQL 데이터베이스와 통신할 수 있도록 연결합니다.

## 구조
DDD - 도메인 주도 개발

TDD - 테스트 주도 개발

## 도메인
1. Member (회원)
2. Gym (암장) 
3. Post (게시글)

## Memo
### 확장 가능한 부분들 List

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
