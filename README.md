# What's Your ETF

ETF 탐색, 포트폴리오 구성, 투자 시뮬레이션과 시장 뉴스 분석을 제공하는 팀 프로젝트입니다.

## 구성

- `frontend/WYE`: Android 앱(Kotlin, Jetpack Compose)
- `backend/user-service`: Spring Boot 사용자·포트폴리오 API
- `backend/data-service`: Python 기반 데이터 수집·분석 서비스
- `db`: 데이터베이스 관련 파일
- `docs`: 설계 및 개발 문서

## 로컬 설정

저장소에는 인증정보와 머신별 설정이 포함되지 않습니다. 실행 전에 각 서비스에서 요구하는 환경변수를 설정하고, Android 프로젝트에는 로컬에서 다음 파일을 준비하세요.

- `frontend/WYE/local.properties`
- `frontend/WYE/app/src/google-services.json`

비밀번호, API 키, JWT 서명키와 keystore는 커밋하지 마세요. 백엔드 환경변수 목록과 실행 방법은 `docs` 및 `exec` 디렉터리의 문서를 참고하세요.

## Android 단위 테스트

```powershell
cd frontend/WYE
./gradlew.bat testDebugUnitTest
```

## 주의

실제 배포 전에는 환경별 비밀정보 관리, 서명 설정, 외부 API 사용 권한을 별도로 구성해야 합니다.
