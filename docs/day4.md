# Day 4 - 회원가입 및 로그인 기능 구현

**날짜**: 2025.11.05  
**목표**: 세션 기반 회원가입/로그인 기능 추가 및 게시판 연동

---

## 📋 오늘의 작업 내용

### Phase 4: 회원가입 및 로그인 (세션 기반)
- [x] User 엔티티 설계 및 생성
- [x] UserRepository 작성 (Query Method)
- [x] BCrypt 의존성 추가 (비밀번호 암호화)
- [x] AppConfig 작성 (PasswordEncoder 빈 등록)
- [x] UserService 작성 (회원가입, 로그인 검증)
- [x] UserController 작성 (회원가입, 로그인, 로그아웃)
- [x] 회원가입/로그인 페이지 작성
- [x] 게시판과 회원 기능 연동
  - BoardController 수정 (로그인 체크, 권한 확인)
  - CommentController 수정 (로그인 체크, 권한 확인)
  - 작성자 자동 입력
  - 본인만 수정/삭제 가능
- [x] View 수정 (권한별 버튼 표시/숨김)

---

## 🤔 주요 질문 및 학습 내용

### Q1. 회원 기능 구현 방식 선택

**질문 배경:**
- Spring Security를 사용할지, 간단한 세션 로그인을 사용할지 선택

**답변 내용:**

**두 가지 방식:**

1. **간단한 세션 로그인** ⭐ (선택)
   - Spring Security 없이 구현
   - HttpSession으로 로그인 상태 관리
   - BCrypt로 비밀번호 암호화
   - 난이도: ★★☆☆☆

2. **Spring Security 사용**
   - 프레임워크 활용
   - 복잡한 보안 설정
   - 난이도: ★★★★☆

**결정:**
- **방식 1**로 구현 후 나중에 Spring Security로 마이그레이션 예정
- 핵심 개념(세션, 인증, 권한)을 먼저 학습

**학습 포인트:**
- 단계적 학습이 효과적
- 기본을 이해한 후 고급 기술로 전환

---

### Q2. @RequestParam 어노테이션의 기능은?

**질문 배경:**
- Controller 메서드에서 HTTP 파라미터를 받을 때 사용

**답변 내용:**

**기능:**
- HTTP 요청의 **쿼리 파라미터** 또는 **폼 데이터**를 메서드 파라미터로 바인딩

**예시 1: GET 요청 (쿼리 파라미터)**
```java
@GetMapping("/search")
public String search(@RequestParam String keyword) {
    // URL: /search?keyword=스프링
    // keyword = "스프링"
}
```

**예시 2: POST 요청 (폼 데이터)**
```java
@PostMapping("/user/login")
public String login(
    @RequestParam String username,
    @RequestParam String password) {
    // HTML form의 name="username", name="password"와 매핑
}
```

**주요 옵션:**

1. **required** (필수 여부)
```java
@RequestParam(required = false) String keyword
// 파라미터 없어도 에러 안 남 (null)

@RequestParam String keyword  // 기본값 required = true
// 파라미터 없으면 400 에러
```

2. **defaultValue** (기본값)
```java
@RequestParam(defaultValue = "1") int page
// 파라미터 없으면 page = 1
```

3. **name** (파라미터 이름 지정)
```java
@RequestParam(name = "search_keyword") String keyword
// HTML: <input name="search_keyword">
```

**생략 가능?**
```java
// ✅ 명시적 (추천)
public String login(
    @RequestParam String username,
    @RequestParam String password) {
}

// ⚠️ 생략 가능하지만 비추천
public String login(String username, String password) {
}
```

**명시하는 게 좋은 이유:**
- 가독성: 어디서 온 데이터인지 명확
- 옵션 사용 가능: required, defaultValue 설정
- 이름 불일치 시 name 속성으로 매핑

**정리:**

| 어노테이션 | 데이터 소스 | 예시 |
|-----------|------------|------|
| `@RequestParam` | URL 파라미터, 폼 데이터 | `?keyword=스프링` |
| `@PathVariable` | URL 경로 | `/board/detail/{id}` |
| `@RequestBody` | JSON 요청 본문 | REST API |

---

### Q3. PasswordEncoder 빈을 찾을 수 없다는 에러

**상황:**
```
Parameter 1 of constructor in UserService required a bean 
of type 'PasswordEncoder' that could not be found.
```

**원인:**
- UserService에서 `new BCryptPasswordEncoder()`로 직접 생성
- Spring이 관리하는 빈이 아니라서 의존성 주입 실패

**잘못된 코드:**
```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();  // ❌
}
```

**해결 방법:**

1. **Configuration 클래스 생성** (AppConfig.java)
```java
@Configuration
public class AppConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

2. **UserService 수정**
```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  // ✅ final만 선언
}
```

**학습 포인트:**
- Spring Bean으로 등록해야 의존성 주입 가능
- `@Configuration` + `@Bean`으로 빈 등록
- `@RequiredArgsConstructor`가 final 필드를 자동 주입

---

### Q4. findById 메서드를 새로 만들어야 하나?

**질문 배경:**
- 댓글 삭제 시 본인 확인을 하려면 댓글 정보를 먼저 조회해야 함

**답변:**
- 네, **CommentService에 findById 메서드 추가 필요**

**추가한 메서드:**
```java
public Comment findById(Long id) {
    return commentRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));
}
```

**사용 예:**
```java
@GetMapping("/comment/delete/{id}")
public String delete(@PathVariable Long id, ...) {
    Comment comment = commentService.findById(id);  // 댓글 조회
    
    if (!loginUser.getName().equals(comment.getWriter())) {
        throw new RuntimeException("본인만 삭제 가능");
    }
    
    commentService.delete(id);
}
```

**학습 포인트:**
- 권한 확인을 위해서는 먼저 데이터를 조회해야 함
- Service 계층에 필요한 메서드를 추가로 작성

---

### Q5. 비로그인 상태에서 글쓰기 클릭 시 로그인 페이지로 이동 안 함

**문제 상황:**
- 글쓰기 버튼 클릭 시 로그인 페이지로 이동해야 하는데 작성 페이지가 그대로 표시됨

**원인:**
- writeForm (GET 메서드)에 로그인 체크가 없었음
- POST 메서드만 체크해서 폼은 볼 수 있었음

**해결:**

**writeForm 메서드 수정:**
```java
// 기존 (❌)
@GetMapping("/board/write")
public String writeForm() {
    return "board/write";
}

// 수정 (✅)
@GetMapping("/board/write")
public String writeForm(HttpSession session) {
    User loginUser = (User) session.getAttribute("loginUser");
    
    if (loginUser == null) {
        return "redirect:/user/login";
    }
    
    return "board/write";
}
```

**editForm 메서드도 수정:**
```java
@GetMapping("/board/edit/{id}")
public String editForm(@PathVariable Long id, Model model, HttpSession session) {
    User loginUser = (User) session.getAttribute("loginUser");
    
    if (loginUser == null) {
        return "redirect:/user/login";
    }
    
    Board board = boardService.findById(id);
    
    if (!loginUser.getName().equals(board.getWriter())) {
        throw new RuntimeException("본인만 수정 가능");
    }
    
    model.addAttribute("board", board);
    return "board/edit";
}
```

**학습 포인트:**
- GET 요청(폼 표시)과 POST 요청(처리) 모두 체크 필요
- URL 직접 접근도 막아야 함

---

## 💻 작성한 코드

### 1. User 엔티티 (User.java)

```java
package com.example.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")  // user는 MySQL 예약어
@Getter @Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String username;  // 로그인 아이디

    @Column(nullable = false, length = 100)
    private String password;  // 암호화된 비밀번호

    @Column(nullable = false, length = 20)
    private String name;  // 이름

    @Column(nullable = false, length = 50)
    private String email;

    @Column(nullable = false)
    private LocalDateTime createDate;

    @PrePersist
    public void prePersist() {
        this.createDate = LocalDateTime.now();
    }
}
```

**학습 포인트:**
- `@Table(name = "users")`: user는 MySQL 예약어이므로 users 사용
- `unique = true`: username 중복 불가
- `length = 100`: 암호화된 비밀번호는 길이가 김

---

### 2. UserRepository (UserRepository.java)

```java
package com.example.board.repository;

import com.example.board.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // username으로 User 찾기 (로그인 시 사용)
    Optional<User> findByUsername(String username);
    
    // username 존재 여부 확인 (중복 체크)
    boolean existsByUsername(String username);
}
```

**Query Method 의미:**
- `findByUsername`: `SELECT * FROM users WHERE username = ?`
- `existsByUsername`: `SELECT COUNT(*) > 0 FROM users WHERE username = ?`

---

### 3. AppConfig (AppConfig.java)

```java
package com.example.board.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**학습 포인트:**
- `@Configuration`: 설정 클래스 표시
- `@Bean`: Spring 컨테이너에 빈 등록
- PasswordEncoder를 빈으로 등록해야 의존성 주입 가능

---

### 4. UserService (UserService.java)

```java
package com.example.board.service;

import com.example.board.entity.User;
import com.example.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    // 회원가입
    public User register(User user) {
        // 1. 중복 체크
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }
        
        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        
        // 3. 저장
        return userRepository.save(user);
    }
    
    // 로그인 검증
    public User login(String username, String password) {
        // username으로 User 조회
        User user = userRepository.findByUsername(username).orElse(null);
        
        // User가 없거나 비밀번호가 틀리면 null 반환
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }
        
        return user;
    }
}
```

**BCrypt 사용법:**
- `encode(평문)`: 비밀번호 암호화
- `matches(평문, 암호화된비밀번호)`: 비밀번호 일치 확인

**암호화 예시:**
```
평문: "1234"
암호화: "$2a$10$eImiTXuWVxfM37uY4JANjQ.LKfV5P8zLfJ8wN8aF5Vw6vQ3fZ6J8m"
```

**특징:**
- 같은 평문이라도 매번 다른 암호화 결과 (Salt 사용)
- 복호화 불가능 (단방향 해시)
- matches()로만 검증 가능

---

### 5. UserController (UserController.java)

```java
package com.example.board.controller;

import com.example.board.entity.User;
import com.example.board.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    
    // 회원가입 폼
    @GetMapping("/user/register")
    public String registerForm() {
        return "user/register";
    }
    
    // 회원가입 처리
    @PostMapping("/user/register")
    public String register(User user) {
        try {
            userService.register(user);
            return "redirect:/user/login";
        } catch (RuntimeException e) {
            return "redirect:/user/register";
        }
    }
    
    // 로그인 폼
    @GetMapping("/user/login")
    public String loginForm() {
        return "user/login";
    }
    
    // 로그인 처리
    @PostMapping("/user/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session) {
        
        User user = userService.login(username, password);
        
        if (user == null) {
            return "redirect:/user/login";
        }
        
        session.setAttribute("loginUser", user);
        return "redirect:/board/list";
    }
    
    // 로그아웃
    @GetMapping("/user/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/user/login";
    }
}
```

**HttpSession 사용법:**
- `setAttribute("키", 값)`: 세션에 저장
- `getAttribute("키")`: 세션에서 조회
- `invalidate()`: 세션 무효화 (로그아웃)

**세션 생명주기:**
```
로그인 → session.setAttribute("loginUser", user)
↓
세션 유지 (브라우저 닫을 때까지 또는 타임아웃까지)
↓
로그아웃 → session.invalidate()
```

---

### 6. BoardController 수정 (권한 체크)

**게시글 작성 폼:**
```java
@GetMapping("/board/write")
public String writeForm(HttpSession session) {
    User loginUser = (User) session.getAttribute("loginUser");
    
    if (loginUser == null) {
        return "redirect:/user/login";
    }
    
    return "board/write";
}
```

**게시글 작성 처리:**
```java
@PostMapping("/board/write")
public String write(Board board, HttpSession session) {
    User loginUser = (User) session.getAttribute("loginUser");
    
    if (loginUser == null) {
        return "redirect:/user/login";
    }
    
    board.setWriter(loginUser.getName());
    boardService.save(board);
    return "redirect:/board/list";
}
```

**게시글 수정 폼:**
```java
@GetMapping("/board/edit/{id}")
public String editForm(@PathVariable Long id, Model model, HttpSession session) {
    User loginUser = (User) session.getAttribute("loginUser");
    
    if (loginUser == null) {
        return "redirect:/user/login";
    }
    
    Board board = boardService.findById(id);
    
    if (!loginUser.getName().equals(board.getWriter())) {
        throw new RuntimeException("본인이 작성한 글만 수정할 수 있습니다.");
    }
    
    model.addAttribute("board", board);
    return "board/edit";
}
```

**게시글 수정 처리:**
```java
@PostMapping("/board/edit/{id}")
public String edit(@PathVariable Long id, Board board, HttpSession session) {
    User loginUser = (User) session.getAttribute("loginUser");
    
    if (loginUser == null) {
        return "redirect:/user/login";
    }
    
    Board existingBoard = boardService.findById(id);
    
    if (!loginUser.getName().equals(existingBoard.getWriter())) {
        throw new RuntimeException("본인이 작성한 글만 수정할 수 있습니다.");
    }
    
    boardService.update(id, board);
    return "redirect:/board/detail/" + id;
}
```

**게시글 삭제:**
```java
@GetMapping("/board/delete/{id}")
public String delete(@PathVariable Long id, HttpSession session) {
    User loginUser = (User) session.getAttribute("loginUser");
    
    if (loginUser == null) {
        return "redirect:/user/login";
    }
    
    Board board = boardService.findById(id);
    
    if (!loginUser.getName().equals(board.getWriter())) {
        throw new RuntimeException("본인이 작성한 글만 삭제할 수 있습니다.");
    }
    
    boardService.delete(id);
    return "redirect:/board/list";
}
```

---

### 7. CommentController 수정 (권한 체크)

**CommentService에 findById 추가:**
```java
public Comment findById(Long id) {
    return commentRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));
}
```

**댓글 작성:**
```java
@PostMapping("/comment/write")
public String write(
        @RequestParam Long boardId,
        @RequestParam String content,
        HttpSession session) {
    
    User loginUser = (User) session.getAttribute("loginUser");
    
    if (loginUser == null) {
        return "redirect:/user/login";
    }
    
    commentService.save(boardId, loginUser.getName(), content);
    return "redirect:/board/detail/" + boardId;
}
```

**댓글 삭제:**
```java
@GetMapping("/comment/delete/{id}")
public String delete(
        @PathVariable Long id,
        @RequestParam Long boardId,
        HttpSession session) {
    
    User loginUser = (User) session.getAttribute("loginUser");
    
    if (loginUser == null) {
        return "redirect:/user/login";
    }
    
    Comment comment = commentService.findById(id);
    
    if (!loginUser.getName().equals(comment.getWriter())) {
        throw new RuntimeException("본인이 작성한 댓글만 삭제할 수 있습니다.");
    }
    
    commentService.delete(id);
    return "redirect:/board/detail/" + boardId;
}
```

---

### 8. View 수정 (권한별 버튼 표시)

**list.html - 로그인 정보 표시:**
```html
<div style="text-align: right; padding: 10px; border-bottom: 1px solid #ddd;">
    <span th:if="${session.loginUser != null}">
        <strong th:text="${session.loginUser.name}">사용자</strong>님 환영합니다! 
        <a th:href="@{/user/logout}">
            <button type="button">로그아웃</button>
        </a>
    </span>
    <span th:unless="${session.loginUser != null}">
        <a th:href="@{/user/login}">
            <button type="button">로그인</button>
        </a>
        <a th:href="@{/user/register}">
            <button type="button">회원가입</button>
        </a>
    </span>
</div>
```

**detail.html - 게시글 수정/삭제 버튼 (본인만):**
```html
<!-- 본인만 수정 버튼 보임 -->
<a th:if="${session.loginUser != null && session.loginUser.name == board.writer}"
   th:href="@{/board/edit/{id}(id=${board.id})}">
    <button>수정</button>
</a>

<!-- 본인만 삭제 버튼 보임 -->
<a th:if="${session.loginUser != null && session.loginUser.name == board.writer}"
   th:href="@{/board/delete/{id}(id=${board.id})}"
   onclick="return confirm('정말 삭제하시겠습니까?')">
    <button>삭제</button>
</a>
```

**detail.html - 댓글 삭제 버튼 (본인만):**
```html
<div th:each="comment : ${comments}">
    <div>
        <strong th:text="${comment.writer}">작성자</strong>
        <span th:text="${#temporals.format(comment.createdDate, 'yyyy-MM-dd HH:mm')}"></span>
        
        <!-- 본인만 댓글 삭제 버튼 보임 -->
        <a th:if="${session.loginUser != null && session.loginUser.name == comment.writer}"
           th:href="@{/comment/delete/{id}(id=${comment.id}, boardId=${board.id})}"
           onclick="return confirm('댓글을 삭제하시겠습니까?')">
            <button type="button">삭제</button>
        </a>
    </div>
    <p th:text="${comment.content}"></p>
</div>
```

**detail.html - 댓글 작성 폼 (로그인 시):**
```html
<!-- 로그인한 사용자만 댓글 작성 가능 -->
<div th:if="${session.loginUser != null}">
    <form th:action="@{/comment/write}" method="post">
        <input type="hidden" name="boardId" th:value="${board.id}">
        <!-- 작성자 입력란 제거됨 -->
        <div>
            <label>댓글 내용:</label>
            <textarea name="content" rows="3" required></textarea>
        </div>
        <button type="submit">댓글 등록</button>
    </form>
</div>
<div th:unless="${session.loginUser != null}">
    <p>댓글을 작성하려면 <a th:href="@{/user/login}">로그인</a>이 필요합니다.</p>
</div>
```

**write.html - 작성자 입력란 제거:**
```html
<!-- ❌ 삭제된 부분 -->
<tr>
    <th>작성자</th>
    <td>
        <input type="text" name="writer" required>
    </td>
</tr>
```

---

## 📊 데이터베이스 구조

### users 테이블
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(20) NOT NULL,
    email VARCHAR(50) NOT NULL,
    create_date DATETIME NOT NULL
);
```

**특징:**
- `username`: UNIQUE 제약조건 (중복 불가)
- `password`: 암호화된 비밀번호 저장 (100자)

**샘플 데이터:**
```
id | username | password                                          | name   | email
1  | hong     | $2a$10$eImiTXuWVxfM37uY4JANjQ.LKfV5P8zLfJ8wN8aF... | 홍길동 | hong@test.com
2  | kim      | $2a$10$dFwqE7fA3kL9mN2pQ5rS6uT.8Vz9XbC1yD4eF6... | 김철수 | kim@test.com
```

---

### ERD 업데이트
```
┌─────────────┐         ┌──────────────┐         ┌──────────────┐
│   User      │         │   Board      │ 1     N │   Comment    │
├─────────────┤         ├──────────────┤◄────────┤──────────────┤
│ id (PK)     │         │ id (PK)      │         │ id (PK)      │
│ username(UK)│         │ title        │         │ content      │
│ password    │         │ content      │         │ writer       │
│ name        │──┐      │ writer       │         │ created_date │
│ email       │  │      │ ...          │         │ board_id(FK) │
└─────────────┘  │      └──────────────┘         └──────────────┘
                 │             │
                 └─────────────┘
                (writer는 User.name을 참조하지만 FK는 아님)
```

**관계:**
- User는 여러 Board/Comment를 작성 가능 (1:N)
- writer 필드는 User.name을 저장 (FK 아님, 단순 문자열)

---

## 🎓 핵심 개념 정리

### 1. 세션 기반 인증

**세션이란?**
- 서버에 저장되는 사용자별 임시 저장소
- 브라우저와 서버 간 상태 유지

**동작 원리:**
```
1. 로그인 성공
   ↓
2. session.setAttribute("loginUser", user)
   - 서버 메모리에 User 객체 저장
   - 세션 ID 생성 (예: JSESSIONID=ABC123)
   ↓
3. 응답 시 쿠키로 세션 ID 전송
   Set-Cookie: JSESSIONID=ABC123
   ↓
4. 이후 요청 시 쿠키 자동 전송
   Cookie: JSESSIONID=ABC123
   ↓
5. 서버가 세션 ID로 User 객체 조회
   User loginUser = session.getAttribute("loginUser")
```

**세션 vs 쿠키:**

| 구분 | 세션 | 쿠키 |
|-----|------|------|
| 저장 위치 | 서버 | 클라이언트(브라우저) |
| 보안 | 높음 | 낮음 |
| 용량 제한 | 없음 (서버 메모리) | 4KB |
| 만료 시점 | 설정 가능 | 설정 가능 |

---

### 2. 비밀번호 암호화 (BCrypt)

**BCrypt란?**
- Blowfish 암호 기반의 단방향 해시 함수
- Salt 자동 생성으로 같은 비밀번호도 다른 해시 생성

**특징:**
```
평문: "1234"

암호화 (매번 다름):
$2a$10$eImiTXuWVxfM37uY4JANjQ.LKfV5P8zLfJ8wN8aF5Vw6vQ3fZ6J8m
$2a$10$dFwqE7fA3kL9mN2pQ5rS6uT.8Vz9XbC1yD4eF6gH8iJ0kL2mN4oP6q

복호화: 불가능 ✅ (단방향)
검증: matches() 메서드 사용
```

**구조:**
```
$2a$10$eImiTXuWVxfM37uY4JANjQ.LKfV5P8zLfJ8wN8aF5Vw6vQ3fZ6J8m
 │   │  │                      └─ 해시 (31자)
 │   │  └─ Salt (22자)
 │   └─ Cost Factor (반복 횟수 = 2^10)
 └─ 알고리즘 버전
```

**사용법:**
```java
// 암호화
String encoded = passwordEncoder.encode("1234");
// "$2a$10$..."

// 검증
boolean matches = passwordEncoder.matches("1234", encoded);
// true

boolean matches = passwordEncoder.matches("wrong", encoded);
// false
```

---

### 3. 인증(Authentication)과 인가(Authorization)

**인증 (Authentication):**
- "당신은 누구인가?" → 신원 확인
- 예: 로그인 (아이디/비밀번호 확인)

**인가 (Authorization):**
- "무엇을 할 수 있는가?" → 권한 확인
- 예: 본인만 글 수정/삭제 가능

**구현 예:**
```java
// 인증 체크
if (loginUser == null) {
    return "redirect:/user/login";  // 로그인 필요
}

// 인가 체크
if (!loginUser.getName().equals(board.getWriter())) {
    throw new RuntimeException("권한 없음");  // 본인만 가능
}
```

---

### 4. Controller 레이어 패턴

**공통 패턴:**
```java
public String action(파라미터들, HttpSession session) {
    // 1. 인증 체크
    User loginUser = (User) session.getAttribute("loginUser");
    if (loginUser == null) {
        return "redirect:/user/login";
    }
    
    // 2. 비즈니스 로직 실행
    Data data = service.getData();
    
    // 3. 인가 체크 (필요 시)
    if (!loginUser.hasPermission(data)) {
        throw new RuntimeException("권한 없음");
    }
    
    // 4. 처리
    service.process(data);
    
    // 5. 리다이렉트 또는 뷰 반환
    return "redirect:/success";
}
```

---

### 5. Thymeleaf 세션 접근

**세션 데이터 접근:**
```html
<!-- 세션에서 loginUser 가져오기 -->
${session.loginUser}

<!-- loginUser의 name 속성 -->
${session.loginUser.name}

<!-- null 체크 -->
th:if="${session.loginUser != null}"
th:unless="${session.loginUser != null}"
```

**조건부 렌더링:**
```html
<!-- 로그인 시만 표시 -->
<div th:if="${session.loginUser != null}">
    로그인된 사용자만 볼 수 있음
</div>

<!-- 비로그인 시만 표시 -->
<div th:unless="${session.loginUser != null}">
    로그인이 필요합니다
</div>

<!-- 본인만 버튼 표시 -->
<button th:if="${session.loginUser != null && 
                 session.loginUser.name == board.writer}">
    수정
</button>
```

---

## 🔍 동작 흐름

### 회원가입 흐름
```
1. 사용자가 회원가입 폼 작성
   - username, password, name, email 입력
   ↓
2. POST /user/register
   ↓
3. UserController.register()
   - User 객체에 폼 데이터 자동 바인딩
   ↓
4. UserService.register()
   - username 중복 체크
   - 비밀번호 암호화
   - DB 저장
   ↓
5. redirect:/user/login
   ↓
6. 로그인 페이지로 이동
```

---

### 로그인 흐름
```
1. 사용자가 로그인 폼 작성
   - username, password 입력
   ↓
2. POST /user/login
   ↓
3. UserController.login()
   - @RequestParam으로 username, password 받음
   ↓
4. UserService.login()
   - DB에서 username으로 User 조회
   - 비밀번호 일치 확인 (BCrypt matches)
   - 성공: User 반환, 실패: null 반환
   ↓
5. 성공 시:
   - session.setAttribute("loginUser", user)
   - redirect:/board/list
   
   실패 시:
   - redirect:/user/login
```

---

### 게시글 작성 흐름 (로그인 연동)
```
1. 사용자가 [글쓰기] 클릭
   ↓
2. GET /board/write
   ↓
3. BoardController.writeForm()
   - 세션에서 loginUser 확인
   - 비로그인 시: redirect:/user/login
   - 로그인 시: board/write.html 표시
   ↓
4. 사용자가 제목, 내용 입력 후 등록
   (작성자 입력란 없음)
   ↓
5. POST /board/write
   ↓
6. BoardController.write()
   - 세션에서 loginUser 확인
   - board.setWriter(loginUser.getName())
   - boardService.save(board)
   ↓
7. redirect:/board/list
```

---

### 권한 체크 흐름 (수정/삭제)
```
1. 사용자가 [수정] 또는 [삭제] 클릭
   ↓
2. Controller 메서드 실행
   ↓
3. 인증 체크
   - session.getAttribute("loginUser")
   - null이면 로그인 페이지로
   ↓
4. 데이터 조회
   - Board board = boardService.findById(id)
   ↓
5. 인가 체크
   - loginUser.getName().equals(board.getWriter())
   - 다르면 예외 발생
   ↓
6. 처리 진행
   - 수정 또는 삭제
```

---

## 🐛 트러블슈팅

### 문제 1: PasswordEncoder 빈을 찾을 수 없음
**증상**: `Parameter 1 of constructor in UserService required a bean...`

**원인**: UserService에서 직접 `new BCryptPasswordEncoder()` 생성

**해결**:
1. AppConfig 클래스 생성
2. @Bean으로 PasswordEncoder 등록
3. UserService에서 final로 주입받기

---

### 문제 2: 비로그인 상태에서 글쓰기 폼이 보임
**증상**: 글쓰기 버튼 클릭 시 작성 페이지가 표시됨

**원인**: writeForm (GET) 메서드에 로그인 체크 없음

**해결**: GET 메서드에도 세션 체크 추가
```java
@GetMapping("/board/write")
public String writeForm(HttpSession session) {
    if (session.getAttribute("loginUser") == null) {
        return "redirect:/user/login";
    }
    return "board/write";
}
```

---

### 문제 3: 다른 사람 글도 수정/삭제 버튼이 보임
**증상**: 권한이 없는데도 버튼이 표시됨

**원인**: View에서 권한 체크 안 함

**해결**: Thymeleaf 조건문 추가
```html
<button th:if="${session.loginUser != null && 
                 session.loginUser.name == board.writer}">
    수정
</button>
```

---

### 문제 4: 세션이 유지되지 않음
**증상**: 로그인 후 다시 로그인 페이지로 이동

**원인**: 
1. session.setAttribute() 누락
2. 쿠키 차단됨

**해결**:
1. setAttribute 확인
2. 브라우저 쿠키 설정 확인
3. 시크릿 모드 해제

---

## 💡 배운 것과 느낀 점

### 기술적 학습
1. **세션 기반 인증**: HttpSession으로 로그인 상태 관리
2. **비밀번호 암호화**: BCrypt를 이용한 안전한 비밀번호 저장
3. **인증과 인가**: 로그인 체크와 권한 체크의 차이
4. **의존성 주입**: @Bean으로 빈 등록, @RequiredArgsConstructor로 주입
5. **Thymeleaf 세션**: ${session.loginUser}로 세션 데이터 접근
6. **권한별 UI**: 조건부 렌더링으로 버튼 표시/숨김

### 개발 패턴
1. **Controller 패턴**: 인증 체크 → 로직 실행 → 인가 체크
2. **Service 계층**: 비즈니스 로직 분리 (암호화, 검증)
3. **보안 강화**: GET/POST 모두 권한 체크
4. **사용자 경험**: 권한 없으면 버튼 숨김

### 문제 해결 능력
1. PasswordEncoder 빈 등록 문제 해결
2. 세션 체크 누락 발견 및 수정
3. 권한 체크를 GET/POST 모두에 적용

### 어려웠던 점
1. 빈 등록 개념 이해 (@Configuration, @Bean)
2. 세션의 생명주기와 작동 방식
3. Controller의 모든 메서드에 일일이 체크하는 것 (반복 코드)

### 느낀 점
- 인증/인가는 모든 웹 애플리케이션의 기본
- 보안은 빈틈없이 체크해야 함 (GET/POST 모두)
- Spring Security가 왜 필요한지 이해됨 (반복 코드 제거)
- 세션 기반 인증을 먼저 이해하니 개념이 명확해짐

---

## 🎯 다음 계획

### Phase 5: Spring Security로 마이그레이션 (예정)
- [ ] Spring Security 의존성 추가
- [ ] SecurityConfig 설정
- [ ] UserDetailsService 구현
- [ ] 로그인/로그아웃 설정
- [ ] 권한 체크 자동화 (@PreAuthorize)
- [ ] CSRF 보호

### Phase 6: 대댓글 기능 (보류)
- [ ] Comment 엔티티에 자기 참조 추가
- [ ] parent/child 관계 설정
- [ ] 대댓글 UI

### 추가 고려 사항
- [ ] 이메일 인증
- [ ] 비밀번호 찾기
- [ ] 프로필 수정
- [ ] 관리자 권한

---

## 📝 메모

### 핵심 개념 복습
1. **세션**: 서버에 저장되는 사용자별 임시 저장소
2. **BCrypt**: Salt를 이용한 안전한 단방향 암호화
3. **인증**: 신원 확인 (로그인)
4. **인가**: 권한 확인 (본인만 수정/삭제)
5. **@Bean**: Spring 컨테이너에 객체 등록

### 실무 팁
- GET/POST 모두 권한 체크 필수
- 비밀번호는 반드시 암호화
- 세션 타임아웃 설정 고려
- 중요 작업은 재인증 요구
- 에러 메시지는 구체적으로 (보안상 주의)

### Spring Security와 비교
**현재 방식 (수동):**
- 장점: 간단, 개념 이해 쉬움
- 단점: 반복 코드 많음, 빈틈 가능성

**Spring Security:**
- 장점: 자동화, 강력한 보안, 표준
- 단점: 초기 설정 복잡, 학습 곡선

**결론**: 기본을 이해했으니 다음은 Spring Security!

---

**작성일**: 2025.11.05  
**이전 학습**: [Day 3 - 댓글 기능 구현](day3.md)  
**다음 학습**: [Day 5 - Spring Security 마이그레이션](day5.md) (예정)