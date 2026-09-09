# What's Your ETF (WYE)

ETF를 탐색하고 포트폴리오를 구성해 과거 성과를 시뮬레이션하며, 시장 뉴스와 보유 전략을 함께 관리하는 팀 프로젝트입니다.

## 주요 기능

- ETF 검색·필터링과 종목 상세 정보
- 산업·구성 종목 클러스터 시각화
- ETF 포트폴리오 구성과 백테스트
- 저장한 전략 비교와 AI 진단
- ETF 뉴스, 관심 종목, 보유 자산, 알림 관리

## 프로젝트 구성

| 경로 | 역할 | 기술 |
| --- | --- | --- |
| `frontend/WYE` | Android 애플리케이션 | Kotlin, Jetpack Compose, Hilt, Room, Retrofit |
| `backend/user-service` | 사용자·인증·ETF·포트폴리오 API | Java 21, Spring Boot, JPA |
| `backend/data-service` | 데이터 수집·분석·캐시 연동 | Python, FastAPI, SQLAlchemy |
| `db` | 데이터베이스 스키마와 기본 데이터 | PostgreSQL |
| `docs`, `exec` | API·설계·실행 가이드 | Markdown |

## 시작하기

### Android

Android Studio에서 `frontend/WYE`를 열고 다음 로컬 파일을 준비합니다. 두 파일은 Git에서 제외됩니다.

`frontend/WYE/local.properties`

```properties
sdk.dir=C\:\\path\\to\\Android\\Sdk
KAKAO_NATIVE_APP_KEY=your_kakao_native_app_key
```

Firebase Console에서 내려받은 `google-services.json`은 `frontend/WYE/app/src/`에 배치합니다. Debug 빌드는 Android SDK의 기본 debug keystore를 사용합니다.

```powershell
cd frontend/WYE
./gradlew.bat testDebugUnitTest
./gradlew.bat assembleDebug
```

### 사용자 서비스

JDK 21과 PostgreSQL이 필요합니다. 최소한 `DB_PASSWORD`와 충분히 긴 `JWT_SECRET`을 환경변수로 제공해야 하며, 메일·OAuth·AI·메시징 기능을 사용할 때는 해당 서비스 키도 설정해야 합니다.

```powershell
cd backend/user-service
./gradlew.bat test
./gradlew.bat bootRun
```

### 데이터 서비스

Python 가상환경을 만든 뒤 의존성을 설치하고 `.env`에 데이터베이스와 외부 API 설정을 추가합니다. `.env` 파일은 Git에서 제외됩니다.

```powershell
cd backend/data-service
python -m venv .venv
.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn app.main:app --reload
```

## 검증

현재 `main`은 다음 검증을 통과한 정리본입니다.

- Android 전체 단위 테스트와 Debug APK: `testDebugUnitTest`, `assembleDebug`
- 사용자 서비스 전체 단위 테스트: `test`
- 사용자 서비스 패키징: `clean bootJar -x test`
- 데이터 서비스 Python 구문 검사: `python -m compileall`

외부 API·Firebase·실제 데이터베이스를 사용하는 통합 기능은 각 서비스의 유효한 자격증명과 테스트 환경이 있어야 검증할 수 있습니다.

## 보안 및 데이터

- `local.properties`, `google-services.json`, keystore, `.env`는 커밋하지 않습니다.
- 비밀번호·API 키·JWT 서명키는 환경변수나 별도 비밀 관리 도구로 주입합니다.
- 사용자 정보가 포함될 수 있는 DB 덤프와 수집 데이터 덤프는 저장소에서 제외했습니다.
- 배포 전 외부 API 사용 권한과 Firebase·OAuth 설정을 환경별로 확인하세요.

## 문서

- Android 실행·배포: `exec/android_porting_guide.md`
- 백엔드 실행·배포: `exec/backend_porting_guide.md`
- API·설계 문서: `docs/`

## 브랜치

`main`이 유지보수 기준 브랜치입니다. 이전 GitLab 기반 작업 브랜치는 정리 전 태그로 보존합니다.
