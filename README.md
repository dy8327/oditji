# ODITJI

OTT 통합 검색 서비스 팀 프로젝트입니다.
여러 OTT 콘텐츠를 한 곳에서 검색하고, 상세 정보 확인 및 개인 목록 관리를 목표로 합니다.

---

## 프로젝트 개요

* 프로젝트명: ODITJI
* 주제: OTT 콘텐츠 통합 검색 서비스
* 개발 기간: 2026.06 ~ 2026.08
* 개발 인원: 4명
* 목적: OTT 콘텐츠 검색, 상세 정보 제공, 회원 기능, 관리자 기능 구현

---

## 기술 스택

### Backend

* Java 21
* Spring Boot
* MyBatis
* Oracle DB

### Frontend

* JSP
* HTML
* CSS
* JavaScript

### Build / Server

* Maven
* WAR
* Tomcat

### 협업 도구

* GitHub
* Notion
* VS Code

---

## 주요 기능

### 사용자 기능

* 회원가입
* 로그인 / 로그아웃
* 콘텐츠 검색
* 콘텐츠 목록 조회
* 콘텐츠 상세 조회
* 콘텐츠 랭킹 조회
* 마이리스트 관리
* 리뷰 작성 및 조회

### 관리자 기능

* 관리자 로그인
* 회원 관리
* 콘텐츠 관리
* 리뷰 관리

---

## 프로젝트 구조

```text
src/main/java/com/project/oditji
 ├── common
 ├── member
 ├── content
 ├── search
 ├── review
 ├── favorite
 ├── recommend
 └── admin

src/main/resources/mybatis/mappers
 ├── member
 ├── content
 ├── search
 ├── review
 ├── favorite
 ├── recommend
 └── admin

src/main/webapp/WEB-INF/views
 ├── common
 ├── member
 ├── content
 ├── search
 ├── review
 ├── favorite
 └── admin
```

---

## 브랜치 규칙

```text
main
└── develop
    ├── feature/common
    ├── feature/member
    ├── feature/content
    ├── feature/search
    └── feature/admin
```

| 브랜치               | 용도                  |
| ----------------- | ------------------- |
| `main`            | 최종 제출 / 발표용         |
| `develop`         | 개발 통합본              |
| `feature/common`  | 공통 설정, 공통 레이아웃, CSS |
| `feature/member`  | 회원 기능               |
| `feature/content` | 콘텐츠 기능              |
| `feature/search`  | 검색 기능               |
| `feature/admin`   | 관리자 기능              |

---

## Git 사용 규칙

1. `main` 브랜치에 직접 push 하지 않습니다.
2. 작업은 본인 `feature` 브랜치에서 진행합니다.
3. 작업 완료 후 `develop` 브랜치로 Pull Request를 생성합니다.
4. Pull Request 전 본인 PC에서 실행 확인을 합니다.
5. 공통 파일 수정 시 팀원에게 공유합니다.

---

## 커밋 메시지 예시

```text
회원 로그인 화면 JSP 추가
콘텐츠 목록 Mapper 작성
검색 결과 화면 UI 수정
관리자 회원 목록 Controller 추가
```

피해야 할 예시:

```text
수정
최종
테스트
진짜최종
```

---

## 팀원 역할

| 담당  | 주요 작업                    |
| --- | ------------------------ |
| 공통  | 프로젝트 설정, 공통 레이아웃, 공통 CSS |
| 회원  | 로그인, 회원가입, 마이페이지         |
| 콘텐츠 | 콘텐츠 목록, 상세, 랭킹           |
| 검색  | 검색 화면, 검색 결과, 필터         |
| 관리자 | 관리자 메인, 회원/콘텐츠/리뷰 관리     |

---

## 실행 방법

```bash
git clone 저장소주소
cd oditji
```

프로젝트 실행 전 `application.properties`의 DB 설정을 본인 환경에 맞게 수정합니다.

```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521/XEPDB1
spring.datasource.username=계정명
spring.datasource.password=비밀번호
```

---

## 참고 사항

* 본 프로젝트는 학원 팀 프로젝트 목적으로 제작되었습니다.
* 기능 및 화면 구성은 개발 진행 상황에 따라 변경될 수 있습니다.
