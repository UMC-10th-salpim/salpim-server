# 🧓살핌

## 기술 스택
- Java: 17
- Spring Boot: 4.1.0
- MySQL

## 환경 변수 설정
프로젝트 루트에 `.env`파일을 생성하고 다음 정보를 입력하세요. (참고: `.env.example`)

```
DATABASE_URL=your_db_url
GEMINI_API_KEY=your_gemini_api_key
JWT_SECRET_KEY=your_secret_key
# 추가적인 환경 변수들... (Firebase 설정, STATIC_PROFILE_DIR, PUBLIC_BASE_URL 등)
```

## 코드 컨벤션
- Commit Convention 준수
- 브랜치 전략 준수
- PR 기반 코드 리뷰 진행

### 🌳branch 규칙
```
main
 └── dev
      └── feature/*
```

- main 직접 작업 금지
- dev 직접 작업 금지
- feature 브랜치 생성 후 작업
- PR을 통해 dev 브랜치 병합


### 🧷 커밋 컨벤션
| Type | Description |
| :--- | :--- |
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `docs` | 문서 수정 (README, API 문서 등) |
| `style` | 코드 의미에 영향을 주지 않는 변경 (포맷팅, 오타 수정) |
| `refactor` | 코드 리팩토링 (기능 변경 없이 코드 구조 개선) |
| `test` | 테스트 코드 추가 및 수정 |
| `chore` | 빌드 업무, 패키지 매니저 설정 변경, `.gitignore` 수정 등 |

