# 09. Testing Strategy — HexaOracle (MVP)

> 목적: **주요 도메인 규칙**과 **API 계약**을 신뢰성 있게 검증하고, 배포 전후로 **회귀 리스크**를 최소화한다. 본 문서는 테스트 계층, 도구/프레임워크, 모킹 원칙, 커버리지 목표, 파이프라인 게이트, 각 API별 필수 테스트 케이스를 정의한다.

---

## 1) 원칙 (Principles)

* **도메인 우선**: 규칙은 프레임워크에 독립적으로 검증한다(TDD).
* **테스트 피라미드**: Unit ▶ Integration ▶ E2E 순으로 수량이 감소하고, 빠른 테스트가 다수를 차지한다.
* **계약 우선**: API는 **OpenAPI 스키마**와 **예상 에러 포맷**을 준수한다.
* **관측 가능성**: 실패에 대한 로그/트레이스/메트릭이 즉시 확인 가능해야 한다.
* **실행 가능 문서**: 요구사항(AC)과 API 스펙의 예시를 테스트로 구현한다.

---

## 2) 테스트 계층 (Layers)

### 2.1 Domain Unit Tests (순수 도메인)

* **대상**: `Line`, `Lines`, `Hexagram`, `HexagramService`, `ChangedHexagramService`, `Oracle` aggregate 등의 순수 규칙.
* **도구**: JUnit5, AssertJ, Property-based(jqwik)
* **모킹**: 없음(순수 함수/VO 검증)
* **핵심 불변식/성질**

    * `Lines.length == 6`
    * 각 원소 ∈ {6,7,8,9}
    * 변효 인덱스 = 값이 6 또는 9인 위치
    * flip 연산 **involution**(두 번 뒤집으면 원상복구)
    * `(upper, lower)` → `kingWenNo` 매핑의 전단사성(테이블 기준)
* **프로퍼티 기반 테스트 예시**

    * 임의의 `lines`에서 변효만 뒤집은 `lines'`에 대해 `flip(flip(lines)) == lines`.
    * 변효가 0개면 `changedHexagram == null`.

### 2.2 Application Unit/Component Tests (UseCase)

* **대상**: `CastCoinUseCase`, `RequestInterpretationUseCase`, `ListMyOraclesUseCase` 등.
* **도구**: JUnit5 + Mockito (도메인 서비스/포트 목킹)
* **검증 포인트**

    * 입력 유효성 위임(잘못된 `lines` 차단)
    * 트랜잭션 경계에서 도메인 호출 순서 보장
    * LLM 프록시 실패 시 **fallback** 정책 적용

### 2.3 Web Adapter Tests (@WebMvcTest)

* **대상**: Controller, 요청/응답 DTO 매퍼, Bean Validation, 에러 포맷
* **도구**: Spring `@WebMvcTest`, `MockMvc`, `JacksonTester`
* **검증 포인트**

    * 헤더(`Authorization`, `Idempotency-Key`, `Accept-Language`) 처리
    * 잘못된 입력에 대한 4xx + 표준 에러 JSON
    * 페이지네이션/필터 파라미터 바인딩

### 2.4 Persistence Integration Tests (Testcontainers)

* **대상**: JPA 엔티티/리포지토리, 쿼리, 인덱스 활용
* **도구**: Testcontainers(MySQL 8.x), Flyway, Spring Data JPA
* **검증 포인트**

    * `oracle`/`interpretation`/`hexagram*` 스키마 제약
    * 커서 페이지네이션 쿼리, 인덱스 효과
    * 시드 데이터(64괘/효사) 정합성

### 2.5 API Contract/Schema Tests

* **대상**: `/api/v1/**` 전 엔드포인트
* **도구**: RestAssured, springdoc-openapi, **schemathesis**(옵션)
* **검증 포인트**

    * 성공/에러 응답이 OpenAPI 스키마에 부합
    * 에러 코드 테이블과 일치(예: `INVALID_LINES`, `RATE_LIMITED`)

### 2.6 LLM Proxy Tests (Integration)

* **대상**: OpenAI 프록시 클라이언트, 프롬프트 빌더
* **도구**: WireMock/MockWebServer
* **검증 포인트**

    * PII 미포함(이메일/토큰/키 등) 페이로드 보장
    * 타임아웃/재시도, 실패 시 `interpretation.status=FAILED` 처리

### 2.7 E2E/System Tests

* **대상**: 사용자 흐름(가입/로그인 → 점괘 생성 → 해석 보기 → 히스토리)
* **도구**: Playwright 또는 Cypress + 백엔드 실서버(스테이징)
* **전략**: LLM 호출은 **스텁**(일관 응답)으로 고정해 비결정성 제거

### 2.8 Non-Functional Tests

* **성능**: k6/JMeter — 목표: 점괘 생성 API p95 ≤ 200ms, 해석 Job 90% ≤ 5s
* **보안**: OWASP ZAP(DAST), JWT 만료/재발급, CORS, Rate Limit(100 req/min) 시나리오
* **신뢰성**: 장애/재시도, Idempotency-Key 중복 생성 방지, 백업/복구 리허설 체크

---

## 3) 모킹/스텁 가이드

* **도메인**: 모킹 금지(순수 함수)
* **어댑터/외부 I/O**: 포트 인터페이스 뒤에서 모킹/스텁(WireMock)
* **LLM**: 테스트 기본값은 **스텁 응답**(비결정성/비용 억제)
* **DB**: 단위 테스트는 인메모리 금지, 통합은 Testcontainers로 실제 MySQL 사용

---

## 4) 커버리지 & 품질 게이트

* **라인 70% / 브랜치 60%**(백엔드), 프론트는 라인 70% 기준 권장
* **ArchUnit 룰**: 계층 의존성 위반 시 빌드 실패
* **CI 게이트**: format/lint/test/openapi-validate 통과 없이는 머지 금지

---

## 5) 파이프라인 (CI)

1. **Build & Unit** (domain/application/web)
2. **OpenAPI 생성 & 검증** (스키마 정합성)
3. **Testcontainers 통합 테스트** (MySQL/Flyway)
4. **Contract 테스트** (RestAssured + schemathesis)
5. **E2E(옵션, 스테이징 트리거)**
6. **품질 게이트**: 커버리지, ArchUnit, Lint

---

## 6) 환경 매트릭스

* **Java 21 + Spring Boot**, **MySQL 8.x**, **Redis(옵션: 큐/캐시)**
* **로케일**: `ko|en` — 응답 메시지/사전 텍스트 확인
* **토큰 유형**: Access/Refresh 만료/갱신 시나리오

---

## 7) API별 필수 테스트 케이스 (요약)

### 7.1 POST `/auth/signup`

* 성공: 형식 유효, `201`, 유저 생성, 토큰 반환
* 실패: 중복 이메일 `409 EMAIL_EXISTS`, 약한 비밀번호 `422 WEAK_PASSWORD`

### 7.2 POST `/auth/login`

* 성공: `200`, access/refresh 토큰
* 실패: `401 INVALID_CREDENTIALS`, 잠금/백오프 로깅 확인

### 7.3 POST `/oracles`

* 성공: `201`, 본괘/변괘/변효 계산, `createdAt`/`oracleId`
* 헤더: `Authorization` 필수, `Idempotency-Key` 재요청 동일 응답
* 검증 실패: `422 INVALID_LINES` (길이≠6, 값∉{6,7,8,9})
* 로케일: `Accept-Language=ko|en`에 따라 텍스트 현지화(사전)
* LLM 실패: `interpretation.status=FAILED`, fallback 응답 동작 확인

### 7.4 GET `/me/oracles`

* 성공: 커서 페이지네이션 동작(`nextCursor`), 기본 정렬 `-createdAt`
* 필터: `from/to`, `hexagram`, `q`(텍스트)
* 권한: 본인 소유만 노출

### 7.5 GET `/me/oracles/{id}`

* 성공: 단건 상세 + (옵션) `?include=interpretation`
* 권한: NOT\_OWNER → `403`
* 존재하지 않음: `404`

### 7.6 GET `/hexagrams` & `/hexagrams/{no}`

* 성공: 캐시/ETag 동작(304), 다국어 이름/문구 정합성
* 바이너리/팔괘 파라미터 조합 검색

### 7.7 보안/비기능 공통

* **Rate Limit**: 100 req/min(게스트 30) 초과 시 `429` + `Retry-After`
* **CORS/보안 헤더**: 정책 위반 요청 차단

---

## 8) 테스트 데이터 & 픽스처

* **도메인 픽스처**: 대표 `lines` 케이스(무변/단일변/복수변)
* **사전 시드**: 64괘 + 6효 텍스트(ko/en) — Flyway `V2__seed_hexagrams.sql`
* **E2E**: 테스트 전용 사용자/토큰 발급 스크립트

---

## 9) 예시 코드 스니펫

### 9.1 Domain Property Test (jqwik)

```java
@Property
void flip_is_involution(@ForAll("validLines") int[] lines) {
  int[] flipped = ChangedHexagramService.flip(lines);
  int[] ref = ChangedHexagramService.flip(flipped);
  assertThat(ref).containsExactly(lines);
}
```

### 9.2 WebMvc Validation Test

```java
@WebMvcTest(OracleController.class)
class OracleControllerTest {
  @Autowired MockMvc mvc;

  @Test void invalid_lines_422() throws Exception {
    mvc.perform(post("/api/v1/oracles")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"lines\":[7,8,9,7]}"))
      .andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.error.code").value("INVALID_LINES"));
  }
}
```

### 9.3 Contract (RestAssured)

```java
 given()
   .header("Authorization", token)
   .body(Map.of("lines", List.of(7,8,9,7,8,8)))
 .when()
   .post("/api/v1/oracles")
 .then()
   .statusCode(201)
   .body("chosen.movingLineIndexes", hasItem(3));
```

---

## 10) 리포트 & 관측성

* **테스트 리포트**: JUnit XML, Jacoco HTML, Playwright Report
* **메트릭**: 요청 실패율, p95 지연, LLM 타임아웃 비율
* **로깅**: `X-Request-Id` 상관ID, PII 마스킹 규칙 준수

---

## 11) 릴리스 체크리스트 (Testing DoD)

* ✅ 모든 테스트/정적 분석/포맷 통과
* ✅ OpenAPI 스키마와 실제 런타임 응답 일치
* ✅ 성능 목표(p95 200ms / LLM 5s) 준수
* ✅ Rate Limit/보안 헤더/인증 시나리오 검증
* ✅ 롤백/모니터링 계획 및 경보 설정 확인

---

## 12) 변경 관리

* 본 문서 변경은 **\[DOCS] 커밋**으로 관리하며, 도메인/스펙 영향이 있으면 **03-domain-model.md**와 **04-api-spec.md**를 동시에 업데이트한다.
