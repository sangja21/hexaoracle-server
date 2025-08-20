# HexaOracle API Spec (Draft v0.2)

> Scope: 인증(회원가입/로그인/비회원 토큰), 오라클(점괘) API, MyPage 히스토리, 64괘 사전.

---

## 0) 공통 규약

* **Base URL**: `/api/v1`
* **Content-Type**: `application/json; charset=utf-8`
* **인증**: `Authorization: Bearer <JWT>` (비회원 토큰 포함)
* **시간/타임존**: 모든 시간은 ISO8601 UTC 표기 (`2025-08-20T07:20:00Z`)
* **로케일**: `Accept-Language: ko | en` 또는 `?locale=ko|en` (응답 텍스트/사전 번역)
* **페이지네이션**: cursor 기반

    * 요청: `?limit=20&cursor=eyJwYWdlIjoyfQ==`
    * 응답: `{"items": [...], "nextCursor": "..."}`
* **정렬**: 기본 `-createdAt` (최신 우선)
* **Idempotency**: 쓰기 요청에 `Idempotency-Key: <uuid>` 지원 (중복 생성 방지)
* **Rate Limit**: 기본 100 req/min (Guest 30 req/min). 초과 시 `429`와 `Retry-After` 헤더
* **감사/추적**: `X-Request-Id` 수용, 서버는 모든 쓰기 요청을 감사 로그에 기록(누가/언제/무엇을)
* **에러 포맷**:

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "lines must contain 6 integers in [6,9]",
    "details": [{"field": "lines", "reason": "invalid_length"}]
  }
}
```

* **표준 상태코드**: 200, 201, 204, 400, 401, 403, 404, 409, 422, 429, 500

---

## 1) 인증 / Auth

### 1.1 회원가입

**POST** `/auth/signup`

* 설명: 이메일/패스워드 기반 회원 가입.
* 요청 바디

```json
{
  "email": "user@example.com",
  "password": "P@ssw0rd!",
  "displayName": "June"
}
```

* 응답 `201 Created`

```json
{
  "user": {
    "id": "usr_01HXX...",
    "email": "user@example.com",
    "displayName": "June",
    "createdAt": "2025-08-20T07:20:00Z"
  }
}
```

* 유효성: email 형식, password 최소 8자(대소문자/숫자/특수문자 중 3종 조합)
* 에러: `409 EMAIL_EXISTS`, `422 WEAK_PASSWORD`

### 1.2 로그인 (JWT 토큰 발급)

**POST** `/auth/login`

* 요청

```json
{ "email": "user@example.com", "password": "P@ssw0rd!" }
```

* 응답 `200 OK`

```json
{
  "accessToken": "<jwt>",
  "refreshToken": "<jwt>",
  "expiresIn": 3600,
  "user": {
    "id": "usr_01HXX...",
    "email": "user@example.com",
    "displayName": "June"
  }
}
```

* 에러: `401 INVALID_CREDENTIALS`

### 1.3 토큰 갱신

**POST** `/auth/refresh`

* 요청

```json
{ "refreshToken": "<jwt>" }
```

* 응답

```json
{ "accessToken": "<jwt>", "expiresIn": 3600 }
```

* 에러: `401 INVALID_TOKEN`

### 1.4 로그아웃

**POST** `/auth/logout`

* 헤더: `Authorization: Bearer <access>`
* 요청 바디(옵션)

```json
{ "refreshToken": "<jwt>" }
```

* 응답 `204 No Content`

### 1.5 내 프로필 조회

**GET** `/me`

* 설명: 현재 토큰의 사용자 정보
* 응답

```json
{ "id": "usr_01HXX...", "email": "user@example.com", "displayName": "June", "locale": "ko" }
```

---

## 2) 오라클 / Divinations (a.k.a. Oracles)

> 입력은 **6효(lines)** 또는 **동전 원본(coinTosses)**. 결과는 **원괘/지괘**, **변효**, **해석**.

### 데이터 모델

* **Line 값(정수)**: 6=노음(변), 7=소양, 8=소음, 9=노양(변)
* **인덱스**: 1(하효, bottom) → 6(상효, top)
* **coinTosses** 인코딩: 각 효마다 3-coin, `H=3`, `T=2`, 합계가 6\~9

```json
"coinTosses": [ [3,2,2], [3,3,2], [2,2,2], [3,3,3], [3,2,3], [2,3,2] ]
```

* **movingLineIndexes**: `[1,4,6]` (1-based)

### 2.1 점괘 생성 (Oracle Cast)

**POST** `/oracles`

* 헤더:
    * `Authorization` (회원 필수)
    * `Idempotency-Key` 권장

* 요청 (둘 중 하나 제공 필수)
```json
{
  "question": "다음 분기 제품 출시 시기를 정해도 될까?",
  "lines": [7,8,9,7,8,8],
  "locale": "ko-KR"
}
```

* 동작 규칙
    * `lines` 제공 시: 바로 계산
* 응답 `201 Created`

```json
{
  "oracleId": "a6f9...",
  "chosen": {
    "type": "original",
    "lines": [7,8,9,7,8,8],
    "upper": "☳",
    "lower": "☵",
    "kingWenNo": 40,
    "movingLineIndexes": [3]
  },
  "interpretation": {
    "judgement": "규율을 세우되 지나친 경직을 경계하라.",
    "image": "호수 위의 물…",
    "line_index": 3,
    "line_text": "지나침과 부족함 사이의 균형을 경계하라.",
    "sources": ["Korean-Classic-v1"],
    "aiCommentary": "당신이 처한 상황은 억눌림보다는 해소가 필요한 때입니다. 규칙을 지키되 지나친 억제는 피하고, 사람들과의 관계에서 열린 태도를 유지하는 것이 좋습니다."
  },
  "createdAt": "2025-08-20T07:00:00Z"
}
```

* 검증

    * `lines.length == 6`, 모든 원소 ∈ {6,7,8,9}
    * `coinTosses.length == 6`, 각 내부배열 길이 3, 합계 ∈ {6,7,8,9}
* 에러: `422 INVALID_LINES`, `422 INVALID_TOSSES`


## 3) MyPage / History

> 로그인 사용자의 저장된 점괘 히스토리.

### 3.1 내 히스토리 목록

**GET** `/me/oracles`

* 쿼리: `?from=2025-08-01&to=2025-08-20&hexagram=2&limit=20&cursor=...`
* 필터
    * `from`, `to`: 생성일 기간
    * `hexagram`: 원괘 번호(1..64) 또는 `changed=23`
    * `q`: 메모/태그 텍스트 검색

* 응답
```json
{
  "items": [
    {
      "id": "orc_01J0...",
      "question": "다음 분기 제품 출시 시기를 정해도 될까?",
      "summary": {
        "original": {"hexagramNo": 2, "name": {"ko": "곤"}},
        "changed": {"hexagramNo": 23, "name": {"ko": "박"}}
      },
      "createdAt": "2025-08-20T07:20:00Z"
    }
  ],
  "nextCursor": null
}

```

### 3.2 내 히스토리 단건 조회

**GET** `/me/oracles/{id}`

* 설명: 내 소유의 점괘만 접근 가능
* 응답: 2.1와 동일 스키마

### 3.3 (옵션) 히스토리 삭제

**DELETE** `/me/oracles/{id}` → `204`

---

## 4) 64괘 사전 / Hexagram Dictionary

> 정적 리소스. 강력 캐싱(ETag) 가능.

### 4.1 목록 검색

**GET** `/hexagrams`

* 쿼리

    * `q`: 이름/별칭 풀텍스트 (예: 곤, 坤, Kun)
    * `no`: 번호(1..64) 다중 가능 `?no=1,2,3`
    * `binary`: 6자리 이진(하→상, 예: `111111`)
    * `trigrams`: 상괘/하괘 기호 (예: `?upper=☰&lower=☷` 또는 코드 `upper=qian&lower=kun`)
    * `locale`: `ko|en`
* 응답(축약 리스트)

```json
{
  "items": [
    {"no":1, "name": {"zh":"乾","ko":"건","en":"Qian"}, "binary":"111111"},
    {"no":2, "name": {"zh":"坤","ko":"곤","en":"Kun"}, "binary":"000000"}
  ]
}
```

### 4.2 단건 조회

**GET** `/hexagrams/{no}`

* 응답

```json
{
  "no": 23,
  "name": {"zh":"剝","ko":"박","en":"Bo"},
  "aliases": ["Peeling", "Splitting Apart"],
  "binary": "000001",
  "trigrams": {"upper":"☶","lower":"☷"},
  "judgement": "...",
  "image": "...",
  "lines": [
    {"line":1, "text":"..."},
    {"line":2, "text":"..."}
  ]
}
```

---

## 5) 보안/권한 모델

* **roles**: `guest`, `user`, `(admin)`
* **scopes**

    * `oracle:create`, `oracle:read`, `oracle:interpret`, `history:read`, `history:write`, `dict:read`
* **게스트 제한**

    * 허용: `oracle:create`, `oracle:interpret`, `dict:read`
    * 불가: `history:write` (persist=false 강제)

### JWT 클레임(예시)

```json
{
  "sub": "usr_01HXX...",
  "role": "user",
  "scope": ["oracle:create","oracle:read","history:read","history:write","dict:read"],
  "iat": 1724140800,
  "exp": 1724144400
}
```

---

## 6) 밸리데이션 규칙 요약

* `lines` 정확히 6개, 각 값 ∈ {6,7,8,9}

---

## 7) 예시 시나리오 (Flow, Minimal Spec)

### 1. 회원 가입 & 로그인
1. `POST /auth/signup`
   → 이메일 + 패스워드로 계정 생성 (이메일 인증 없음)
2. `POST /auth/login`
   → JWT 발급 (`accessToken`, `refreshToken`)

---

### 2. 점괘 생성 & 히스토리
1. `POST /oracles`
    - 입력: `lines`
    - 응답: `oracleId`, 본괘/변괘, 효, 생성 시각
2. `GET /me/oracles`
    - 내 히스토리 전체 조회 (cursor 기반 페이지네이션)
3. `GET /me/oracles/{oracleId}?include=interpretation`
    - 특정 점괘 상세 + 해석 포함

---

### 3. 64괘 사전 탐색
1. `GET /hexagrams?q=곤`
    - 이름/번호/팔괘 조합으로 검색 가능
2. `GET /hexagrams/{hexagramId}`
    - 특정 괘 상세 보기 (괘사, 효사, 이미지, 해석 소스 등)


## 8) 성능/캐싱

* `/hexagrams/**`: `Cache-Control: public, max-age=86400`, `ETag` 응답
* `/divinations/{id}`: `ETag/If-None-Match` 지원 (개인 리소스이므로 public cache 금지)

---

## 9) 운영/기타

* 헬스체크: `GET /health` → `{ "status": "ok", "version": "2025.08.20" }`
* 버전: `GET /version` → git sha, 빌드타임
* OpenAPI 문서: `GET /openapi.json` (자동 생성)

---

## 10) 용어 정리

* **원괘(original)**: 변효 적용 전의 괘
* **지괘/변괘(changed)**: 변효 적용 후의 괘
* **변효(moving line)**: 값 6 또는 9
* **상/이미지(Image)**: 괘상에 대한 비유적 설명
* **판정(Judgement)**: 괘의 총평

---

### 부록 A) 계산 규칙(요약)

* `binary` 표기: 하효→상효 순서 6자리(`1=양`, `0=음`)
* 변효 반전: 6(음→양), 9(양→음), 7/8은 고정
* movingLineIndexes: 값이 6 또는 9인 위치의 1-based 인덱스

### 부록 B) 에러 코드 테이블(예시)

| code                 | http | 의미           |
| -------------------- | ---: | ------------ |
| INVALID\_CREDENTIALS |  401 | 이메일/패스워드 불일치 |
| EMAIL\_EXISTS        |  409 | 이미 가입된 이메일   |
| WEAK\_PASSWORD       |  422 | 비밀번호 정책 위반   |
| INVALID\_LINES       |  422 | 6효 형식 오류     |
| INVALID\_TOSSES      |  422 | 동전 원본 형식 오류  |
| NOT\_OWNER           |  403 | 소유자 아님       |
| RATE\_LIMITED        |  429 | 호출 제한 초과     |

---

> 이 문서는 **제품 API 아키텍처 초안**으로, 이후 실제 OpenAPI 스키마와 동기화 예정.
