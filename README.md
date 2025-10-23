# 스프링 게시판 프로젝트

Spring Boot를 활용한 CRUD 게시판 토이 프로젝트

## 📌 프로젝트 정보

- **프로젝트명**: Spring Board (스프링 게시판)
- **목적**: Spring Boot 학습 및 기본 CRUD 구현
- **개발 기간**: 2025.10.21 ~
- **개발 환경**:
  - IDE: IntelliJ IDEA Community Edition
  - JDK: 17 (또는 21)
  - Spring Boot: 3.x.x
  - Build Tool: Maven

## 🛠️ 기술 스택

### Backend
- **Spring Boot** - 웹 애플리케이션 프레임워크
- **Spring Data JPA** - 데이터베이스 연동 및 ORM
- **Spring MVC** - 웹 MVC 패턴 구현
- **Lombok** - 보일러플레이트 코드 감소

### Database
- **MySQL 8.x** - 관계형 데이터베이스
- **Hibernate** - JPA 구현체

### View
- **Thymeleaf** - 서버 사이드 템플릿 엔진

## 📁 프로젝트 구조
```
src/main/java/com/example/board/
├── entity/
│   └── Board.java              # 게시글 엔티티
├── repository/
│   └── BoardRepository.java    # 데이터 접근 계층
├── service/
│   └── BoardService.java       # 비즈니스 로직
├── controller/
│   └── BoardController.java    # 웹 요청 처리
└── BoardApplication.java       # 메인 애플리케이션

src/main/resources/
├── templates/board/
│   ├── list.html              # 목록 페이지
│   ├── detail.html            # 상세 페이지
│   ├── write.html             # 작성 페이지
│   └── edit.html              # 수정 페이지
├── application.properties     # 설정 파일
└── data.sql                   # 더미 데이터
```

## 📊 ERD (Entity Relationship Diagram)
```
┌──────────────────┐
│      BOARD       │
├──────────────────┤
│ PK  id           │ BIGINT (auto_increment)
│     title        │ VARCHAR(200) NOT NULL
│     content      │ TEXT NOT NULL
│     writer       │ VARCHAR(50) NOT NULL
│     created_date │ DATETIME NOT NULL
│     modified_date│ DATETIME
│     view_count   │ INT NOT NULL
└──────────────────┘
```

## ⚙️ 데이터베이스 설정

### MySQL 데이터베이스 생성
```sql
CREATE DATABASE board CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### application.properties
```properties
# MySQL 연결 설정
spring.datasource.url=jdbc:mysql://localhost:3306/board?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=MY-ID
spring.datasource.password=MY-PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA 설정
spring.jpa.hibernate.ddl-auto=create
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# 로그 레벨
logging.level.org.hibernate.SQL=DEBUG

# data.sql 실행 설정
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true
```

## ✨ 구현 기능

### Phase 1: 기본 CRUD ✅
- [x] 게시글 목록 조회
- [x] 게시글 상세 조회 (조회수 증가 포함)
- [x] 게시글 작성
- [x] 게시글 수정
- [x] 게시글 삭제

### Phase 2: 추가 기능 (예정)
- [ ] 페이징 처리
- [ ] 검색 기능
- [ ] 댓글 기능

### Phase 3: 회원 기능 (예정)
- [ ] 회원가입/로그인
- [ ] Spring Security 적용
- [ ] 권한 관리

## 📋 URL 매핑

| 기능 | Method | URL | 설명 |
|------|--------|-----|------|
| 목록 조회 | GET | `/board/list` | 전체 게시글 목록 |
| 상세 조회 | GET | `/board/detail/{id}` | 특정 게시글 상세 |
| 작성 폼 | GET | `/board/write` | 작성 페이지 |
| 작성 처리 | POST | `/board/write` | 게시글 저장 |
| 수정 폼 | GET | `/board/edit/{id}` | 수정 페이지 |
| 수정 처리 | POST | `/board/edit/{id}` | 게시글 수정 |
| 삭제 | GET | `/board/delete/{id}` | 게시글 삭제 |

## 📚 학습 일지

- **[Day 1 (2025.10.21)](docs/day1.md)** - 프로젝트 초기 설정 및 기본 CRUD 구현
  - 프로젝트 설정 및 MySQL 연동
  - JPA 엔티티, Repository, Service, Controller 구현
  - Thymeleaf 뷰 작성
  - 주요 질문: @Transactional, Model 객체, ViewResolver, POST 데이터 처리

## 🎯 다음 계획 (Phase 2)

### 추가 기능 구현 예정
- [ ] 페이징 처리 (Pageable)
- [ ] 검색 기능 (제목, 내용, 작성자)
- [ ] 댓글 기능 (Comment 엔티티)
- [ ] UI/UX 개선 (Bootstrap/Tailwind CSS)
- [ ] 파일 첨부 기능

## 📖 참고 자료

- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Spring Data JPA 문서](https://spring.io/projects/spring-data-jpa)
- [Thymeleaf 공식 문서](https://www.thymeleaf.org/documentation.html)

## 👨‍💻 개발자

- 개인 학습 프로젝트

---

**Last Updated**: 2025.10.21
