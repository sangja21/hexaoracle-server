# 11. Runbook — 운영자 가이드 (Template)
> 상태: Draft (pre-dev). 배포 후 실제 값/툴/URL로 치환.

## 0) 기본 정보
- 서비스: **HexaOracle API**
- 환경: `<dev|stg|prod>`
- Base URL: `<https://api.example.com/api/v1>`
- 접점: Slack `#ops-hexaoracle`, On-call `<name>` (시간대/연락처 기입)

---

## 1) 헬스체크 / 버전 확인
- **헬스체크**: `GET {BASE}/health` → `{"status":"ok","version":"<date or semver>"}`
- **버전**: `GET {BASE}/version` → `{gitSha, buildTime}`
- **OpenAPI**: `GET {BASE}/openapi.json`
- (K8s 사용 시) `readinessProbe`/`livenessProbe`는 `/health`로 통일
> 점검 Checklist
> - [ ] `/health` 200 확인
> - [ ] `/version`에서 릴리즈 대상 `gitSha` 확인
> - [ ] OpenAPI 문서 로딩/스키마 유효성 확인

---

## 2) 장애 유형별 대응 (MVP)
### A. **LLM 타임아웃/실패 → 룰(사전) 폴백**
1) **징후**: 해석 생성 지연, 5xx/타임아웃, LLM 프록시 오류 로그
2) **즉시 조치**
    - API는 `interpretation.status=FAILED`로 처리 후 **사전 텍스트 기반 fallback**을 응답(저장하지 않음).
    - 응답 지연이 계속되면 LLM 호출을 **일시 비활성(토글)** 하고 폴백만 제공(디그레이드 모드).
3) **점검**
    - LLM 프록시 헬스/키 유효성, 외부 API 한도, 큐 적체(옵션: Redis Job) 확인
    - Rate Limit/에러 스파이크 알람 확인
4) **복구/후속**
    - 재시도 윈도 설정(백오프), 정상화 후 폴백 토글 원복
    - 장애 리포트: 원인/지표/재발 방지 액션 기록

> 운영 메모
> - 폴백은 **사전(64괘/효사) 조합 텍스트**만 사용, DB에 **영구 저장 금지**.
> - 테스트 전략에 폴백 케이스 포함(계약/통합 테스트).

---

## 3) 시드 재적재 / 마이그레이션 순서
- **표준 순서(Flyway)**
    1. `V1__schema.sql` (코어 스키마)
    2. `V2__seed_hexagrams.sql` (64괘/효사 시드)
    3. `V3__indexes.sql` (접근 패턴 확정 후)
    4. `V4__i18n.sql` (다국어 보강)
- **개발/스테이징 재적재(예시)**
    - `flyway repair && flyway migrate`
    - 시드만 재주입이 필요할 때: 버전 증가 `V{N}__seed_refresh.sql`로 **누적 적용** (기존 버전 수정 금지)
- **프로덕션 주의**
    - 시드 변경은 **새 마이그레이션 파일**로 배포
    - DDL/대량 DML은 유지보수 창구에서 수행, 롤백 플랜/백업 확인
- **소스 위치**: `src/main/resources/db/migration`

---

## 4) 핸드오버(1페이지) 체크리스트 ✅
- [ ] `/health` 200 / `/version` gitSha 일치 / `openapi.json` 로드 확인
- [ ] LLM 장애 시 폴백 동작(응답 OK, **저장은 안 함**) 검증
- [ ] 로그/알람: 4xx/5xx 급증, Rate Limit 초과 패턴 모니터
- [ ] 마이그레이션: `V1→V2→V3…` 순서, **수정 금지/추가만** 원칙 준수
- [ ] 비밀/키: 환경변수/시크릿 매니저에서 관리(로테이션 정책 확인)
- [ ] 문제 발생 시 연락 체계/디그레이드 토글 위치 숙지
