# ADR-0001: 헥사고날 아키텍처 채택
- Status: Accepted
- Date: 2025-08-21
- Authors: @sangja

## Context
- 백엔드 아키텍처 스타일을 결정해야 했다.
- 도메인 규칙이 외부(프레임워크/DB/LLM)로 새어 나가지 않게 유지해야 한다.
- LLM, DB, 메시지큐 등 외부 연동은 반드시 **Port 뒤**로 숨겨야 한다.

## Decision
- **헥사고날 아키텍처(Ports & Adapters)** 채택.
- 의존성 방향: `adapter` / `infrastructure` → `application` → `domain`.
- `domain`: 규칙/엔티티/VO/서비스, `application`: 유즈케이스 오케스트레이션, `adapter`: Web/Persistence, `infrastructure`: 외부 연동.

## Alternatives Considered
- **레이어드 아키텍처**: 단순하지만 도메인/인프라 경계가 약해 규칙 침범 위험.
- **클린 아키텍처**: 개념적으로 유사하지만 레이어 수가 많아 학습/구현 부담 증가.

## Consequences
- **장점**: 테스트 용이성, 외부 의존성 격리, 도메인 규칙 보존.
- **단점**: 학습 곡선이 필요, 작은 팀에서는 다소 과잉 설계일 수 있음.

## References
- [02-architecture.md](../02-architecture.md)
- [Alistair Cockburn, Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
