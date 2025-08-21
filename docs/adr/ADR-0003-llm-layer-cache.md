# ADR-0003: LLM 해석 레이어 & 캐시
- Status: Accepted
- Date: 2025-08-21
- Authors: @sangja

## Context
- AI 해석은 비용/지연이 크고 실패 가능성도 존재한다.
- 안정적인 사용자 경험을 위해 LLM 호출을 통제하고 캐시해야 한다.

## Decision
- **LLM 프록시 계층**을 두어 프롬프트 빌드 및 호출을 캡슐화.
- 해석 결과는 `interpretation` 테이블에 저장(캐시).
- LLM 실패 시 **사전(dictionary) 기반 fallback** 제공.

## Alternatives Considered
- **클라이언트 직접 호출**: 보안 취약, API 키 노출.
- **매번 실시간 호출**: 비용과 응답 지연 증가.

## Consequences
- **장점**: 응답 안정성 확보, 반복 호출 최적화, 비용 절감.
- **단점**: 캐시 일관성 관리 필요, DB 스토리지 증가.

## References
- [08-security-privacy.md §7](../08-security-privacy.md)
- [07-infra.md](../07-infra.md)
- [09-testing-strategy.md §2.6](../09-testing-strategy.md)
