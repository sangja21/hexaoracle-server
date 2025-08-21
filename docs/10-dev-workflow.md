# 10. Dev Workflow — HexaOracle

본 문서는 HexaOracle 저장소의 **브랜치 전략, 커밋 컨벤션, ADR 작성 규칙, 코드 스타일/CI 정책**을 정의한다. 팀 합의가 필요한 항목은 ✅ 표기로 명시한다.

---

## 1) 브랜치 전략

### 1.1 기본 브랜치

* **main**: 배포(프로덕션)용 안정 브랜치. 태깅(semver) 기준.
* **develop**: 통합(Integration) 브랜치. 모든 기능 작업은 여기로 PR 머지.

### 1.2 작업 브랜치 네이밍 규칙

* 포맷: `<type>/<issue-or-id>-<slug>` (kebab-case)
* **type**: `feat`, `fix`, `docs`, `refactor`, `test`, `perf`, `chore`, `build`, `ci`
* 예시

    * `feat/123-create-oracle-api`
    * `docs/erd-initial`
    * `fix/567-null-mapping-hexagram`

### 1.3 브랜치 플로우

1. `develop`에서 작업 브랜치 생성 → 커밋/푸시.
2. PR 타깃은 **develop**.
3. 릴리스 시점에 `develop` → `main`으로 **Release PR** 머지 후 태깅.
4. 핫픽스: `main`에서 `fix/*` 브랜치 파생 → `main`에 머지 및 태그 → 변경분을 `develop`에 **역머지(Back-merge)**.

✅ **팀 합의 포인트**

* [ ] `develop` 유지 vs. **trunk-based**(직접 `main`으로) 전환 여부
* [ ] 릴리스 주기(예: 주간/격주)와 태깅 정책

---

## 2) 커밋 컨벤션

### 2.1 형식 (사용자 지정 포맷)

요약 줄은 한 문장, 상세는 불릿으로 기술한다.

```
[TYPE] : 요약 메시지(기능/의도)
- 상세 1
- 상세 2
```

* **TYPE 목록**

    * **FEAT**: 사용자 기능 추가/변경
    * **FIX**: 버그 수정
    * **REFACTOR**: 리팩토링(행동 변화 없음)
    * **DOCS**: 문서 변경(README, /docs/\*)
    * **TEST**: 테스트 코드/데이터 추가·수정
    * **PERF**: 성능 개선
    * **CHORE**: 빌드/도구/환경 등 잡무
    * **BUILD**: 빌드 시스템/의존성
    * **CI**: CI 파이프라인/스크립트
    * **STYLE**: 포맷팅/세미콜론 등 의미 없는 스타일 변경
    * **REVERT**: 특정 커밋 되돌리기

* **예시**

```
[FEAT] : 점괘 생성 API(POST /oracles) 추가
- 본괘/변괘 계산 도메인 서비스 연결
- JPA 영속 및 트랜잭션 범위 설정
```

```
[DOCS] : 06-erd.md 초안 추가
- User/Oracle/Interpretation 관계 정의
- 인덱스/제약조건 초안
```

### 2.2 추가 규칙

* 한 커밋은 **하나의 의도**만 담는다(작게, 자주).
* **이슈 연동**: 상세에 `Refs #123` 또는 `Fixes #123` 표기 권장.
* **BREAKING CHANGE**가 있으면 상세 마지막 줄에 명시.
* 멀티 스코프가 필요하면 `[]`를 중첩해도 됨(선택): `[FEAT][api] : ...`

✅ **팀 합의 포인트**

* [ ] `STYLE` 커밋 허용 여부(단독 커밋 vs. 다른 타입과 합치기)
* [ ] PR 머지 전략: **Squash** vs. Rebase vs. Merge commit

---

## 3) PR 가이드 & 템플릿

### 3.1 원칙

* **작게, 독립적으로**: 리뷰가 30분 이내 가능한 크기 목표.
* **한 PR = 한 주제**: 도메인 변경과 스타일 변경을 분리.
* **체크리스트**를 통과하지 않으면 머지하지 않는다.

### 3.2 PR 템플릿 (제안)

```markdown
# HexaOracle
## 참고 자료
<!--
  (Optional: 참고 자료가 없는 작업 - 단순 버그 픽스 등 의 경우엔 해당 란을 제거해주세요 !)
  작업에 대한 참고자료(PR, 피그마, 슬랙 등)가 있는 경우 링크를 참고 자료에 같이 추가해주세요.
  히스토리나 정책, 특정 기술 등에 대한 이해가 필요한 작업일 때 참고자료가 있다면 리뷰어에게 큰 도움이 됩니다!
-->

## PR 설명
<!-- 해당 PR이 왜 발생했고, 어떤부분에 대한 작업인지 작성해주세요. -->

## 리뷰 포인트
<!-- 
    리뷰어가 함께 고민해주었으면 하는 내용을 간략하게 기재해주세요.
    커밋 링크가 포함되면, 더욱이 효과적일 거예요! 
-->

## Definition of Done (DoD)
<!--
    DOD 란 해당 작업을 완료했다고 간주하기 위해 충족해야 하는 기준을 의미합니다.
    어떤 기능을 위해 어떤 요구사항을 만족하였으며, 어떤 테스트를 수행했는지 등을 명확하게 체크리스트로 기재해 주세요.
    리뷰어 입장에서, 모든 맥락을 파악하기 이전에 작업의 성숙도/완성도를 파악하는 데에 도움이 됩니다.
    만약 계획되거나 연관 작업이나 파생 작업이 존재하는데, 이후로 미뤄지는 경우 TODO -, 사유와 함께 적어주세요.

    ex:
    - [x] 상품 도메인 모델 구조 설계 완료 ( [정책 참고자료](관련 문서 링크) )
    - [x] 상품 재고 차감 로직 유닛/통합 테스트 완료
    - [ ] TODO - 상품 주문 로직 개발 ( 정책 미수립으로 인해 후속 작업에서 진행 )
-->
```

---

## 4) ADR 작성 규칙

### 4.1 위치 & 파일명

* 위치: `docs/12-adr/`
* 파일명: `ADR-YYYYMMDD-<slug>.md` (예: `ADR-20250821-llm-proxy-choice.md`)

### 4.2 템플릿

```markdown
# ADR: <제목>
- Status: Proposed | Accepted | Superseded | Rejected
- Date: YYYY-MM-DD
- Authors: @id, @id

## Context
- 배경/문제 정의, 제약조건

## Decision
- 최종 선택(근거 포함)

## Alternatives Considered
- A / B / C (장단점)

## Consequences
- 장점/단점, 리스크, 마이그레이션 영향

## References
- 이슈/PR 링크, 외부 근거
```

### 4.3 운영 규칙

* **의미 있는 아키텍처 선택**은 반드시 ADR로 남긴다.
* **Superseded** 시 상단에 링크 추가(체인 유지).
* 작은 값(변수명, 미세 포맷팅 등)은 ADR 대상 아님.

✅ **팀 합의 포인트**

* [ ] ADR 승인 필요 인원 및 승격 기준

---

## 5) 코드 스타일 & 품질

### 5.1 Backend (Java/Spring)

* **Spotless + Google Java Format** 적용.
* Gradle 태스크:

    * `./gradlew spotlessApply` (자동 포맷)
    * `./gradlew spotlessCheck` (검사)
* **Checkstyle**(선택) 규칙은 기본 Google 스타일에서 프로젝트 룰만 최소 확장.

### 5.2 Frontend (React)

* **Prettier** + **ESLint(typescript-eslint/airbnb 권장)**
* NPM 스크립트:

    * `npm run lint`, `npm run format`
* import 정렬/미사용 변수 에러를 CI에서 차단.

### 5.3 공통

* **pre-commit** 훅에 `format/lint/test`를 걸고, CI에서도 동일하게 검증한다.
* 빌드에 **자바, 프론트 각각 최소 커버리지 기준** 설정(예: line 70%/branch 60%).

✅ **팀 합의 포인트**

* [ ] 최소 커버리지 임계치(라인/브랜치)
* [ ] ESLint 규칙 강도(Warning vs. Error)

---

## 6) CI/CD 개요

* **CI 트리거**: PR 열림/업데이트 시 `build + test + lint + spotlessCheck` 실행.
* **Quality Gate**: CI 실패 시 머지 금지(Branch Protection).
* **CD**: `main` 태깅(`vX.Y.Z`) 시 배포 워크플로 실행.
* 아티팩트: 백엔드 Docker 이미지, 프론트 정적 번들.

✅ **팀 합의 포인트**

* [ ] Squash merge 강제 여부
* [ ] 릴리스 노트 자동 생성 여부(actions/release-please 등)

---

## 7) 버전/릴리스 정책

* **Semantic Versioning**: `MAJOR.MINOR.PATCH`
* 태그 예시: `v1.2.0`
* **Release PR**: `develop → main`, 체크리스트 포함.
* 마이그레이션(Flyway) 포함 시 릴리스 노트에 **업그레이드 가이드** 명시.

---

## 8) Definition of Done (DoD)

* 모든 테스트/정적분석/포맷 통과.
* 필요한 문서 업데이트 완료(특히 **03-domain-model.md**, **04-api-spec.md**).
* 보안/개인정보/성능 영향 검토.
* 롤백/모니터링 계획 반영.

---

## 9) 부록: 커밋/PR 예시

**Commit**

```
[REFACTOR] : HexagramService 이진 변환 로직 단순화
- flip 연산 involution 성질 기반 테스트 추가
- 매핑 테이블 접근 캐시화
```

**PR Title**

```
feat: implement oracle casting API (POST /oracles)
```

**PR Body**

```markdown
## What
- 점괘 생성 API 및 도메인 서비스 연결

## Why
- 사용자 입력(lines) 기반 본괘/변괘 계산 제공

## How
- HexagramService 도입, Application 유즈케이스 연결

## Testing
- 단위/통합 테스트 통과 스크린샷 첨부

## Checklist
- [x] 테스트 통과
- [x] 03/04 문서 반영
- [x] 보안/마이그레이션 영향 검토
```

---

> 문서는 저장소 상황에 맞춰 수시로 업데이트한다. 변경 시 커밋 타입은 **\[DOCS]** 를 사용하고, 문서 내부 변경 이력은 Git 로그로만 관리한다.
