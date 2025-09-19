# Gift REST Exposure Plan

## Goals
- Expose four REST endpoints (`listGoods`, `listCandidates`, `checkEligibility`, `grantBatch`) without impacting existing Dubbo-facing services.
- Keep implementation minimal, follow Hexin DDD layering, and reuse existing external APIs for data access.
- Ensure every functional increment is protected by automated tests before moving to the next step.

## Constraints & Assumptions
- Spring Boot + Dubbo multi-module project; changes land in `deploy` module under the DDD structure described in `hexin_ddd_structure.txt`.
- Only REST contract is new; downstream Dubbo services stay untouched.
- Use existing Dubbo clients (`PortfolioBaseApi`, `PackageBaseApi`, `PortfolioTrackApi`, `PackageTrackApi`, `UserPaidStatusApi`, `ActivityGiftRightsApi`).
- VO/DTO classes must stay lightweight and serializable via Jackson; avoid heavy annotations beyond Lombok + validation.
- Front-end consumes REST URLs under `interfaces/rest`; internal service orchestration must keep using Dubbo clients.

## High-Level Architecture
- **interfaces/rest**: `GiftController` hosts the four endpoints. Request payloads land in `rest/query`, responses in `rest/vo`. Converters isolate controller from manager VO/DTOs.
- **app/manager**: `ProductGiftManager` orchestrates calls to domain services and external APIs via injected collaborators.
- **modules/gift/domain/service**: Focused domain services delivering core logic:
  - `GoodsAssembler`: merge base portfolios/packages into `GoodsBaseVO`.
  - `CandidatePolicy`: derive gift candidates per flow diagram.
  - `EligibilityService`: short-circuit eligibility per user.
  - `GiftGrantService`: batch grant rights with failure isolation.
- **common/external/rpc (or existing client packages)**: Reuse/declare Dubbo interfaces, add thin adapters if signatures require wrapping.
- **interfaces/rest/converter` & `app/manager/converter`**: Narrow conversions between external DTOs and internal VO structures.

## Data Contracts
- `GoodsBaseVO`: `{ goodsId: Long, goodsName: String, type: String, advisorId: Integer }`.
- `GiftCandidateVO`: `{ userId: Integer, nickName: String, avatarUrl: String, productName: String, purchaseDate: String, durationMonths: Integer }`.
- REST payloads:
  - `ListGoodsQuery`: `advisorId` (Integer, required).
  - `ListCandidatesQuery`: `selectedGood`, `allGoods` (reuse `GoodsBaseVO`).
  - `CheckEligibilityCommand`: `selectedGood`, `userIds`.
  - `GrantBatchCommand`: `selectedGood`, `candidates`, `attr`, `source`.
- REST responses mirror flow: lists of VO or boolean flags.

## Implementation Steps & Required Tests
1. **Scaffold REST package artefacts**
   - Create query/VO classes, controller skeleton, and converters with TODO placeholders.
   - Tests: `GiftControllerTest` ensuring request binding & 200 OK with mocked manager.
2. **Goods aggregation**
   - Implement `GoodsAssembler` using `PortfolioBaseApi` & `PackageBaseApi`; wire into manager.
   - Tests: `GoodsAssemblerTest` mocking APIs, verifying merge & type tags.
3. **Candidate derivation**
   - Build `CandidatePolicy` to fetch all paid users, derive diff, enrich latest order info.
   - Tests: `CandidatePolicyTest` covering portfolio/package, empty diff, ordering rules.
4. **Eligibility evaluation**
   - 保持 `EligibilityService` 为 TODO 骨架，并标注外部 Dubbo 检查待接入。
   - Tests: `EligibilityServiceTest` 验证当前占位行为（全部返回 false），待接口确认后再扩展。
5. **Gift grant orchestration**
   - 串行调用 `ActivityGiftRightsApi` 并在服务端拼装 `source`（选用 selectedGood.advisorId、goodsId、type、userId）。
   - Tests: `GiftGrantServiceTest` 覆盖成功/失败并断言 `source` 文案格式。
6. **ProductGiftManagerImpl**
   - Integrate domain services + external APIs, implement four manager methods delegating appropriately.
   - Tests: `ProductGiftManagerImplTest` using mocks to confirm sequencing and parameter passing.
7. **Finalize REST controller**
   - Wire validation, inject manager, map manager outputs to response VOs，确保 `/grant` 不再暴露 `advisorId` 字段。
   - Tests: Extend `GiftControllerTest` with MockMvc behavioural tests (status, payload structure)。
8. **Integration smoke (optional)**
   - If feasible, add Spring slice test verifying Dubbo clients mocked via stubs and all beans load.

_All steps require running the specified unit test suite before continuing. Command template: `mvn -pl deploy test -Dtest=<TestClass>#<method>` or equivalent._

## Testing Matrix
- `listGoods`: `GoodsAssemblerTest`, `ProductGiftManagerImplTest#listGoods`, `GiftControllerTest#listGoods`.
- `listCandidates`: `CandidatePolicyTest`, `ProductGiftManagerImplTest#listCandidates`, `GiftControllerTest#listCandidates`.
- `checkEligibility`: `EligibilityServiceTest`, `ProductGiftManagerImplTest#checkEligibility`, `GiftControllerTest#checkEligibility`.
- `grantBatch`: `GiftGrantServiceTest`, `ProductGiftManagerImplTest#grantBatch`, `GiftControllerTest#grantBatch`.

## Risks & Mitigations
- **External API latency**: keep calls minimal; consider future batching or caching.
- **Large candidate list**: ensure domain services stream/process lists efficiently, avoid N^2 operations.
- **Dubbo DTO evolution**: encapsulate conversions so REST layer unaffected by upstream changes.

## Follow-Up Questions
- Confirm actual package names for REST/controller placement.
- Validate availability of Dubbo clients in Spring context (possibly via `@DubboReference`).
- Determine whether existing exception handlers cover new endpoints or if custom errors are needed.
