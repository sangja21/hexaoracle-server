# ADR-0002: 로그인 필수 정책
- Status: Accepted
- Date: 2025-08-21
- Authors: @sangja

## Context
- 초기에 비회원 체험(익명 토큰)도 고려했으나, 서비스 복잡도가 증가하고 데이터 병합 이슈가 발생함.
- 보안과 단순성을 위해 모든 기능은 로그인 이후에만 제공하기로 했다.

## Decision
- **모든 사용자 플로우는 회원가입/로그인 이후에만 가능**.
- 게스트 토큰/익명 기록 병합 로직은 제거한다.
- 최소한의 정보(email, password, displayName)로 가입 후 즉시 점괘 생성 가능.

## Alternatives Considered
- **익명 토큰 발급 후 계정 승격**: 접근성은 좋으나 DB 병합/만료 관리 필요.
- **세션 기반 관리**: 확장성 떨어짐, 멀티 인스턴스 환경에서 불리.

## Consequences
- **장점**: 구현 단순화, 데이터 무결성 보장, 보안성 강화.
- **단점**: 초기 진입 장벽 상승 → 유입률이 다소 낮아질 수 있음.

## References
- [01-requirements.md §2.1](../01-requirements.md)
- [05-sequence-diagrams.md §1](../05-sequence-diagrams.md)
