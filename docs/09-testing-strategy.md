# 08. Security & Privacy — HexaOracle (MVP Draft)

> 범위: 프론트(React), 백엔드(Spring Boot), DB(MySQL), 인프라(Docker), 외부 AI(OpenAI API). 본 문서는 **MVP 보안/프라이버시 기준선**을 정의하며, 운영 단계에서 지속 개선한다.

---

## 1) 목표 & 원칙

* **최소 수집**(Data Minimization): 서비스 제공에 반드시 필요한 정보만 수집/저장.
* **기본값 보안**(Secure by Default): 닫힌 CORS, 짧은 수명 토큰, 최소 권한.
* **분리**(Separation): 점 행위(`oracle`)와 AI 해석(`interpretation`)의 저장 책임 분리.
* **가시성**(Observability): 감사/접근 로그, 경보(Alerts)로 이상 징후 탐지.
* **복구 가능성**(Resilience): 백업/DR, 키 롤테이션 계획.

---

## 2) 위협 모델(요약)

* **주요 자산**: 사용자 계정(이메일/패스워드 해시), 점괘 히스토리(질문/lines/괘), 관리자 권한, API 키(데이터베이스/LLM).
* **공격 벡터**: 취약한 인증(Brute-force), 인젝션(SQL/LLM Prompt), XSS, CSRF(쿠키 사용 시), 토큰 탈취, 취약한 의존성, 잘못된 CORS/헤더 설정, 비정상 대량 호출(DoS/스크래핑).
* **가정**: 전송 구간 TLS, 서버/DB에 기본 방화벽, CI에서 의존성 스캔 가동.

---

## 3) 데이터 분류 & 처리 목적

| 분류      | 항목                            | 저장소               | 목적          | 보존          | 근거     |
| ------- | ----------------------------- | ----------------- | ----------- | ----------- | ------ |
| PII     | email                         | DB.user           | 로그인, 통지     | 탈퇴 시 삭제     | 계약 이행  |
| 민감(해시)  | password\_hash                | DB.user           | 인증          | 변경/탈퇴 시     | 계약 이행  |
| 사용자 컨텐츠 | question, lines, hexagram ids | DB.oracle         | 서비스 제공/히스토리 | 사용자 삭제 요청 시 | 정당한 이익 |
| AI 생성물  | ai\_commentary                | DB.interpretation | 해석 제공       | 사용자 삭제 요청 시 | 정당한 이익 |
| 텔레메트리   | requestId, IP(마스킹), UA        | 로그                | 운영/보안 모니터링  | 30\~90일     | 정당한 이익 |

> 원칙: **AI 요청에는 PII를 포함하지 않는다.** (email, IP 등 배제)

---

## 4) 인증/인가

* **로그인**: 이메일+패스워드, 실패 5회/15분 잠금(백오프), 계정 열거 방지(모호한 에러 메시지).
* **암호 저장**: `Argon2id`(권장) 또는 `bcrypt(12+)`로 해시. 솔트 필수.
* **토큰**: JWT Access(≤ 1h) + Refresh(≤ 30d). Refresh 토큰 블랙리스트/철회 테이블(옵션) 운영.
* **권한**: `user`, `admin`(최소 권한). 관리 UI는 IP 제한 + 2FA(추가 계획).
* **게스트**: MVP는 토큰 기반 세션만(옵션 `guest_token`은 후속 결정).

---

## 5) API 보안

* **TLS/HSTS**: HTTPS 강제, HSTS 프리로드(도메인 준비 후).
* **CORS**: 허용 오리진 화이트리스트(프론트 도메인), `credentials=false`(JWT 헤더 기반).
* **Rate Limit**: 기본 `100 req/min`(게스트 30). 초과 시 `429` + `Retry-After`(04-API 규약 준수).
* **Idempotency**: 쓰기 요청에 `Idempotency-Key` 지원(중복 생성 방지).
* **입력 검증**: `lines_json` 길이/도메인 값 유효성 검사(6개, 값∈{6,7,8,9}).
* **출력 안전화**: JSON 응답에 HTML/스크립트 삽입 금지, 에러는 표준 포맷.
* **보안 헤더**: `X-Content-Type-Options: nosniff`, `Referrer-Policy: strict-origin-when-cross-origin`, `X-Frame-Options: DENY`.

---

## 6) 프론트엔드(React) 방어

* **XSS 방지**: `dangerouslySetInnerHTML` 금지, 외부 Markdown 렌더 시 화이트리스트/샌드박스.
* **CSP**: `default-src 'self'; img-src 'self' data:; connect-src 'self' api-domain; frame-ancestors 'none'` (초안).
* **토큰 저장**: `Authorization: Bearer` 헤더(로컬 스토리지 사용 시 XSS 최소화 필수). 가능하면 메모리 보관 + 새로고침 시 재인증.

---

## 7) LLM 연동 보안/프라이버시

* **데이터 최소화**: OpenAI에 전송하는 페이로드는 `question`, `hexagram` 요약만. **email/userId/IP 미포함**.
* **프롬프트 인젝션 방지**: 시스템 프롬프트에 안전 규칙 명시(외부 데이터 요청 금지, 내부 토큰/키 노출 금지). 사용자 입력은 검증 및 길이 제한.
* **타임아웃/재시도**: 실패 시 `interpretation.status=FAILED`. 응답은 **사전 텍스트 기반 fallback** 조합(저장 X).
* **모델/버전 태깅**: `interpretation.version`에 모델/템플릿 버전 기록.
* **데이터 사용 정책**: OpenAI API는 기본적으로 학습 미사용(옵트인하지 않음). 비밀키는 서버측에서만 사용.

---

## 8) 데이터 저장/암호화/백업

* **저장소**: MySQL(UTF8MB4). 스키마는 06-ERD 준수.
* **암호화**: 디스크 암호화(호스트/클라우드), 백업 파일 암호화. 열(Column) 암호화는 PII 범위 축소로 대체.
* **백업/복구**: 일일 스냅샷 + 7/30일 보존(개발/운영 분리). 복구 리허설 분기 1회.
* **삭제/정정**: 계정 삭제 시 `user` 및 소유 `oracle`/`interpretation` 삭제(혹은 비가역 익명화). 외래키 ON DELETE 전략은 마이그레이션에서 명시.

---

## 9) 로깅/모니터링/감사

* **요청 추적**: `X-Request-Id` 수용, 모든 쓰기 요청은 감사 로그(누가/언제/무엇을).
* **민감정보 마스킹**: 이메일, IP는 필요 시 마스킹. 패스워드/토큰/키는 로그 금지.
* **경보**: 인증 실패 급증, 4xx/5xx 급증, Rate Limit 초과 패턴 경보.

---

## 10) 비밀 관리 & CI/CD

* **Secrets**: `.env` 금지, 배포 환경 변수/비밀 금고(KMS/Secret Manager) 사용. 키 주기적 롤테이션.
* **CI 보안**: 최소 권한 토큰, 서드파티 액션 표준화, 의존성 스캔(SCA) + SAST, 빌드 아티팩트 서명(옵션).
* **이미지**: Docker 기반. 최소 베이스 이미지, 루트 미사용, 취약점 스캔.

---

## 11) 규정 준수(라이트)

* **GDPR/CCPA 최소 준수**: 접근/삭제 요청(DSR) 처리 절차, 개인정보처리방침(Privacy Notice) 공개.
* **쿠키 배너**: JWT 헤더 방식 사용 시 불필요(추후 분석/광고 쿠키 추가 시 배너/설정 필요).

---

## 12) 운영 시나리오

* **AI 실패 Fallback**: `status=FAILED` → 사전(`hexagram*`) 텍스트 조합을 응답. 저장하지 않음.
* **해석 제외 옵션**: `?include=interpretation=false` → 오직 점 결과(본괘/변괘) + 사전 텍스트만 반환.
* **계정 삭제**: 사용자 트리거 → 관련 리소스 삭제/익명화 → 백업 보존 정책 따름.

---

## 13) 출시 체크리스트 (MVP)

* [ ] 비밀번호 해시 `bcrypt(12+)` 또는 `Argon2id` 적용 테스트 통과
* [ ] JWT 만료/재발급/철회 시나리오 E2E 테스트
* [ ] CORS 허용 오리진 정확히 설정(로컬/스테이징/프로덕션 분리)
* [ ] Rate Limit(게스트/사용자) 및 에러 포맷 검증
* [ ] LLM 요청에 PII 미포함 보장(Unit/Integration)
* [ ] 로깅 마스킹 규칙/경보 룰 배포
* [ ] 백업/복구 리허설 1회 수행 기록
* [ ] 관리자 엔드포인트 보호(IP 제한/기본 인증 철거/비공개화)

---

## 14) 오픈 이슈

* [ ] `interpretation` 비동기 잡 분리 필요성 재검토(트래픽 증가 시)
* [ ] CSP 세부 정책(이미지/폰트/CDN) 고도화
* [ ] 데이터 보존 기간 구체화(로그/백업/사용자 컨텐츠)

> 문서 변경 시 커밋 타입은 **\[DOCS]** 를 사용. 보안 설계 변경이 도메인/스펙에 영향 줄 경우 **03-domain-model.md**와 **04-api-spec.md**를 함께 갱신한다.
