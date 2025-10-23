# Day 1 - 프로젝트 초기 설정 및 기본 CRUD 구현

**날짜**: 2025.10.21  
**목표**: Spring Boot 프로젝트 설정 및 게시판 기본 기능 구현

---

## 📋 오늘의 작업 내용

### 1. 프로젝트 초기 설정
- [x] IntelliJ IDEA Community Edition 설치
- [x] Spring Initializr로 프로젝트 생성
- [x] MySQL 데이터베이스 설치 및 설정
- [x] 의존성 추가 (Spring Web, JPA, MySQL Driver, Thymeleaf, Lombok, DevTools)

### 2. 엔티티 계층 구현
- [x] Board 엔티티 작성
- [x] JPA 어노테이션 적용
- [x] Lombok 어노테이션 활용

### 3. Repository 계층 구현
- [x] BoardRepository 인터페이스 작성
- [x] JpaRepository 상속

### 4. Service 계층 구현
- [x] BoardService 클래스 작성
- [x] CRUD 비즈니스 로직 구현
- [x] @Transactional 적용

### 5. Controller 계층 구현
- [x] BoardController 클래스 작성
- [x] URL 매핑 (7개 메서드)
- [x] Model을 통한 데이터 전달

### 6. View 계층 구현
- [x] list.html (목록)
- [x] detail.html (상세)
- [x] write.html (작성)
- [x] edit.html (수정)

### 7. 더미 데이터 삽입
- [x] data.sql 작성 (10개 게시글)
- [x] application.properties 설정

---

## 🤔 주요 질문 및 학습 내용

### Q1. JAR vs WAR 차이는?

**답변:**
- **JAR (Java ARchive)**: 독립 실행 가능, 내장 톰캣 포함, 스프링 부트 기본 방식
- **WAR (Web Application ARchive)**: 외부 톰캣 필요, 전통적인 방식

**결정**: 토이프로젝트에서는 **JAR** 사용 (간단하고 현대적)

---

### Q2. 의존성(Dependencies)은 무엇인가?

**학습 내용:**

| 의존성 | 용도 | 필수도 |
|--------|------|--------|
| Spring Web | 웹 애플리케이션 핵심, 내장 톰캣 | ⭐⭐⭐ |
| Spring Data JPA | 데이터베이스 연동, CRUD 자동 생성 | ⭐⭐⭐ |
| MySQL Driver | MySQL 연결 | ⭐⭐⭐ |
| Thymeleaf | HTML 템플릿 엔진 | ⭐⭐⭐ |
| Lombok | 코드 자동 생성 (Getter/Setter) | ⭐⭐ |
| DevTools | 자동 재시작, 개발 편의성 | ⭐⭐ |

---

### Q3. @Transactional은 무엇인가?

**학습 내용:**
- 여러 작업을 하나의 묶음(트랜잭션)으로 처리
- 모두 성공하거나, 모두 실패 (원자성 보장)
- **Dirty Checking**: 엔티티 변경 시 자동으로 DB 반영

**사용 위치:**
```java
// 사용 O
@Transactional
public Board findById(Long id) {
    Board board = boardRepository.findById(id).orElseThrow();
    board.setViewCount(board.getViewCount() + 1);  // 자동 반영!
    return board;
}

// 사용 X (Repository 메서드가 이미 처리)
public Board save(Board board) {
    return boardRepository.save(board);
}
```

**어노테이션 선택:**
- `org.springframework.transaction.annotation.Transactional` ✅ 추천
- ~~`jakarta.transaction.Transactional`~~ (기능 제한적)

---

### Q4. orElseThrow()는 무엇인가?

**학습 내용:**
- `Optional<T>` 객체를 다루는 메서드
- 값이 있으면 반환, 없으면 예외 발생

**사용 예:**
```java
Board board = boardRepository.findById(id)
    .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
```

**Optional의 다른 메서드:**
- `orElse(기본값)`: 값이 없으면 기본값 반환
- `orElseGet(() -> 생성메서드)`: 값이 없을 때만 메서드 실행
- `isPresent()`: 값 존재 여부 확인

---

### Q5. 서블릿과 스프링 MVC의 차이는?

**서블릿 방식 (기존):**
```java
protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    String action = request.getParameter("action");
    switch(action) {
        case "list":
            request.setAttribute("boards", boards);
            dispatcher.forward(request, response);
    }
}
```

**스프링 MVC 방식:**
```java
@GetMapping("/board/list")
public String list(Model model) {
    model.addAttribute("boards", boards);
    return "board/list";  // 뷰 이름
}
```

**주요 차이:**
- URL 매핑: switch 분기 → 어노테이션으로 자동 분리
- 데이터 전달: `request.setAttribute()` → `model.addAttribute()`
- 뷰 처리: `dispatcher.forward()` → `return "뷰이름"`

---

### Q6. Controller 반환 타입이 String인 이유는?

**학습 내용:**
- String = 뷰 이름 (HTML 파일 경로)
- 스프링이 ViewResolver로 자동 처리

**구분 방법:**
```java
// 1. 일반 뷰 (HTML 렌더링)
return "board/list";  // templates/board/list.html

// 2. 리다이렉트 (URL 이동)
return "redirect:/board/list";

// 3. 포워드 (내부 이동)
return "forward:/board/list";

// 4. 문자열 그대로 (@ResponseBody 필요)
@ResponseBody
return "hello";  // "hello" 문자열 표시
```

---

### Q7. Model 객체란?

**학습 내용:**
- 데이터를 뷰(HTML)로 전달하는 바구니
- 서블릿의 `HttpServletRequest.setAttribute()`와 유사하지만 더 간단

**비교:**
```java
// 서블릿
request.setAttribute("boards", boards);
dispatcher.forward(request, response);

// 스프링
model.addAttribute("boards", boards);
return "board/list";
```

---

### Q8. POST 데이터는 어떻게 받나?

**학습 내용:**

**방법 1: 객체로 자동 바인딩 (추천!)** ⭐
```java
@PostMapping("/board/write")
public String write(Board board) {
    // HTML의 name과 Board 필드명 일치하면 자동 바인딩!
    boardService.save(board);
    return "redirect:/board/list";
}
```

**방법 2: @RequestParam (개별)**
```java
@PostMapping("/board/write")
public String write(@RequestParam String title,
                    @RequestParam String content) {
    // ...
}
```

**방법 3: HttpServletRequest (비추천)**
```java
@PostMapping("/board/write")
public String write(HttpServletRequest request) {
    String title = request.getParameter("title");
    // ...
}
```

**주의사항:**
- HTML의 `name` 속성과 엔티티 필드명 일치 필수
- `@Setter` 있어야 자동 바인딩 동작

---

## 🐛 트러블슈팅

### ViewResolver 구분 방법
**질문**: 반환하는 String을 그대로 써야 할 때는?  
**학습**: 접두사로 구분
- 접두사 없음 → 뷰 이름 (HTML 렌더링)
- `redirect:` → 리다이렉트
- `forward:` → 포워드
- `@ResponseBody` → 문자열 그대로

---

## 💡 핵심 개념 정리

### 1. Spring MVC 흐름
```
브라우저 → Controller → Service → Repository → DB
                ↓
            Model에 데이터 담기
                ↓
            View 이름 반환
                ↓
        ViewResolver가 HTML 찾기
                ↓
            Thymeleaf 렌더링
                ↓
            브라우저에 응답
```

### 2. JPA Repository의 마법
```java
public interface BoardRepository extends JpaRepository<Board, Long> {
    // 메서드 없어도 CRUD 자동 생성!
    // save(), findById(), findAll(), deleteById() 등
}
```

### 3. Thymeleaf 필수 문법
```html
<!-- 네임스페이스 -->
<html xmlns:th="http://www.thymeleaf.org">

<!-- 텍스트 출력 -->
<p th:text="${board.title}"></p>

<!-- URL 생성 -->
<a th:href="@{/board/detail/{id}(id=${board.id})}">상세</a>

<!-- 반복문 -->
<tr th:each="board : ${boards}">
    <td th:text="${board.title}"></td>
</tr>

<!-- 폼 데이터 바인딩 -->
<input type="text" name="title" th:value="${board.title}" />
```

### 4. 계층별 역할
- **Entity**: 데이터 구조 정의 (DB 테이블과 매핑)
- **Repository**: 데이터 접근 (CRUD)
- **Service**: 비즈니스 로직 (트랜잭션 처리)
- **Controller**: 요청/응답 처리 (URL 매핑)
- **View**: 화면 표시 (Thymeleaf)

---

## 📊 작성한 코드 통계

- **Entity**: 1개 (Board)
- **Repository**: 1개 (BoardRepository)
- **Service**: 1개 (BoardService) - 5개 메서드
- **Controller**: 1개 (BoardController) - 7개 메서드
- **View**: 4개 (list, detail, write, edit)
- **SQL**: 10개 더미 데이터

---

## 🎯 내일 계획

### Phase 2: 페이징 및 검색 기능
- [ ] Pageable을 이용한 페이징 처리
- [ ] 검색 기능 구현 (제목, 내용, 작성자)
- [ ] 페이지네이션 UI 추가

---

## 📝 메모

### 배운 점
1. 스프링은 서블릿보다 훨씬 간결하고 강력하다
2. JPA를 사용하면 SQL 작성 없이도 CRUD 가능
3. @Transactional로 데이터 정합성 보장
4. Thymeleaf는 자연스러운 템플릿 문법 제공

### 어려웠던 점
1. @Transactional 개념 이해 (Dirty Checking)
2. Optional과 orElseThrow() 사용법
3. ViewResolver의 동작 원리
4. Model과 HttpServletRequest의 차이

### 느낀 점
- 서블릿 경험이 있어서 스프링 MVC 이해가 더 쉬웠음
- 반복적인 코드를 프레임워크가 처리해주니 생산성이 높음
- 한 번에 다 이해하려 하지 말고 일단 따라 해보는 게 중요

---

**작성일**: 2025.10.21  
**다음 학습**: [Day 2 - 페이징 및 검색 기능](day2.md)
