# Day 3 - 댓글 기능 구현

**날짜**: 2025.10.30  
**목표**: 게시판에 댓글 기능 추가

---

## 📋 오늘의 작업 내용

### Phase 3: 댓글 기능
- [x] Comment 엔티티 설계 및 생성
- [x] Board-Comment 양방향 연관관계 매핑
- [x] CommentRepository 작성 (Query Method)
- [x] CommentService 작성 (댓글 CRUD)
- [x] CommentController 작성 (댓글 작성/삭제)
- [x] detail.html에 댓글 UI 추가
- [x] 댓글 기능 테스트 및 동작 확인

---

## 🤔 주요 질문 및 학습 내용

### Q1. 대댓글을 지금 구현하기 힘든가?

**질문 배경:**
- 댓글 기능을 구현하면서 대댓글(답글) 기능도 함께 구현할지 고민

**답변 내용:**

**대댓글 구현 가능 여부:**
- ✅ 지금 구현 가능합니다!

**두 가지 방식:**

1. **단순 대댓글 (1단계만)**
   - 구조: 일반 댓글 → 대댓글 (1단계만)
   - 난이도: ★★☆☆☆ (중하)
   - 학습 포인트: 자기 참조 관계
   - 구현 방법: Comment 엔티티에 `parentId` 또는 `parent` 필드 추가

2. **무제한 계층 대댓글**
   - 구조: 댓글 → 대댓글 → 대대댓글...
   - 난이도: ★★★★☆ (상)
   - 단점: 복잡한 재귀 쿼리, 성능 이슈

**결정:**
- 일단 **일반 댓글만** 먼저 구현
- 대댓글은 **나중에 마지막에 추가** (단계적 학습)

**학습 포인트:**
- 기능을 단계적으로 나누어 구현하는 것이 학습에 효과적
- 기본 기능을 완성하고 테스트한 후 확장하는 것이 안전

---

### Q2. import 오류 - @Id 어노테이션이 잘못됨

**상황:**
```java
import org.springframework.data.annotation.Id;  // ❌ 잘못된 import
```

**문제:**
- Spring Data의 `@Id`를 import함
- JPA의 `@Id`를 사용해야 함

**해결:**
```java
import jakarta.persistence.Id;  // ✅ 올바른 import
// 또는
import jakarta.persistence.*;   // 여기에 Id가 포함됨
```

**학습 포인트:**
- JPA 엔티티에서는 `jakarta.persistence` 패키지의 어노테이션 사용
- Spring Data의 어노테이션과 혼동하지 않기
- IntelliJ의 자동 import 기능 사용 시 주의

---

### Q3. public 생략해도 사용 가능한거 아니야 원래?

**질문 배경:**
- CommentService의 메서드에 public을 붙이지 않았더니 CommentController에서 호출이 안 됨

**초기 생각:**
- Java에서 public을 생략하면 default(package-private) 접근 제어자
- 같은 패키지 계층이면 접근 가능할 것 같았음

**실제 상황:**
```
com.example.board.service      ← CommentService 위치
com.example.board.controller   ← CommentController 위치
```

**답변:**
- 이 둘은 **다른 패키지**입니다!
- `com.example.board.service`와 `com.example.board.controller`는 별개의 패키지
- 하위 패키지라고 해도 다른 패키지로 취급됨

**Java 접근 제어자 정리:**

| 접근 제어자 | 같은 클래스 | 같은 패키지 | 다른 패키지 (자식 클래스) | 다른 패키지 |
|------------|------------|------------|------------------------|------------|
| public     | ✅         | ✅         | ✅                     | ✅         |
| protected  | ✅         | ✅         | ✅                     | ❌         |
| default    | ✅         | ✅         | ❌                     | ❌         |
| private    | ✅         | ❌         | ❌                     | ❌         |

**결론:**
- Service 메서드는 **반드시 public**이어야 다른 패키지의 Controller에서 호출 가능
- Spring에서 Service 메서드는 항상 public으로 작성하는 것이 관례

**올바른 코드:**
```java
public List<Comment> findByBoardId(Long boardId) { ... }
public Comment save(...) { ... }
public void delete(Long commentId) { ... }
```

---

### Q4. redirect URL에서 변수 치환이 안 되는 이유?

**상황:**
```java
return "redirect:/board/detail/{boardId}";  // ❌ 작동 안 함
```

**문제:**
- `{boardId}`가 PathVariable 문법처럼 보이지만, redirect에서는 자동 치환되지 않음
- 실제로는 `http://localhost:8080/board/detail/{boardId}` 이런 URL로 이동 시도

**해결 방법:**

1. **문자열 연결 (추천)**
```java
return "redirect:/board/detail/" + boardId;  // ✅
```

2. **RedirectAttributes 사용 (고급)**
```java
public String write(..., RedirectAttributes redirectAttributes) {
    redirectAttributes.addAttribute("id", boardId);
    return "redirect:/board/detail/{id}";
}
```

**학습 포인트:**
- redirect URL은 단순 문자열로 처리됨
- 변수를 포함하려면 문자열 연결(+) 사용
- PathVariable 문법은 @GetMapping, @PostMapping 등의 URL 매핑에서만 사용

---

## 💻 작성한 코드

### 1. Comment 엔티티 (Comment.java)

```java
package com.example.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 50)
    private String writer;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
    }
}
```

**학습 포인트:**
- `@ManyToOne`: Comment → Board (다대일 관계)
- `@JoinColumn(name = "board_id")`: 외래키 컬럼명 지정
- `fetch = FetchType.LAZY`: 지연 로딩 (필요할 때만 Board 조회)
- `@PrePersist`: 엔티티가 저장되기 전 자동 실행
- `columnDefinition = "TEXT"`: 긴 댓글을 위한 TEXT 타입

---

### 2. Board 엔티티 수정 (Board.java)

**추가된 필드:**
```java
@OneToMany(mappedBy = "board", 
           cascade = CascadeType.REMOVE, 
           orphanRemoval = true,
           fetch = FetchType.LAZY)
private List<Comment> comments = new ArrayList<>();
```

**학습 포인트:**
- `@OneToMany`: Board → Comment (일대다 관계)
- `mappedBy = "board"`: Comment의 board 필드와 매핑 (양방향 관계의 주인이 아님)
- `cascade = CascadeType.REMOVE`: Board 삭제 시 연관된 Comment도 삭제
- `orphanRemoval = true`: 고아 객체(연관관계가 끊긴 객체) 자동 삭제
- `new ArrayList<>()`: NullPointerException 방지를 위한 초기화

**양방향 연관관계:**
```
Board (1) ←─────→ (N) Comment
         ↑ 주인      ↓ 
      mappedBy    @JoinColumn
```

**연관관계의 주인:**
- 외래키를 가진 쪽(Comment)이 연관관계의 주인
- 주인만 외래키를 관리(등록, 수정, 삭제)
- 주인이 아닌 쪽(Board)은 읽기만 가능

---

### 3. CommentRepository (CommentRepository.java)

```java
package com.example.board.repository;

import com.example.board.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByBoardIdOrderByCreatedDateAsc(Long boardId);
}
```

**학습 포인트:**
- Query Method로 특정 게시글의 댓글 조회
- `findByBoardId`: board_id로 검색
- `OrderByCreatedDateAsc`: 작성일 오름차순 정렬 (오래된 댓글이 위로)

**자동 생성되는 SQL:**
```sql
SELECT * FROM comment 
WHERE board_id = ? 
ORDER BY created_date ASC;
```

---

### 4. CommentService (CommentService.java)

```java
package com.example.board.service;

import com.example.board.entity.Board;
import com.example.board.entity.Comment;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    
    // 댓글 목록 조회
    public List<Comment> findByBoardId(Long boardId) {
        return commentRepository.findByBoardIdOrderByCreatedDateAsc(boardId);
    }
    
    // 댓글 저장
    public Comment save(Long boardId, String writer, String content) {
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        
        Comment comment = new Comment();
        comment.setWriter(writer);
        comment.setContent(content);
        comment.setBoard(board);  // 연관관계 설정 (중요!)
        
        return commentRepository.save(comment);
    }
    
    // 댓글 삭제
    public void delete(Long commentId) {
        commentRepository.deleteById(commentId);
    }
}
```

**학습 포인트:**
- `comment.setBoard(board)`: 연관관계 설정이 외래키를 자동으로 설정해줌
- BoardRepository도 주입받아서 Board 조회
- 댓글 저장 시 Board 객체를 먼저 조회해야 함

**중요: 연관관계 설정**
```java
comment.setBoard(board);  // 이것이 board_id를 설정해줌
```

이렇게 하면 JPA가 자동으로:
```sql
INSERT INTO comment (content, writer, created_date, board_id) 
VALUES (?, ?, ?, ?);
```

---

### 5. CommentController (CommentController.java)

```java
package com.example.board.controller;

import com.example.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    // 댓글 작성
    @PostMapping("/comment/write")
    public String write(
            @RequestParam Long boardId,
            @RequestParam String writer,
            @RequestParam String content) {
        commentService.save(boardId, writer, content);
        
        return "redirect:/board/detail/" + boardId;
    }
    
    // 댓글 삭제
    @GetMapping("/comment/delete/{id}")
    public String delete(
            @PathVariable Long id,
            @RequestParam Long boardId) {
        commentService.delete(id);
        
        return "redirect:/board/detail/" + boardId;
    }
}
```

**학습 포인트:**
- 댓글 작성: POST 방식 (데이터 변경)
- 댓글 삭제: GET 방식 (간단한 삭제는 GET으로도 가능, 실무에서는 DELETE 권장)
- redirect 시 문자열 연결로 변수 포함
- 작업 후 게시글 상세 페이지로 리다이렉트

---

### 6. BoardController 수정 (detail 메서드)

```java
private final CommentService commentService;  // 필드 추가

@GetMapping("/board/detail/{id}")
public String detail(@PathVariable Long id, Model model) {
    Board board = boardService.findById(id);
    List<Comment> comments = commentService.findByBoardId(id);  // 추가
    
    model.addAttribute("board", board);
    model.addAttribute("comments", comments);  // 추가
    return "board/detail";
}
```

**학습 포인트:**
- Controller에서 여러 Service를 주입받아 사용 가능
- 게시글 상세 조회 시 댓글 목록도 함께 조회
- Model에 담아서 View로 전달

---

### 7. detail.html 수정 (댓글 영역 추가)

```html
<!-- 댓글 영역 -->
<hr>
<h3>댓글 (<span th:text="${#lists.size(comments)}">0</span>)</h3>

<!-- 댓글 목록 -->
<div th:if="${#lists.isEmpty(comments)}">
    <p>첫 댓글을 작성해보세요!</p>
</div>

<div th:unless="${#lists.isEmpty(comments)}">
    <div th:each="comment : ${comments}" style="border: 1px solid #ddd; padding: 10px; margin: 10px 0;">
        <div>
            <strong th:text="${comment.writer}">작성자</strong>
            <span th:text="${#temporals.format(comment.createdDate, 'yyyy-MM-dd HH:mm')}">2025-10-30 14:30</span>
            <a th:href="@{/comment/delete/{id}(id=${comment.id}, boardId=${board.id})}"
               onclick="return confirm('댓글을 삭제하시겠습니까?')">
                <button type="button">삭제</button>
            </a>
        </div>
        <p th:text="${comment.content}" style="margin-top: 10px;">댓글 내용</p>
    </div>
</div>

<!-- 댓글 작성 폼 -->
<h4>댓글 작성</h4>
<form th:action="@{/comment/write}" method="post">
    <input type="hidden" name="boardId" th:value="${board.id}">
    <div>
        <label>작성자:</label>
        <input type="text" name="writer" required>
    </div>
    <div>
        <label>댓글 내용:</label>
        <textarea name="content" rows="3" style="width: 100%;" required></textarea>
    </div>
    <div>
        <button type="submit">댓글 등록</button>
    </div>
</form>
```

**학습 포인트:**
- `${#lists.size(comments)}`: 댓글 개수 표시
- `th:if`, `th:unless`: 조건부 렌더링
- `th:each`: 댓글 목록 반복
- `input type="hidden"`: 화면에 안 보이지만 데이터 전송
- `required`: HTML5 유효성 검사
- `onclick="return confirm(...)`: JavaScript 삭제 확인

**Thymeleaf 날짜 포맷:**
```html
${#temporals.format(comment.createdDate, 'yyyy-MM-dd HH:mm')}
```

**URL에 여러 파라미터 전달:**
```html
th:href="@{/comment/delete/{id}(id=${comment.id}, boardId=${board.id})}"
→ /comment/delete/1?boardId=5
```

---

## 🎓 핵심 개념 정리

### 1. JPA 연관관계 매핑

**단방향 vs 양방향:**
- **단방향**: 한쪽에서만 참조 (Comment → Board만 가능)
- **양방향**: 양쪽에서 참조 (Comment ↔ Board 모두 가능)

**이번 구현:**
```java
Comment → Board  (@ManyToOne, @JoinColumn)  [연관관계의 주인]
Board → Comment  (@OneToMany, mappedBy)     [읽기 전용]
```

---

### 2. 연관관계의 주인 (Owner)

**규칙:**
- **외래키가 있는 테이블이 연관관계의 주인**
- Comment 테이블에 board_id가 있으므로 Comment가 주인
- 주인만 등록, 수정, 삭제 가능
- 주인이 아닌 쪽은 `mappedBy` 속성 사용

**왜 이렇게 설계?**
- 외래키를 가진 쪽에서 관계를 관리하는 것이 자연스러움
- DB의 구조와 객체 모델이 일치

---

### 3. Cascade와 orphanRemoval

**Cascade (영속성 전이):**
```java
cascade = CascadeType.REMOVE
```
- Board를 삭제하면 연관된 Comment도 함께 삭제

**orphanRemoval (고아 객체 제거):**
```java
orphanRemoval = true
```
- 부모 엔티티(Board)와 연관관계가 끊어진 자식 엔티티(Comment) 자동 삭제
- 예: `board.getComments().remove(comment)` 실행 시 Comment 삭제

**차이점:**
- Cascade: 부모의 상태 변화가 자식에게 전파
- orphanRemoval: 연관관계가 끊어진 자식을 자동 제거

---

### 4. LAZY vs EAGER 로딩

**LAZY (지연 로딩):**
```java
fetch = FetchType.LAZY
```
- 실제로 사용할 때 조회
- Board 조회 시 Comment는 조회하지 않음
- `board.getComments()`를 호출할 때 Comment 조회

**EAGER (즉시 로딩):**
```java
fetch = FetchType.EAGER
```
- 엔티티 조회 시 연관된 엔티티도 즉시 조회
- Board 조회 시 Comment도 함께 조회

**권장사항:**
- **@ManyToOne, @OneToOne**: 기본값 EAGER → LAZY로 변경 권장
- **@OneToMany, @ManyToMany**: 기본값 LAZY (그대로 사용)
- LAZY 로딩으로 성능 최적화

---

### 5. 연관관계 설정 방법

**Comment를 저장할 때:**
```java
Comment comment = new Comment();
comment.setWriter(writer);
comment.setContent(content);
comment.setBoard(board);  // 이것이 핵심!

commentRepository.save(comment);
```

**JPA가 자동으로:**
```sql
INSERT INTO comment (writer, content, board_id, created_date)
VALUES ('홍길동', '댓글 내용', 5, '2025-10-30 14:30:00');
```

**핵심:**
- `comment.setBoard(board)`를 하면 JPA가 board의 id를 board_id에 자동 설정
- 직접 board_id를 설정할 필요 없음

---

## 📊 데이터베이스 구조

### comment 테이블
```sql
CREATE TABLE comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content TEXT NOT NULL,
    writer VARCHAR(50) NOT NULL,
    created_date DATETIME NOT NULL,
    board_id BIGINT NOT NULL,
    FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE
);
```

**외래키 제약조건:**
- `board_id`: board 테이블의 id를 참조
- `ON DELETE CASCADE`: Board 삭제 시 Comment도 자동 삭제

---

### ERD (Entity Relationship Diagram)
```
┌─────────────┐         ┌──────────────┐
│   Board     │ 1     N │   Comment    │
├─────────────┤◄────────┤──────────────┤
│ id (PK)     │         │ id (PK)      │
│ title       │         │ content      │
│ content     │         │ writer       │
│ writer      │         │ created_date │
│ ...         │         │ board_id(FK) │
└─────────────┘         └──────────────┘
```

**관계:**
- 하나의 Board는 여러 Comment를 가질 수 있음 (1:N)
- 하나의 Comment는 하나의 Board에만 속함 (N:1)

---

## 🔍 동작 흐름

### 댓글 작성 흐름
```
1. 사용자가 댓글 작성 폼 작성
   ↓
2. POST /comment/write
   ↓
3. CommentController.write()
   - boardId, writer, content 받음
   ↓
4. CommentService.save()
   - Board 조회
   - Comment 객체 생성
   - 연관관계 설정 (setBoard)
   - 저장
   ↓
5. redirect:/board/detail/{boardId}
   ↓
6. BoardController.detail()
   - Board 조회
   - Comment 목록 조회
   ↓
7. detail.html 렌더링
   - 게시글 표시
   - 댓글 목록 표시
```

---

### 댓글 삭제 흐름
```
1. 사용자가 삭제 버튼 클릭
   ↓
2. JavaScript confirm (확인)
   ↓
3. GET /comment/delete/{id}?boardId={boardId}
   ↓
4. CommentController.delete()
   - commentId로 댓글 삭제
   ↓
5. redirect:/board/detail/{boardId}
   ↓
6. BoardController.detail()
   - Board 조회
   - Comment 목록 조회 (삭제된 댓글 제외)
   ↓
7. detail.html 렌더링
```

---

## 🐛 트러블슈팅

### 문제 1: @Id import 오류
**증상**: Comment 엔티티에서 @Id 어노테이션 인식 안 됨

**원인**: Spring Data의 @Id를 import
```java
import org.springframework.data.annotation.Id;  // ❌
```

**해결**:
```java
import jakarta.persistence.Id;  // ✅
```

---

### 문제 2: CommentService 메서드 호출 안 됨
**증상**: CommentController에서 commentService의 메서드가 보이지 않음

**원인**: Service 메서드에 public 없음
```java
List<Comment> findByBoardId(Long boardId) { ... }  // ❌
```

**해결**:
```java
public List<Comment> findByBoardId(Long boardId) { ... }  // ✅
```

**이유**: 다른 패키지에서 접근하려면 public 필요

---

### 문제 3: redirect URL에 변수가 치환되지 않음
**증상**: 리다이렉트 후 URL이 `/board/detail/{boardId}` 그대로 남음

**원인**: redirect에서는 PathVariable 문법이 작동하지 않음
```java
return "redirect:/board/detail/{boardId}";  // ❌
```

**해결**:
```java
return "redirect:/board/detail/" + boardId;  // ✅
```

---

## 💡 배운 것과 느낀 점

### 기술적 학습
1. **JPA 연관관계**: @ManyToOne, @OneToMany로 테이블 관계를 객체로 표현
2. **양방향 연관관계**: mappedBy로 연관관계의 주인 설정
3. **Cascade & orphanRemoval**: 부모 삭제 시 자식도 함께 관리
4. **LAZY 로딩**: 성능 최적화를 위한 지연 로딩
5. **연관관계 설정**: setBoard()로 외래키 자동 설정

### 개발 패턴
1. **연관관계 매핑**: 외래키를 객체 참조로 변환
2. **Service 계층 분리**: 각 엔티티마다 독립적인 Service
3. **Controller 역할 분리**: Board와 Comment 각각의 Controller

### 문제 해결 능력
1. import 오류 해결 (jakarta.persistence vs spring.data)
2. 접근 제어자 이해 (public의 중요성)
3. redirect URL 처리 (문자열 연결)

### 어려웠던 점
1. 양방향 연관관계 개념 이해
2. mappedBy의 의미와 연관관계의 주인
3. public 생략 시 접근 불가 문제

### 느낀 점
- JPA의 연관관계 매핑은 처음엔 어렵지만 이해하면 매우 강력
- 외래키를 직접 다루지 않고 객체 참조로 관리하는 것이 편리
- 테이블 설계와 객체 설계의 차이를 이해하는 것이 중요
- 댓글 기능은 거의 모든 게시판에 필수인 만큼 잘 이해해야 함

---

## 🎯 다음 계획

### Phase 4: 대댓글 기능 (예정)
- [ ] Comment 엔티티에 자기 참조 추가
- [ ] parent/child 관계 설정
- [ ] 대댓글 작성 UI 추가
- [ ] 대댓글 표시 로직 (들여쓰기)
- [ ] 1단계 깊이 제한

### 학습 목표
- 자기 참조 연관관계 이해
- 계층형 데이터 구조 처리
- UI에서 계층 구조 표현

---

## 📝 메모

### 핵심 개념 복습
1. **연관관계의 주인**: 외래키를 가진 쪽
2. **mappedBy**: 주인이 아닌 쪽에 설정
3. **setBoard(board)**: 연관관계 설정 = 외래키 설정
4. **LAZY 로딩**: 필요할 때만 조회

### 실무 팁
- Service 메서드는 항상 public
- 연관관계 설정 후 저장
- Cascade 옵션 신중히 선택
- LAZY 로딩 기본 사용

---

**작성일**: 2025.10.30  
**이전 학습**: [Day 2 - 페이징 및 검색 기능](day2.md)  
**다음 학습**: [Day 4 - 대댓글 기능 구현](day4.md) (예정)