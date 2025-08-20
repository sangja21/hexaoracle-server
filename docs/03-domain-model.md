# HexaOracle 도메인 모델 (03-domain-model.md)

> 목적: **주역(역경) 점복 절차**를 소프트웨어 도메인으로 모델링한다. 던진 동전/효 → 괘 생성 → 변괘 계산 → 해석(Interpretation)까지를 한 번의 점(Oracle)으로 추상화한다.

---

## 1) 용어 정의 (Ubiquitous Language)

* **효(Line, 爻)**: 한 줄의 음/양. 동전법에서는 한 효가 `{6,7,8,9}` 중 하나의 값으로 표현된다.

    * `6 = 노음(old yin, 변하는 음)` → _ _ 
    * `7 = 소양(young yang, 변하지 않는 양)` → __
    * `8 = 소음(young yin, 변하지 않는 음)` → _ _
    * `9 = 노양(old yang, 변하는 양)` → __
    * 표기 방향: **아래(초효)** → **위(상효)** 순서로 6개가 쌓여 하나의 괘를 이룸.
* **괘(Hexagram, 卦)**: 6개의 효로 구성된 도형. 상괘(윗 삼효)와 하괘(아랫 삼효)로 나뉜다.

    * **본괘(Original Hexagram)**: 변화를 적용하기 전의 6효가 만든 괘.
    * **변효(Moving Line)**: 값이 `6` 또는 `9`인 효. 변화의 대상.
    * **변괘(Changed Hexagram)**: 변효를 뒤집어(음↔양) 얻는 2차 괘.
* **팔괘(Trigram)**: 3효로 구성된 기본 도형(건☰, 곤☷, 감☵, 이☲, 진☳, 손☴, 간☶, 태☱). 상괘/하괘를 각각 팔괘로 식별한다.
* **척전법(동전법)**: 3개의 동전을 던져 한 효를 생성하는 규칙.

    * 동전 3개 결과의 합(앞면=3, 뒷면=2): `6,7,8,9` 중 하나가 됨.

> **참고 표**
>
> | 합계 | 효  | 음/양 | 변여부 |
> | -: | -- | --- |-----|
> |  6 | 노음 | 음   | 변함  |
> |  7 | 소양 | 양   | 고정  |
> |  8 | 소음 | 음   | 고정  |
> |  9 | 노양 | 양   | 변함  |

---

## 2) 핵심 도메인 개체 (Entities & Value Objects)

### Aggregate: `Oracle` (Aggregate Root)

* **의미**: 하나의 점 행위(던지기부터 해석까지)의 수명주기를 캡슐화.
* **식별자**: `OracleId` (UUID 등)
* **상태**:

    * `question: Question` (VO) — 점의 물음/의도, 길이 제한 및 금칙어 필터 불변식 포함
    * `method: DivinationMethod` (enum: COIN, YARROW 등)
    * `throws: CoinTosses` (VO) — 6×(3동전) 원천 데이터, 감사 추적용
    * `lines: Lines` (VO) — 길이=6, 각 요소 ∈ {6,7,8,9}
    * `originalHexagram: Hexagram` (VO)
    * `changedHexagram: Hexagram?` (VO, 변효가 1개 이상일 때만 존재)
    * `movingLineIndexes: Set<1..6>` (1=초효, 6=상효)
    * `createdAt, locale, userId?`
* **행위**:

    * `cast()` — throws → lines 생성 (동전법 규칙 적용)
    * `buildOriginalHexagram()` — lines → hexagram
    * `buildChangedHexagram()` — moving line flip → hexagram
    * `summarize()` — 본괘/변괘/변효 요약 제공 (DTO 변환)

### `Hexagram` (Value Object)

* **구성**: `lines[6]` (bottom→top), `upper: Trigram`, `lower: Trigram`
* **식별**: King Wen 번호(1..64), 또는 팔괘쌍 `(upper, lower)` + 패턴 서명
* **불변식**: 길이=6, 요소는 음/양(0/1)로 정규화 가능

### `Line` (Value Object)

* **구성**: `value: 6|7|8|9`
* **도출 속성**: `isYang: boolean`, `isMoving: boolean`, `toChanged(): Line`

### `Interpretation` (Entity / 별도 Aggregate)

* **의미**: 텍스트 해석, 주석, 전통 문구(괘사/효사) 등을 보관
* **식별자**: `InterpretationId`
* **연관**: `oracleId` (또는 `hexagramId`), `locale`, `version`
* **상태**: `judgement`, `image`, `linesCommentary[1..6]`, `sources[]`
* **행위**: 해석 규칙/템플릿에 따라 `Oracle` 스냅샷을 받아 텍스트 생성

### 보조 VO

* `Question(text, tags)`
* `CoinToss(value: 2|3)` — 개별 동전 결과, 1효당 3개 묶음
* `CoinTosses(effects: 6 × 3)` — 원천 로그(감사)
* `Lines([Line×6])`
* `Trigram(code ∈ {乾,坤,坎,離,震,巽,艮,兌})`

---

## 3) 도메인 서비스

> 엔티티에 넣기엔 **규칙 중심**이거나 **외부 테이블** 의존이 큰 계산을 분리한다.

1. **`LineFactoryByCoin`**

    * 입력: 동전 3개(2 또는 3)의 합 → `{6,7,8,9}`
    * 규칙: 합계 6=노음, 7=소양, 8=소음, 9=노양
2. **`HexagramService`**

    * `from(lines: Lines): Hexagram`
    * 상괘/하괘 도출, King Wen 매핑 테이블 조회
3. **`ChangedHexagramService`**

    * 입력: 본괘 `lines`
    * 처리: `value ∈ {6,9}`인 인덱스만 뒤집어 새 `lines'` 생성 → `Hexagram`
4. **`InterpretationService`**

    * 입력: `OracleSnapshot`
    * 출력: `Interpretation`
    * 정책: 본괘 위주/변괘 우선/단일 변효 강조 등 전략 주입

---

## 4) 애그리거트 경계 & 불변식

### 경계

* **`Oracle`**: 점복 데이터의 생성·변형의 **유일한 변경 지점**

    * 내부에 `Lines`, `Hexagram`(VO)을 포함
    * `Interpretation`은 별도 애그리거트로, 생성 후에도 독립 관리(버전, 지역화)

### 불변식(예시)

* `Lines.length == 6` (항상 6효)
* 각 `Line.value ∈ {6,7,8,9}`
* `movingLineIndexes == { i | line[i] ∈ {6,9} }`
* `changedHexagram` 존재 조건: `movingLineIndexes.size() > 0`
* `Hexagram.upper == lines[4..6]`, `Hexagram.lower == lines[1..3]`
* `Question.text` 길이 ≤ 500자, 금칙어/PII 필터 통과
* `Oracle`의 `cast()` 이후에는 `throws`와 `lines`의 재작성 금지(감사 무결성)

> **도메인 이벤트(선택)**: `OracleCasted`, `HexagramsComputed`, `InterpretationAttached`

---

## 5) 규칙은 예시 케이스와 함께 (척전법 → 괘/변괘)

### 케이스 A: 단일 변효(9)

* 동전 합(초효→상효): `7, 8, 9, 7, 8, 8`
* 본괘 Lines: `[7,8,9,7,8,8]`

    * 변효: 3효(값 9)
* 변괘 Lines: `[7,8,**8**,7,8,8]` (3효 양→음으로 뒤집음)
* 효과: 본괘 효사 **3효**를 우선 고려, 변괘 전체 논의는 보조(전략에 따라 다름)

### 케이스 B: 복수 변효(6,9 혼재)

* 동전 합: `6, 7, 8, 9, 7, 6`
* 본괘: `[6,7,8,9,7,6]` → 변효 인덱스 = {1,4,6}
* 변괘: `[**7**,7,8,**8**,7,**7**]` (1효 음→양, 4효 양→음, 6효 음→양)
* 효과: 변효가 다수이면 변괘의 의미 비중을 높이는 전략 선택 가능

> King Wen 번호/팔괘 매핑은 별도의 테이블(리소스)로 유지한다. 구현에서는 `(upper, lower)` 조합으로 우선 식별하고, 필요한 경우 번호로 변환한다.

---

## 6) 리포지토리 & 트랜잭션 경계

* `OracleRepository`

    * `save(oracle)`, `findById(id)`
* `InterpretationRepository`

    * 지역화/버전 분리 보관, 롤백/감사추적 필수
* 트랜잭션: `cast → buildOriginal → buildChanged → persist`는 **하나의 트랜잭션**으로 커밋

---

## 7) DTO 설계 (응용 계층 전송용)

> 도메인 모델과 **분리**한다. DTO는 입·출력 요구에 맞춰 평탄화/식별자화한다.

### 요청 DTO

```json
POST /oracles
{
  "question": "다음 분기 제품 출시 시기를 정해도 될까?",
  "lines": [7,8,9,7,8,8],
  "locale": "ko-KR"
}

```

### 응답 DTO

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

> 주: `upper/lower`는 예시. 실제 번호/팔괘 매핑은 구현 테이블 기준으로 채움.
> - 괘의 효(爻)는 **밑에서부터 위로** 쌓아 올린다. (1효=초효, 6효=상효)
> - **하괘(lower)** = 1효~3효 (아래 세 줄), **상괘(upper)** = 4효~6효 (위 세 줄)
> - 괘 이름은 전통적으로 **상괘 → 하괘 순서**로 읽는다.
    >   - 예: lower=☵(水), upper=☶(山) ⇒ 山水蒙(산수몽, 4번 괘)
> - `kingWenNo`는 문왕괘서(King Wen sequence)의 번호(1~64)로,  
    >   (상괘, 하괘) 조합을 매핑 테이블로 조회하여 얻는다.
> - 구현에서는 (upper, lower) → `kingWenNo` → `괘명/괘사/효사` 로 이어지는  
    >   리소스 테이블을 활용한다.

---

## 8) 간단 설계 다이어그램 (텍스트 UML)

```declarative
[Oracle] <AggregateRoot>
 - id: OracleId
 - question: Question
 - lines: Lines
 - chosenHexagram: Hexagram       // 본괘 or 지괘 중 최종 선택된 괘
 - movingLineIndexes: Set<int>    // 변효 위치
 - createdAt: DateTime
 + buildChosenHexagram(): Hexagram
 + interpret(): Interpretation

[Hexagram] <VO>
 - lines[6]         // 6개 효 (아래→위)
 - upper: Trigram   // 상괘
 - lower: Trigram   // 하괘
 - kingWenNo: int   // 문왕순서 번호

[Line] <VO>
 - value: 6|7|8|9
 + isYang(): bool
 + isMoving(): bool
 + toChanged(): Line

[Interpretation] <Entity>
- id: InterpretationId
- oracleId: OracleId
- judgement: string
- image: string
- line_index: int          // 선택된 변효 위치 (없으면 null)
- line_text: string        // 해당 효사 해석
- sources: [string]        // 해석 출처
- locale: string
- version: string
- aiCommentary: string     // AI 해설
+ render(): InterpretationText

```

---

## 9) 검증 시나리오 (Acceptance Examples)

* **AC1 – 6효 고정**: 사용자가 COIN 방식으로 점을 치면 `Lines.length == 6`을 보장한다.
* **AC2 – 변효 식별**: 입력이 `[6,7,8,9,7,6]`일 때 `movingLineIndexes == {1,4,6}`.
* **AC3 – 변괘 계산**: 위 AC2 입력의 변괘는 `[7,7,8,8,7,7]`로 계산된다.
* **AC4 – 무변괘 처리**: 변효가 없으면 `changedHexagram == null`.

---

## 10) 구현 노트

* King Wen 매핑은 별도 리소스(JSON/테이블)로 보관: `(upper, lower) → no`, `(no → 이름/괘사/효사 키)`
* 국제화(I18N): 해석 문구/괘명 현지화. `Interpretation`는 locale/version으로 분기
* 테스트: 규칙/불변식은 **프로퍼티 기반 테스트**로 보강 (예: 항상 6효, flip의 involution 등)

---

## 11) 경계 정리 (응용/도메인/인프라)

* **도메인**: `Oracle`, `Hexagram`, `Line`, VO/Services
* **응용**: `OracleAppService` (트랜잭션, DTO 변환, 리포지토리 조합)
* **인프라**: Persistence(Mapping), 리소스 테이블, 로깅/감사, I18N 번들

---

### 결론

* **도메인은 엔티티/VO/서비스로 규칙을 모델링**하고, **DTO는 응용 계층의 I/O 표면**이다. 위 설계를 기준으로 구현 시 스키마/리소스/국제화를 단계적으로 붙이면 된다.
