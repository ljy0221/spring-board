# Day 2 - 페이징 및 검색 기능 구현

**날짜**: 2025.10.22  
**목표**: 게시판에 페이징과 검색 기능 추가

---

## 📋 오늘의 작업 내용

### Phase 2-1: 페이징 처리
- [x] Pageable 인터페이스 이해 및 적용
- [x] Repository - 페이징 지원 (JpaRepository 기본 제공)
- [x] Service - Page<Board> 반환 메서드 수정
- [x] Controller - @PageableDefault 어노테이션 적용
- [x] View - 페이지네이션 UI 구현

### Phase 2-2: 검색 기능
- [x] Repository - Query Method 작성 (4개)
- [x] Service - 검색 로직 구현
- [x] Controller - 검색 파라미터 처리
- [x] View - 검색 폼 추가
- [x] 검색 조건 유지 기능

### 기타
- [x] 더미 데이터 50개로 확장 (페이징 테스트용)

---

## 🤔 주요 질문 및 학습 내용

### Q1. Pageable이란?

**학습 내용:**
- 페이지 번호, 페이지 크기, 정렬 정보를 담는 인터페이스
- Spring Data JPA가 제공하는 페이징 추상화

**사용 예:**
```java
@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
Pageable pageable
```

**설정 의미:**
- `size = 10`: 한 페이지에 10개씩
- `sort = "id"`: id 필드 기준 정렬
- `direction = DESC`: 내림차순 (최신순)

---

### Q2. Page<T>란?

**학습 내용:**
- 페이징된 데이터와 페이지 정보를 함께 제공하는 인터페이스
- List를 감싸고 있으면서 추가 정보 제공

**주요 메서드:**
```java
boards.getContent()        // 실제 데이터 리스트
boards.getNumber()         // 현재 페이지 번호 (0부터)
boards.getTotalPages()     // 전체 페이지 수
boards.getTotalElements()  // 전체 데이터 개수
boards.isFirst()           // 첫 페이지 여부
boards.isLast()            // 마지막 페이지 여부
boards.hasNext()           // 다음 페이지 존재 여부
boards.hasPrevious()       // 이전 페이지 존재 여부
```

**Thymeleaf에서 사용:**
```html
${boards.content}          // 실제 게시글 리스트
${boards.number}           // 현재 페이지
${boards.totalPages}       // 전체 페이지 수
```

---

### Q3. Query Method란?

**학습 내용:**
- 메서드 이름만으로 쿼리를 자동 생성하는 JPA 기능
- SQL을 직접 작성하지 않아도 됨

**작성 규칙:**
```
findBy + 필드명 + 조건
```

**조건 키워드:**
- `Containing`: LIKE '%키워드%'
- `And`: AND 조건
- `Or`: OR 조건
- `OrderBy`: 정렬
- `GreaterThan`: >
- `LessThan`:

**예시:**
```java
// 제목으로 검색 (LIKE '%키워드%')
Page<Board> findByTitleContaining(String title, Pageable pageable);

// 작성자로 검색
Page<Board> findByWriterContaining(String writer, Pageable pageable);

// 제목 OR 내용으로 검색
Page<Board> findByTitleContainingOrContentContaining(
    String title, String content, Pageable pageable);
```

**자동 생성되는 SQL:**
```sql
-- findByTitleContaining("스프링", pageable)
SELECT * FROM board 
WHERE title LIKE '%스프링%' 
ORDER BY id DESC 
LIMIT 10 OFFSET 0;
```

---

### Q4. @RequestParam이란?

**학습 내용:**
- URL의 쿼리 파라미터를 메서드 파라미터로 받는 어노테이션

**사용 예:**
```java
@GetMapping("/board/list")
public String list(@RequestParam(required = false) String searchType,
                   @RequestParam(required = false) String keyword) {
    // ...
}
```

**URL과 파라미터 매핑:**
```
/board/list?searchType=title&keyword=스프링

→ searchType = "title"
→ keyword = "스프링"
```

**옵션:**
- `required = false`: 필수 아님 (없어도 됨)
- `required = true`: 필수 (기본값)
- `defaultValue`: 기본값 지정

---

### Q5. GET vs POST, 언제 뭘 쓰나?

**학습 내용:**

**GET 방식:**
- 데이터 조회
- URL에 파라미터 노출
- 북마크 가능
- 브라우저 히스토리에 남음

**사용 예:**
- 게시글 목록 조회
- 게시글 상세 조회
- **검색** ← 오늘 사용!
- 페이징

**POST 방식:**
- 데이터 변경 (생성/수정/삭제)
- URL에 파라미터 안 보임
- 북마크 불가
- 새로고침 시 재전송 경고

**사용 예:**
- 게시글 작성
- 게시글 수정
- 게시글 삭제
- 로그인

**왜 검색은 GET?**
```
/board/list?searchType=title&keyword=스프링

→ 이 URL을 공유하거나 북마크 가능
→ 검색 결과 페이지를 다른 사람과 공유 편리
```

---

## 💻 작성한 코드

### 1. Repository (BoardRepository.java)
```java
public interface BoardRepository extends JpaRepository<Board, Long> {
    // 제목으로 검색
    Page<Board> findByTitleContaining(String title, Pageable pageable);
    
    // 내용으로 검색
    Page<Board> findByContentContaining(String content, Pageable pageable);
    
    // 작성자로 검색
    Page<Board> findByWriterContaining(String writer, Pageable pageable);
    
    // 제목 OR 내용으로 검색
    Page<Board> findByTitleContainingOrContentContaining(
        String title, String content, Pageable pageable);
}
```

**학습 포인트:**
- 메서드 이름만으로 쿼리 자동 생성
- Pageable 파라미터로 페이징 지원
- 구현 코드 없이 선언만으로 동작

---

### 2. Service (BoardService.java)
```java
// 페이징 적용된 전체 조회
public Page<Board> findAll(Pageable pageable) {
    return boardRepository.findAll(pageable);
}

// 검색 기능
public Page<Board> search(String searchType, String keyword, Pageable pageable) {
    switch (searchType) {
        case "title":
            return boardRepository.findByTitleContaining(keyword, pageable);
        case "content":
            return boardRepository.findByContentContaining(keyword, pageable);
        case "writer":
            return boardRepository.findByWriterContaining(keyword, pageable);
        case "titleOrContent":
            return boardRepository.findByTitleContainingOrContentContaining(
                keyword, keyword, pageable);
        default:
            return boardRepository.findAll(pageable);
    }
}
```

**학습 포인트:**
- switch 문으로 검색 타입 분기
- 같은 Pageable을 모든 메서드에 전달
- 기본값 처리 (default)

---

### 3. Controller (BoardController.java)
```java
@GetMapping("/board/list")
public String list(
    @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) 
    Pageable pageable,
    @RequestParam(required = false) String searchType,
    @RequestParam(required = false) String keyword,
    Model model) {
    
    Page<Board> boards;
    
    if (keyword != null && !keyword.isEmpty()) {
        boards = boardService.search(searchType, keyword, pageable);
    } else {
        boards = boardService.findAll(pageable);
    }
    
    model.addAttribute("boards", boards);
    model.addAttribute("searchType", searchType);
    model.addAttribute("keyword", keyword);
    
    return "board/list";
}
```

**학습 포인트:**
- @PageableDefault로 기본 페이징 설정
- @RequestParam으로 검색 파라미터 받기
- 검색어 유무로 분기 처리
- 검색 조건도 Model에 담아서 View로 전달

---

### 4. View (list.html)

**검색 폼:**
```html
<form th:action="@{/board/list}" method="get">
    <select name="searchType">
        <option value="title" th:selected="${searchType == 'title'}">제목</option>
        <option value="content" th:selected="${searchType == 'content'}">내용</option>
        <option value="writer" th:selected="${searchType == 'writer'}">작성자</option>
        <option value="titleOrContent" 
                th:selected="${searchType == 'titleOrContent'}">제목+내용</option>
    </select>
    <input type="text" name="keyword" th:value="${keyword}" />
    <button type="submit">검색</button>
</form>
```

**페이지네이션 (검색 조건 포함):**
```html
<a th:href="@{/board/list(page=${page}, searchType=${searchType}, keyword=${keyword})}" 
   th:text="${page + 1}">
</a>
```

**학습 포인트:**
- GET 방식 폼 사용
- th:selected로 선택된 값 유지
- th:value로 입력 값 유지
- 페이지 링크에 검색 조건 포함

---

## 🎨 Thymeleaf 문법 정리

### 반복문에서 인덱스 사용
```html
<!-- 0부터 4까지 -->
<span th:each="i : ${#numbers.sequence(0, 4)}">
    <span th:text="${i}"></span>
</span>

<!-- 페이지 번호: 0부터 전체페이지-1까지 -->
<span th:each="page : ${#numbers.sequence(0, boards.totalPages - 1)}">
    <span th:text="${page + 1}"></span>  <!-- 1부터 표시 -->
</span>
```

### 조건부 속성
```html
<!-- selected 속성 조건부 추가 -->
<option value="title" th:selected="${searchType == 'title'}">제목</option>

<!-- 결과: searchType이 'title'이면 -->
<option value="title" selected>제목</option>
```

### URL에 여러 파라미터 전달
```html
<!-- 단일 파라미터 -->
th:href="@{/board/list(page=${page})}"
→ /board/list?page=1

<!-- 여러 파라미터 -->
th:href="@{/board/list(page=${page}, searchType=${searchType}, keyword=${keyword})}"
→ /board/list?page=1&searchType=title&keyword=스프링
```

### 빈 리스트 체크
```html
<tr th:if="${#lists.isEmpty(boards.content)}">
    <td colspan="5">검색 결과가 없습니다.</td>
</tr>
```

---

## 📊 페이징 동작 원리

### URL과 페이지 번호
```
/board/list              → page=0 (기본값, 1페이지)
/board/list?page=0       → 1페이지 (0부터 시작!)
/board/list?page=1       → 2페이지
/board/list?page=2       → 3페이지
```

### SQL 변환
```java
// page=0, size=10
Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());

↓ 변환

SELECT * FROM board 
ORDER BY id DESC 
LIMIT 10 OFFSET 0;  -- 1~10번째
```
```java
// page=1, size=10
Pageable pageable = PageRequest.of(1, 10, Sort.by("id").descending());

↓ 변환

SELECT * FROM board 
ORDER BY id DESC 
LIMIT 10 OFFSET 10;  -- 11~20번째
```

### OFFSET 계산
```
page=0 → OFFSET 0   (0 * 10)
page=1 → OFFSET 10  (1 * 10)
page=2 → OFFSET 20  (2 * 10)
page=3 → OFFSET 30  (3 * 10)
```

---

## 🔍 검색 동작 원리

### Query Method → SQL 변환

**1. 제목 검색**
```java
findByTitleContaining("스프링", pageable)

↓

SELECT * FROM board 
WHERE title LIKE '%스프링%' 
ORDER BY id DESC 
LIMIT 10 OFFSET 0;
```

**2. 제목 OR 내용 검색**
```java
findByTitleContainingOrContentContaining("스프링", "스프링", pageable)

↓

SELECT * FROM board 
WHERE title LIKE '%스프링%' 
   OR content LIKE '%스프링%'
ORDER BY id DESC 
LIMIT 10 OFFSET 0;
```

### URL 예시
```
/board/list?searchType=title&keyword=스프링
→ 제목에 "스프링" 포함된 게시글 검색

/board/list?searchType=title&keyword=스프링&page=1
→ 제목에 "스프링" 포함된 게시글 중 2페이지
```

---

## 🐛 트러블슈팅

### 문제 1: 페이지 번호가 너무 많이 표시됨
**상황**: 페이지가 많을 때 1, 2, 3, ... 50까지 모두 표시

**해결 방법 (선택사항):**
```html
<!-- 현재 페이지 기준 앞뒤 5개씩만 표시 -->
<span th:each="page : ${#numbers.sequence(
    T(Math).max(0, boards.number - 5), 
    T(Math).min(boards.totalPages - 1, boards.number + 5)
)}">
```

---

### 문제 2: 검색 후 페이지 이동 시 검색 조건 사라짐
**원인**: 페이지네이션 링크에 검색 조건 미포함

**해결:**
```html
<!-- ❌ 잘못된 예 -->
th:href="@{/board/list(page=${page})}"

<!-- ✅ 올바른 예 -->
th:href="@{/board/list(page=${page}, searchType=${searchType}, keyword=${keyword})}"
```

---

### 문제 3: 검색어 한글 깨짐
**원인**: URL 인코딩 문제

**해결**: 스프링 부트는 기본적으로 UTF-8 설정되어 있어 문제없음
만약 문제가 있다면:
```properties
# application.properties
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.force=true
```

---

## 💡 핵심 개념 정리

### 1. 페이징의 3요소
```
1. Pageable   : 페이지 정보 입력 (요청)
2. Repository : 페이징 쿼리 실행
3. Page<T>    : 페이징 결과 출력 (응답)
```

### 2. Query Method 네이밍 규칙
```
findBy + 필드명 + 조건 + (And/Or) + 필드명 + 조건

예:
findByTitle                    → WHERE title = ?
findByTitleContaining          → WHERE title LIKE %?%
findByTitleAndWriter           → WHERE title = ? AND writer = ?
findByTitleContainingOrContentContaining
→ WHERE title LIKE %?% OR content LIKE %?%
```

### 3. 페이지 번호의 혼란
```
내부 (Java, DB):   0, 1, 2, 3, 4  (0부터 시작)
화면 (사용자):     1, 2, 3, 4, 5  (1부터 표시)

→ 화면 표시 시: ${boards.number + 1}
```

### 4. GET 방식 검색의 장점
```
✅ URL 공유 가능
✅ 북마크 가능
✅ 브라우저 뒤로가기 동작
✅ 검색 엔진 크롤링 가능
```

---

## 📈 성능 고려사항 (참고)

### 페이징 성능
```sql
-- OFFSET이 클수록 느려짐
SELECT * FROM board 
ORDER BY id DESC 
LIMIT 10 OFFSET 10000;  -- 느림!

-- 해결: 커서 기반 페이징 (나중에 학습)
```

### LIKE 검색 성능
```sql
-- 앞에 %가 있으면 인덱스 사용 불가
WHERE title LIKE '%키워드%'  -- 느림

-- 전문 검색(Full-Text Search) 고려 (나중에)
```

---

## 🎯 오늘 배운 것

### 기술적 학습
1. **Pageable**: Spring Data JPA의 페이징 추상화
2. **Page<T>**: 페이징 결과와 메타데이터
3. **Query Method**: 메서드 이름으로 쿼리 자동 생성
4. **@RequestParam**: URL 파라미터 받기
5. **GET 방식 검색**: 검색 조건 URL 유지

### 개발 패턴
1. **검색 + 페이징 조합**: 가장 많이 쓰는 패턴
2. **조건부 분기**: keyword 유무로 검색/전체 구분
3. **상태 유지**: 검색 조건을 Model에 담아 View로 전달

### 문제 해결 능력
1. 페이지 번호 0부터 시작하는 것 이해
2. 검색 조건 유지하기
3. Thymeleaf 조건부 속성 활용

---

## 📝 메모

### 배운 점
1. JPA Query Method는 정말 강력하다
2. Pageable과 Page로 페이징이 매우 쉬워진다
3. GET 방식 검색이 URL 공유에 유리하다
4. Thymeleaf 문법이 점점 익숙해진다

### 어려웠던 점
1. 페이지 번호가 0부터 시작하는 것 (화면은 1부터)
2. 검색 조건을 페이지네이션 링크에 포함시키기
3. Query Method 네이밍 규칙 익히기

### 느낀 점
- 페이징과 검색은 거의 모든 게시판에 필수 기능
- Spring Data JPA가 정말 많은 것을 자동화해준다
- 한 번 이해하면 다른 프로젝트에도 바로 적용 가능
- 가장 많이 쓰일 것 같은 기능을 배웠다

---

## 🎯 내일 계획

### Phase 3: 댓글 기능 (예정)
- [ ] Comment 엔티티 설계
- [ ] Board-Comment 연관관계 (@OneToMany, @ManyToOne)
- [ ] 댓글 CRUD 구현
- [ ] 댓글 목록 표시
- [ ] 댓글 작성/삭제

### 학습 목표
- JPA 연관관계 이해
- @OneToMany, @ManyToOne 사용법
- 양방향 연관관계 설정
- 외래키 매핑

---

**작성일**: 2025.10.22  
**이전 학습**: [Day 1 - 프로젝트 초기 설정 및 기본 CRUD](day1.md)  
**다음 학습**: [Day 3 - 댓글 기능 구현](day3.md)