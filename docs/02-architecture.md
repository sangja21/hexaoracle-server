# 02-architecture.md — HexaOracle Server 아키텍처 개요

## 아키텍처 스타일 (헥사고날: Ports & Adapters)

```declarative
src/
 └─ main/
    ├─ java/
    │   └─ com.hexaoracle.server
    │       ├─ ServerApplication.java
    │       ├─ config/                 # 공통 설정 (CORS, Jackson, Security, Swagger 등)
    │       ├─ common/                 # 공통 (error, util, annotation, paging 등)
    │       │   ├─ error/
    │       │   │   ├─ GlobalExceptionHandler.java
    │       │   │   └─ ProblemDetails.java
    │       │   └─ util/
    │       ├─ domain/                 # "순수" 도메인 (엔티티/VO/도메인 서비스/포트)
    │       │   ├─ casting/
    │       │   │   ├─ model/          # ValueObject: Line(6/7/8/9), Hexagram, Trigram...
    │       │   │   ├─ service/        # 도메인 규칙: 본괘/변괘 계산
    │       │   │   └─ port/           # Repository/Reader/Writer 인터페이스
    │       │   ├─ dictionary/
    │       │   │   └─ model/
    │       │   └─ interpretation/
    │       │       └─ model/
    │       ├─ application/            # 유즈케이스 계층(트랜잭션, 흐름)
    │       │   ├─ casting/            # CastCoinUseCase, DTO(입출력)
    │       │   ├─ interpretation/     # RequestInterpretationUseCase
    │       │   └─ history/            # ListMyOraclesUseCase
    │       ├─ adapter/
    │       │   ├─ web/                # REST 컨트롤러, 요청/응답 DTO(웹 전용), 매퍼
    │       │   │   ├─ casting/
    │       │   │   ├─ interpretation/
    │       │   │   └─ dictionary/
    │       │   └─ persistence/        # JPA 엔티티/리포지토리(도메인 port 구현)
    │       │       ├─ casting/
    │       │       ├─ dictionary/
    │       │       └─ interpretation/
    │       └─ infrastructure/         # 외부 시스템 클라이언트, Flyway, Security 등
    │           ├─ ai/                 # LLM 클라이언트(프록시)
    │           └─ queue/              # 해석 Job 큐(테이블/메시지 브로커)
    └─ resources/
        ├─ application.yml
        └─ db/migration/               # Flyway V1__schema.sql, V2__seed.sql 등

test/
 └─ java/
    └─ com.hexaoracle.server
        ├─ domain/                     # 도메인 순수 테스트(TDD)
        ├─ application/                # 유즈케이스 단위 테스트(포트 목킹)
        ├─ adapter/web/                # WebMvcTest
        ├─ adapter/persistence/        # Testcontainers 통합 테스트
        └─ support/                    # Fixture/헬퍼

```

* **Domain**: 순수 규칙(엔티티/VO/도메인서비스/포트)
* **Application**: 유즈케이스, 트랜잭션, 흐름, 포트 호출
* **Adapters**: Web(컨트롤러/웹DTO/매퍼), Persistence(JPA 엔티티/리포지토리)
* **Infrastructure**: 외부 시스템 클라이언트(LLM), 보안, 큐 등

의존성 방향: `adapter`/`infrastructure` → `application` → `domain` (내측 단방향)

핵심 원칙:

* 비즈니스 규칙은 **domain** 밖으로 새지 않는다.
* **application**은 오케스트레이션과 트랜잭션만 담당한다.
* 외부 I/O는 모두 **port** 인터페이스 뒤로 숨긴다.

---

## 모듈 레이아웃 (멀티모듈 전환 가이드)

> 현재 단일 모듈이지만, 추후 멀티모듈 전환 시 가이드

* `server-domain`

    * `domain/casting|dictionary|interpretation`
    * `common` (ProblemDetails 등 공통 타입은 최소 공유)
* `server-application`

    * 유즈케이스(CastCoinUseCase, RequestInterpretationUseCase, ListMyOraclesUseCase)
* `server-adapter-web`

    * Controller, Web DTO, 매퍼
* `server-adapter-persistence`

    * JPA 엔티티/Repository 구현, Testcontainers 통합 테스트
* `server-infrastructure`

    * LLM 프록시, 보안, 큐/잡 처리

의존성 규칙(예시):

* `server-adapter-*` → `server-application` → `server-domain`
* `server-infrastructure` → `server-application` (필요 시) → `server-domain`
* 상호 참조 금지, 웹/퍼시스턴스는 도메인 엔티티 직접 노출 금지(변환 계층 명확화)

---

## 컴포넌트 개요

### 주요 컴포넌트

* **Domain**

    * `casting.model`: `Line(6/7/8/9)`, `Hexagram`, `Trigram` 등 VO
    * `casting.service`: 본괘/변괘 계산 로직(순수 함수/규칙)
    * `casting.port`: `CastingRepository`, `CastingReader`, `CastingWriter` 등
    * `dictionary.model`, `interpretation.model`
* **Application**

    * `casting`: `CastCoinUseCase` (+ I/O DTO)
    * `interpretation`: `RequestInterpretationUseCase`
    * `history`: `ListMyOraclesUseCase`
* **Adapters**

    * `web`: REST 컨트롤러, Web 전용 DTO, 매퍼
    * `persistence`: JPA 엔티티, Spring Data Repository(포트 구현)
* **Infrastructure**

    * `ai`: LLM 클라이언트 프록시(벤더 교체 용이)
    * `queue`: 해석 Job 큐(브로커 or 테이블)

---

## 테스트 전략

* `test/domain`: **순수 도메인 TDD** (프레임워크 없이 규칙 검증)
* `test/application`: **유즈케이스 단위 테스트** (포트 목킹, 경계/트랜잭션 검증)
* `test/adapter/web`: `@WebMvcTest`로 컨트롤러/변환/검증
* `test/adapter/persistence`: **Testcontainers** 기반 통합 테스트 (MySQL)
* `test/support`: Fixture/헬퍼 유틸(빌더, 샘플 Hexagram 등)

---

## 운영/비기능 고려

* **거버넌스**: ArchUnit로 패키지 의존성 룰 스캔, 빌드 실패 조건 설정
* **관측성**: 요청/쿼리/큐 Job 트레이스(상관ID), 메트릭(요청 실패율, LLM 지연), 구조적 로깅
* **보안**: 인증/인가(Security), 입력 검증, 비밀 키 분리(profile/Secret Manager)
* **성능**: 캐시(사전/해석 인기 항목), 비동기 분리, 쿼리 튜닝/인덱스, 배치
* **확장성**: LLM 벤더/큐 브로커 교체 가능(프록시/추상화 유지)
