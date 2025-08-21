# 06. ERD — HexaOracle (Draft, Rev. 2025‑08‑22)

> 목적: 도메인 모델(03)과 API 스펙(04)을 **관계형 스키마**로 구체화한다. 본 문서는 MySQL 8.x 기준 초안이며, MVP 개발 이후 마이그레이션(플라이웨이)으로 진화시킨다.

* 범위: 회원/인증(요약), 오라클(점괘) 기록, AI 해석, 64괘 사전
* 원칙:

  * 동전 던지기(coin)는 **프론트 전용**. 서버는 **lines(6,7,8,9)** 배열만 수신/저장
  * 도메인 규칙은 애플리케이션에서 검증, DB는 무결성/참조/인덱싱에 집중
  * 히스토리는 `Oracle` 자체가 원장(ledger)
  * 국제화는 `locale` 컬럼(ko|en) + 사전 테이블의 다국어 컬럼으로 처리

---

## 0) 키/ID 전략 (MVP)

* **전략**: PK는 **`BIGINT AUTO_INCREMENT`** 로 통일한다. (모든 core/확장 테이블 일관)
* **이유**: 단순성/인덱스 크기/ORM 매핑 용이. 향후 분산/샤딩 필요 시 Snowflake/ULID로 전환 가능(마이그레이션 ADR 별도).
* **노출 규칙**: 외부 API 응답에서는 `string` 으로 직렬화(넘침/JS 정밀도 이슈 회피).
* **대안(참고)**: UUID(`BINARY(16)`) 채택 시 인덱스/정렬에 주의. 본 문서는 BIGINT 기준으로 명시한다.

---

## 1) ER 다이어그램

(그림 생략)

---

## 2) 테이블 정의 (초안)

### 2.1 `user`

* **id** `BIGINT AUTO_INCREMENT` (PK)
* **email** `VARCHAR(255)`
* **email\_lc** `VARCHAR(255)` **GENERATED ALWAYS AS** `(LOWER(email))` **STORED**
* **password\_hash** `VARCHAR(255)`
* **display\_name** `VARCHAR(50)`
* **role** `ENUM('user','admin')` DEFAULT 'user'
* **created\_at / updated\_at** `DATETIME(3)` (자동 타임스탬프)

**인덱스**

* `UNIQUE KEY ux_user_email_lc (email_lc)` — 이메일 대소문자 무시 고유
* (대안) MySQL 8.0 **표현식 인덱스**: `CREATE UNIQUE INDEX ux_user_email_lc ON user ((LOWER(email)));`

**메모**

* 저장 시 애플리케이션 레벨에서도 **lowercase 정규화** 수행 권장.
* 패스워드는 평문 저장 금지. 해시 정책은 §7 참조.

---

### 2.2 `oracle`

* **id** `BIGINT AUTO_INCREMENT` (PK)
* **user\_id** `BIGINT` (FK → user.id, NULL 허용)
* **question** `TEXT` — 사용자가 입력한 질문
* **lines\_json** `JSON` (예: `[6,7,8,9,7,6]`) — 원본 효 배열(하→상)
* **lines\_bits** `CHAR(6)` **GENERATED ALWAYS AS** (
  `CONCAT(`
  `IF(JSON_EXTRACT(lines_json,'$[0]') IN (7,9),'1','0'),`
  `IF(JSON_EXTRACT(lines_json,'$[1]') IN (7,9),'1','0'),`
  `IF(JSON_EXTRACT(lines_json,'$[2]') IN (7,9),'1','0'),`
  `IF(JSON_EXTRACT(lines_json,'$[3]') IN (7,9),'1','0'),`
  `IF(JSON_EXTRACT(lines_json,'$[4]') IN (7,9),'1','0'),`
  `IF(JSON_EXTRACT(lines_json,'$[5]') IN (7,9),'1','0')`)) **STORED**
  — `1=양(7,9)`, `0=음(6,8)`
* **moving\_count** `TINYINT UNSIGNED` **GENERATED ALWAYS AS** (
  `IF(JSON_EXTRACT(lines_json,'$[0]') IN (6,9),1,0)`
  `+ IF(JSON_EXTRACT(lines_json,'$[1]') IN (6,9),1,0)`
  `+ IF(JSON_EXTRACT(lines_json,'$[2]') IN (6,9),1,0)`
  `+ IF(JSON_EXTRACT(lines_json,'$[3]') IN (6,9),1,0)`
  `+ IF(JSON_EXTRACT(lines_json,'$[4]') IN (6,9),1,0)`
  `+ IF(JSON_EXTRACT(lines_json,'$[5]') IN (6,9),1,0)`
  ) **STORED**
* **original\_hexagram\_id** `SMALLINT` (FK → hexagram.id)
* **changed\_hexagram\_id** `SMALLINT NULL` (FK → hexagram.id)
* **locale** `VARCHAR(10)` (예: `ko-KR`, `en-US`)
* **created\_at** `DATETIME(3)` (기본 NOW)

**인덱스**

* `KEY idx_oracle_owner_created_id (user_id, created_at DESC, id DESC)` — **tie-breaker** 포함 페이지네이션
* `KEY idx_oracle_original (original_hexagram_id)`
* `KEY idx_oracle_changed (changed_hexagram_id)`
* `KEY idx_oracle_bits (lines_bits)` — 바이너리 패턴 검색/통계

**무결성 규칙**

* `JSON_TYPE(lines_json) = 'ARRAY' AND JSON_LENGTH(lines_json) = 6`
* `moving_count = COUNT(value ∈ {6,9})` (생성 컬럼으로 자동 계산)
* `moving_count = 0 ⇒ changed_hexagram_id IS NULL`

> `oracle`은 점을 친 **행위(질문+결과 괘)** 자체만 기록하는 불변 원장.
> 해석은 별도 테이블(`interpretation`)로 분리하여 버전/언어/상태 관리.

---

### 2.3 `interpretation`

* **id** `BIGINT AUTO_INCREMENT` (PK)
* **oracle\_id** `BIGINT` (FK → oracle.id, UNIQUE) — 1:1 연결
* **status** `ENUM('PENDING','IN_PROGRESS','COMPLETED','FAILED')` — AI Job 상태
* **ai\_commentary** `MEDIUMTEXT` (NULL 가능; 완료 시 채움) — AI 해석 결과
* **version** `VARCHAR(16)` (예: `v0.2-kr`) — 해석 템플릿/모델 버전
* **locale** `VARCHAR(10)` — 해석 언어
* **created\_at / updated\_at** `DATETIME(3)`

**인덱스**

* `UNIQUE KEY ux_interp_oracle (oracle_id)` — Oracle과 1:1 보장
* `KEY idx_interp_status (status)` — Job 처리 상태 조회 최적화

**메모**

* **fallback\_commentary는 저장하지 않는다.** LLM 실패 시 사전 텍스트 조합으로 **일시 응답**(ephemeral)만 제공(08-security-privacy §12 일치).

---

### 2.4 `hexagram`

* **id** `SMALLINT` (PK, 1..64)
* **name\_zh** `VARCHAR(8)` / **name\_ko** `VARCHAR(16)` / **name\_en** `VARCHAR(32)`
* **binary** `CHAR(6)` (하→상, 1=양/0=음)
* **upper\_trigram** `VARCHAR(4)` / **lower\_trigram** `VARCHAR(4)`
* **judgement** `TEXT`
* **image** `TEXT`

**인덱스**

* `UNIQUE KEY ux_hexagram_no (id)`
* `KEY idx_hexagram_binary (binary)`

---

### 2.5 `hexagram_line`

* **id** `BIGINT` (PK)
* **hexagram\_id** `SMALLINT` (FK → hexagram.id)
* **line\_index** `TINYINT` (1..6)
* **text\_zh / text\_ko / text\_en** `TEXT`

**인덱스**

* `UNIQUE KEY ux_hexagram_line (hexagram_id, line_index)`

---

## 3) 도메인 ↔ 스키마 매핑

| 도메인(03)                                                                                                         | 스키마(06)                                                                                                                              |
| --------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------ |
| `Oracle(id, question, lines, originalHexagram, changedHexagram, movingLineIndexes, createdAt, locale, userId?)` | `oracle(id, user_id, question, lines_json, lines_bits, moving_count, original_hexagram_id, changed_hexagram_id, locale, created_at)` |
| `Interpretation(id, oracleId, judgement, image, line_index, line_text, sources, aiCommentary, locale, version)` | `interpretation(id, oracle_id, status, ai_commentary, version, locale, created_at, updated_at)` + (사전은 `hexagram*` 참조)               |
| `Hexagram(no, name, trigrams, judgement, image)`                                                                | `hexagram`                                                                                                                           |
| `Hexagram.lines[1..6]`                                                                                          | `hexagram_line(hexagram_id, line_index, text_*)`                                                                                     |

> 주: `judgement/image/line_text`의 정적 문구는 사전(`hexagram*`)에서 조합해 응답에 포함한다(04-API §4).

---

## 4) 쿼리 & 접근 패턴

### 4.1 내 히스토리(커서 페이지네이션, 안정화)

```sql
-- 첫 페이지: 최신순 20건
SELECT id, question, original_hexagram_id, changed_hexagram_id, created_at
FROM oracle
WHERE user_id = :userId
ORDER BY created_at DESC, id DESC
LIMIT 20;

-- 다음 페이지: tie-breaker 포함 커서
-- cursor = {createdAt, id}
SELECT id, question, original_hexagram_id, changed_hexagram_id, created_at
FROM oracle
WHERE user_id = :userId
  AND (
    created_at < :cursorCreatedAt
    OR (created_at = :cursorCreatedAt AND id < :cursorId)
  )
ORDER BY created_at DESC, id DESC
LIMIT 20;
```

**인덱스 권장**

* `KEY idx_oracle_owner_created_id (user_id, created_at DESC, id DESC)`

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
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  token_hash VARBINARY(32) NOT NULL, -- SHA-256 등
  expires_at DATETIME(3) NOT NULL,
  merged_user_id BIGINT NULL,        -- 로그인 전환 시 연결 (FK → user.id)
  created_at DATETIME(3) NOT NULL,
  KEY idx_guest_token_exp (expires_at)
);
```

### 5.2 멱등성 키(중복 생성 방지)

```sql
CREATE TABLE idempotency (
  key_hash VARBINARY(32) PRIMARY KEY,
  owner_id BIGINT NULL,
  request_fingerprint VARBINARY(64) NOT NULL,
  response_snapshot MEDIUMBLOB NULL,
  created_at DATETIME(3) NOT NULL
);
```

### 5.3 해석 잡 분리형(고부하/비동기 강화)

```sql
CREATE TABLE interpretation_job (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  oracle_id BIGINT NOT NULL,
  status ENUM('PENDING','IN_PROGRESS','COMPLETED','FAILED') NOT NULL,
  attempts TINYINT UNSIGNED NOT NULL DEFAULT 0,
  last_error TEXT NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY ux_job_oracle(oracle_id)
);
```

---

## 6) FK 정책 (권장값)

| 테이블                  | FK 컬럼                  | 참조            | ON DELETE    | ON UPDATE | 비고                  |
| -------------------- | ---------------------- | ------------- | ------------ | --------- | ------------------- |
| `oracle`             | `user_id`              | `user.id`     | **SET NULL** | CASCADE   | 유저 삭제 시 기록은 남기되 익명화 |
| `oracle`             | `original_hexagram_id` | `hexagram.id` | **RESTRICT** | CASCADE   | 사전 데이터는 고정, 삭제 금지   |
| `oracle`             | `changed_hexagram_id`  | `hexagram.id` | **RESTRICT** | CASCADE   | 〃                   |
| `interpretation`     | `oracle_id`            | `oracle.id`   | **CASCADE**  | CASCADE   | 오라클 삭제 시 해석도 함께 삭제  |
| `hexagram_line`      | `hexagram_id`          | `hexagram.id` | **RESTRICT** | CASCADE   | 64괘 사전 무결성 유지       |
| `guest_token`        | `merged_user_id`       | `user.id`     | **SET NULL** | CASCADE   | 익명→유저 전환 링크         |
| `interpretation_job` | `oracle_id`            | `oracle.id`   | **CASCADE**  | CASCADE   | 잡/결과 동기화            |

> 실제 DDL에는 FK 및 정책을 명시하고, Flyway 마이그레이션에 반영한다.

---

## 7) 마이그레이션 & 시드 전략 (Flyway)

* `V1__schema.sql`: core 테이블(`user`, `hexagram`, `hexagram_line`, `oracle`, `interpretation`)
* `V2__seed_hexagrams.sql`: 64괘/효사 정적 데이터 시드
* `V3__indexes.sql`: 접근 패턴 확정 후 보강 인덱스
* `V4__i18n.sql`: 다국어 컬럼 추가/조정 필요 시

**대량 삽입 권장**

* 트랜잭션 크기 조절(예: 1,000행 단위 배치) 및 타임아웃 설정.
* 예시 — **batched INSERT**:

```sql
START TRANSACTION;
INSERT INTO hexagram (id,name_zh,name_ko,name_en,binary,upper_trigram,lower_trigram,judgement,image) VALUES
  (1,'乾','건','Qian','111111','☰','☰','...','...'),
  (2,'坤','곤','Kun','000000','☷','☷','...','...')
  -- ...
;
-- 6×64 = 384행은 100~200행 단위로 나눠 커밋 권장
COMMIT;
```

* 예시 — **LOAD DATA** (환경 허용 시):

```sql
LOAD DATA LOCAL INFILE '/path/hexagram_line_ko.csv'
INTO TABLE hexagram_line
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
(hexagram_id, line_index, text_ko);
```

---

## 8) 보안/무결성/성능 메모

* **비밀번호 해시**: `Argon2id` 권장(메모리/시간 비용 조절), 대안으로 `bcrypt(12+)`. 해시는 **솔트 + (옵션) pepper** 적용.
* **PII 최소화**: `user` 외 테이블에 이메일 등 PII 저장 금지.
* **이메일 고유 제약**: `email_lc` 고유 인덱스 + 앱단 lowercase 저장.
* **대량 텍스트**: `ai_commentary`는 MEDIUMTEXT(최대 \~16MB).
* **인덱스**: 히스토리는 `(user_id, created_at, id)`가 핵심. 나머지는 접근 패턴 보고 보강.
* **CHECK**: MySQL 8+에서 `CHECK` 유효. 단, JSON 스키마 검증은 애플리케이션/테스트로 보완.
* **캐시**: 사전(`hexagram*`)은 앱 캐시/ETag로 고속화(04-API §8).

---

## 9) 오픈 이슈

* [ ] `lines_json` → 생성 컬럼 계산식의 성능/프로파일링(대량 쓰기 시 트리거 vs 앱계산 비교)
* [ ] 향후 ID 전략을 Snowflake/ULID로 전환할 경우 마이그레이션 계획(키 폭·FK 동기)
* [ ] `locale` 정규화(별도 테이블) 필요성 재검토

---

### 부록 A) 컬럼 규칙(요약)

* `lines_json`: 길이=6, 값∈{6,7,8,9}
* `lines_bits`: 길이=6, 문자∈{0,1} (하→상, 1=양/0=음)
* `moving_count=0`이면 `changed_hexagram_id IS NULL`
* 모든 시간은 UTC(`DATETIME(3)`)

> 본 문서 변경 시 커밋 타입은 **\[DOCS]** 를 사용한다. API/도메인 변경과 연동되는 경우 **03-domain-model.md** 및 **04-api-spec.md**를 함께 갱신한다.
