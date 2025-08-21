# 06. ERD — HexaOracle (Draft)

> 목적: 도메인 모델(03)과 API 스펙(04)을 **관계형 스키마**로 구체화한다. 본 문서는 MySQL 8.x 기준 초안이며, MVP 개발 이후 마이그레이션(플라이웨이)으로 진화시킨다.

* 범위: 회원/인증(요약), 오라클(점괘) 기록, AI 해석, 64괘 사전
* 원칙:

    * 동전 던지기(coin)는 **프론트 전용**. 서버는 **lines(6,7,8,9)** 배열만 수신/저장
    * 도메인 규칙은 애플리케이션에서 검증, DB는 무결성/참조/인덱싱에 집중
    * 히스토리는 `Oracle` 자체가 원장(ledger)
    * 국제화는 `locale` 컬럼(ko|en) + 사전 테이블의 다국어 컬럼으로 처리

---

## 1) ER 다이어그램


---

## 2) 테이블 정의 (초안)

### 2.1 `user`

* **id** `BIGINT AUTO_INCREMENT` (PK)
* **email** `VARCHAR(255)` (UNIQUE)
* **password_hash** `VARCHAR(255)`
* **display_name** `VARCHAR(50)`
* **role** `ENUM('user','admin')` DEFAULT 'user'
* **created_at / updated_at** `DATETIME(3)` (자동 타임스탬프)

**인덱스**

* `ux_user_email (email)`

> email은 중복체크 필요

---

### 2.2 `oracle`

* **id** `BIGINT AUTO_INCREMENT` (PK)
* **user_id** `BIGINT` (FK → user.id, NULL 허용)
* **question** `TEXT` — 사용자가 입력한 질문
* **lines_json** `JSON` (예: `[6,7,8,9,7,6]`) — 원본 효 배열
* **lines_bits** `CHAR(6)` (하→상, 1=양/0=음) — 효 패턴(검색/인덱스용)
* **moving_count** `TINYINT UNSIGNED` (0~6) — 변효(6·9)의 개수
* **original_hexagram_id** `SMALLINT` (FK → hexagram.id)
* **changed_hexagram_id** `SMALLINT NULL` (FK → hexagram.id)
* **locale** `VARCHAR(10)` (예: `ko-KR`, `en-US`)
* **created_at** `DATETIME(3)` (기본 NOW)

**인덱스**

* `idx_oracle_owner_created (user_id, created_at DESC)` — 히스토리 페이지네이션 핵심
* `idx_oracle_original (original_hexagram_id)`
* `idx_oracle_changed (changed_hexagram_id)`
* `idx_oracle_bits (lines_bits)` — 필요 시(바이너리 검색/통계)

**무결성 규칙(애플리케이션 레벨 권장)**

* `LEN(lines_bits) = 6`, 문자 ∈ {0,1}
* `moving_count = count(lines_json ∈ {6,9})`
* `moving_count=0 ⇒ changed_hexagram_id IS NULL`

> `oracle`은 점을 친 **행위(질문+결과 괘)** 자체만 기록하는 불변 원장.
> 해석은 별도 테이블(`interpretation`)로 분리하여 버전/언어/상태 관리.

---

### 2.3 `interpretation`

* **id** `BIGINT AUTO_INCREMENT` (PK)
* **oracle_id** `BIGINT` (FK → oracle.id, UNIQUE) — 1:1 연결
* **status** `ENUM('PENDING','IN_PROGRESS','COMPLETED','FAILED')` — AI Job 상태
* **ai_commentary** `MEDIUMTEXT` (NULL 가능; 완료 시 채움) — AI 해석 결과
* **version** `VARCHAR(16)` (예: `v0.2-kr`) — 해석 템플릿/모델 버전
* **locale** `VARCHAR(10)` — 해석 언어
* **created_at / updated_at** `DATETIME(3)`

**인덱스**

* `ux_interp_oracle (oracle_id)` — Oracle과 1:1 보장
* `idx_interp_status (status)` — Job 처리 상태 조회 최적화

> `interpretation`은 AI 해석 결과를 저장하는 별도 엔티티.
> - 즉시 응답 시에도 저장되며, 실패/재시도/다국어/버전 관리가 가능.
> - 구조적으로 `oracle`은 "점 자체", `interpretation`은 "그 점에 대한 해석"을 책임진다.


### 2.4 `hexagram`

* **id** `SMALLINT` (PK, 1..64)
* **name\_zh** `VARCHAR(8)` / **name\_ko** `VARCHAR(16)` / **name\_en** `VARCHAR(32)`
* **binary** `CHAR(6)` (하→상, 1=양/0=음)
* **upper\_trigram** `VARCHAR(4)` / **lower\_trigram** `VARCHAR(4)`
* **judgement** `TEXT`
* **image** `TEXT`

**인덱스**

* `ux_hexagram_no (id)`
* `idx_hexagram_binary (binary)`

---

### 2.5 `hexagram_line`

* **id** `BIGINT` (PK)
* **hexagram\_id** `SMALLINT` (FK → hexagram.id)
* **line\_index** `TINYINT` (1..6)
* **text\_zh / text\_ko / text\_en** `TEXT`

**인덱스**

* `ux_hexagram_line (hexagram_id, line_index)` (UNIQUE)

---

## 3) 도메인 ↔ 스키마 매핑

| 도메인(03)                                                                                                         | 스키마(06)                                                                                                                                     |
| --------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| `Oracle(id, question, lines, originalHexagram, changedHexagram, movingLineIndexes, createdAt, locale, userId?)` | `oracle(id, user_id, question, lines_json, lines_bits, moving_count, original_hexagram_id, changed_hexagram_id, locale, created_at)`        |
| `Interpretation(id, oracleId, judgement, image, line_index, line_text, sources, aiCommentary, locale, version)` | `interpretation(id, oracle_id, status, ai_commentary, fallback_commentary, version, locale, created_at, updated_at)` + (사전은 `hexagram*` 참조) |
| `Hexagram(no, name, trigrams, judgement, image)`                                                                | `hexagram`                                                                                                                                  |
| `Hexagram.lines[1..6]`                                                                                          | `hexagram_line(hexagram_id, line_index, text_*)`                                                                                            |

> 주: `judgement/image/line_text`의 정적 문구는 사전(`hexagram*`)에서 조합해 응답에 포함한다(04-API §4).

---

## 4) 쿼리 & 접근 패턴

### 4.1 내 히스토리(커서 페이지네이션)

```sql
-- 최신순 20건
SELECT id, question, original_hexagram_id, changed_hexagram_id, created_at
FROM oracle
WHERE user_id = :userId
  AND created_at < :cursorCreatedAt -- 첫 페이지면 NOW()
ORDER BY created_at DESC
LIMIT 20;
```

### 4.2 특정 괘의 해석 불러오기

```sql
SELECT h.id AS no, h.name_ko, h.binary, h.upper_trigram, h.lower_trigram,
       h.judgement, h.image,
       l.line_index, l.text_ko
FROM hexagram h
LEFT JOIN hexagram_line l ON l.hexagram_id = h.id
WHERE h.id = :no
ORDER BY l.line_index ASC;
```

### 4.3 통계 예시(가장 많이 나온 본괘 TOP N)

```sql
SELECT original_hexagram_id AS no, COUNT(*) AS cnt
FROM oracle
GROUP BY original_hexagram_id
ORDER BY cnt DESC
LIMIT 10;
```

---

## 5) 확장/옵션 테이블 제안 (필요 시)

### 5.1 게스트 세션(익명 토큰 저장형이 필요할 때)

```sql
CREATE TABLE guest_token (
  id BINARY(16) PRIMARY KEY,
  token_hash VARBINARY(32) NOT NULL, -- SHA-256 등
  expires_at DATETIME(3) NOT NULL,
  merged_user_id BINARY(16) NULL,    -- 로그인 전환 시 연결
  created_at DATETIME(3) NOT NULL
);
-- oracle.user_id IS NULL AND client 요청 토큰 해시로 소유자 판별
```

### 5.2 멱등성 키(중복 생성 방지)

```sql
CREATE TABLE idempotency (
  key_hash VARBINARY(32) PRIMARY KEY,
  owner_id BINARY(16) NULL,
  request_fingerprint VARBINARY(64) NOT NULL,
  response_snapshot MEDIUMBLOB NULL,
  created_at DATETIME(3) NOT NULL
);
```

### 5.3 해석 잡 분리형(고부하/비동기 강화)

```sql
CREATE TABLE interpretation_job (
  id BINARY(16) PRIMARY KEY,
  oracle_id BINARY(16) NOT NULL,
  status ENUM('PENDING','IN_PROGRESS','COMPLETED','FAILED') NOT NULL,
  attempts TINYINT UNSIGNED NOT NULL DEFAULT 0,
  last_error TEXT NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY ux_job_oracle(oracle_id)
);
```

---

## 6) 마이그레이션 가이드라인 (Flyway)

* `V1__schema.sql`: core 테이블(`user`, `hexagram`, `hexagram_line`, `oracle`, `interpretation`)
* `V2__seed_hexagrams.sql`: 64괘/효사 정적 데이터 시드
* `V3__indexes.sql`: 접근 패턴 확정 후 보강 인덱스
* `V4__i18n.sql`: 다국어 컬럼 추가/조정 필요 시

> 시드는 **트랜잭션**으로 전체 64괘와 효사(6×64)를 일괄 삽입. `hexagram.binary`/`trigram` 값은 도메인 매핑 테이블과 동기화.
> 
> DB 스키마와 고정 데이터를 단계별로 버전 관리해서, 개발·운영 환경 어디서든 동일하게 재현 가능하게 하자

---

## 7) 보안/무결성/성능 메모

* 개인정보: `user` 외 테이블에 이메일 등 PII 저장 금지
* 대량 텍스트: `ai_commentary`는 MEDIUMTEXT로 설정(최대 \~16MB)
* 인덱스: 히스토리 조회는 `(user_id, created_at DESC)`가 핵심. 나머지는 고빈도 쿼리 확인 후 추가
* 체크: MySQL CHECK 제약은 애플리케이션 검증으로 보완(테스트 케이스 필수)
* 캐시: 사전(`hexagram*`)은 애플리케이션 캐시/ETag로 고속화(04-API §8)

---

## 8) 오픈 이슈

* [ ] `lines_bits`를 생성 컬럼으로 둘지(함수 계산) vs. 애플리케이션에서 채울지
* [ ] `interpretation`과 별개로 잡 테이블을 둘지(§5.3), MVP 단계에서는 현 구조 유지 권장
* [ ] `locale`의 정규화(별도 테이블) 필요성

---

### 부록 A) 컬럼 규칙(요약)

* `lines_json`: 길이=6, 값∈{6,7,8,9}
* `lines_bits`: 길이=6, 문자∈{0,1} (하→상)
* `moving_count=0`이면 `changed_hexagram_id IS NULL`
* 모든 시간은 UTC(`DATETIME(3)`)

> 본 문서 변경 시 커밋 메시지 타입은 **\[DOCS]** 를 사용한다. API/도메인 변경과 연동되는 경우 **03-domain-model.md** 및 **04-api-spec.md**를 함께 갱신한다.
