# Bounded contexts — customer

> **Source:** `docs/legacy/customer-spec.md`
>
> **Source version:** no version in the source; read on 2026-10-20
>
> **Provenance:** 2026-10-20 · 747a8dc79c13a273f103abb879e407ce6b9047d3
>
> **Scope:** whole source
>
> **Units covered:** 2 / 2
>
> **Decided:** 2026-10-20 · Claire Martin (architect) · proposal: option 1

The **Scope** line fixes the units analysed; the others appear in the `Coverage` table with the mention "out of scope". Change it only when a scope extension has been validated.

The **Provenance** line is the anchor of every later update: it fixes the exact state of the source this decomposition comes from. Never delete it; change it only together with the content it dates.

## Splitting criteria applied

The source is a spec written before Partition, with no identifiers: a unit is one of its sections, named by the slug of its heading. `#1-overview` and `#4-data` give context only, `#5-conventions` says how code is written (TL, 2026-10-20) and `#6-ruling-history` records rulings: none of them is a unit. The six criteria, evaluated unit by unit over the units in scope:

- **C1 — Functional grouping** (hint, outside the threshold): the unit carries a section of its own.
- **C2 — Root aggregate of its own** (mandatory): the business entity the unit acts on, as the source names it, differs from that of the others.
- **C3 — Business language of its own**: the vocabulary of the unit stands alone.
- **C4 — Life-cycle autonomy**: the unit can be used, versioned and have its model evolve without coordination with the others.
- **C5 — Distinct external dependencies**: the unit relies on named third-party systems different from those of the others.
- **C6 — Shared data** (argues for grouping): the unit reads or writes the same entity or piece of data as others, as the source names it.

Decision rules: **R1** a bounded context needs C2 and one of C3–C5; **R2** C1 never counts in the threshold; **R3** a unit failing R1 joins its nearest candidate; **R4** C6 never creates a bounded context and argues against a separation; **R5** a single unit neither justifies nor forbids a bounded context; **R6** two bounded contexts that no longer satisfy R1 against each other call for a merge.

## Bounded contexts retained

| Bounded context | Units | Criteria satisfied | Justification |
| --- | --- | --- | --- |
| customer | #2-customer-accounts | C2, C3, C4 (hint: C1, section "Customer accounts") | It acts on the account: "A customer has an account: a name, an email, a billing address." Its words (name, email, billing address) appear nowhere in `#3-fraud-review`. Its actor is the customer, who "changes their address at any time". No named third party (C5 no). |
| fraud | #3-fraud-review | C2, C3, C4 (hint: C1, section "Fraud review") | It acts on the fraud review: "A member of the risk team places a customer under fraud review, and lifts it." Its words (risk team, under fraud review, lift, blocked) appear nowhere in `#2-customer-accounts`. Its actor is the risk team: "A review is lifted only by a member of the risk team."; and only reviews are archived ("Reviews older than one year are archived, not deleted.", `#6-ruling-history`, 2024-09-02). No named third party (C5 no). Against R4: it shares with `customer` only the customer it is about; weak sharing, it does not prevail. "An account and a review are two root aggregates (C2), each with its own words (C3)." (Claire Martin, architect, 2026-10-20) |

## Decisions not to split

None.

## Alternatives rejected

| Option | Bounded contexts | Why not chosen | Decided by | Date |
| --- | --- | --- | --- | --- |
| 2 | `customer` (#2-customer-accounts, #3-fraud-review), under R4; breaks R4: the sharing is the customer's identity only, against C2, C3 and C4 of `#3-fraud-review` | "An account and a review are two root aggregates (C2), each with its own words (C3)." | Claire Martin, architect | 2026-10-20 |

## Kept against the criteria

None.

## Gaps detected

- "A customer under fraud review can neither pay nor be refunded." ([`#6-ruling-history`](../../legacy/customer-spec.md#6-ruling-history), 2024-03-12) is also held by `docs/specs/billing/payment/spec.md` as BR-32 (from source: REQ-4, REQ-6 of `docs/sources/billing.md`): it spans two areas, goes to no area file, and is quoted as a candidate under `Applicable area rules and invariants` of `fraud`. Candidate for the admission test of the invariant register. Nothing moved in `billing`. Resolved on 2026-10-20: `fraud` owns it, `payment` consumes `CustomerStatus` (owner); it fails the admission test (a rule one bounded context owns is local to it) and is BR-3 of `fraud`. BR-32 of `payment` stays, a duplicate also stated by `docs/sources/billing.md` (REQ-4, REQ-6), until that source is amended; nothing moved.
- BR-32 of `payment` reads the customers under fraud review from `application.yaml`, "until the risk team has a system": this source describes that system (`#3-fraud-review`: "The other services ask whether a customer is blocked: yes or no."). Whether `payment` asks `fraud` instead is the billing owner's decision. Nothing changed in `billing`. Resolved on 2026-10-20: `payment` asks `fraud` through `CustomerStatus`, an interface of `fraud`'s application layer (owner; architect). Left untrue in `docs/specs/billing/payment/spec.md`, reported, not rewritten: the list of BR-32 and its `application.yaml` (BR-32, the word "Customer under fraud review" of `Language`), the port `FraudReview`, and no `fraud` line in `Contracts with neighbours`.
- "Identifiers are UUIDs, generated by the service, never by the caller." ([`#5-conventions`](../../legacy/customer-spec.md#5-conventions); [`#4-data`](../../legacy/customer-spec.md#4-data) also says "identifiers are UUIDs") against BR-11 of `docs/specs/billing/invoice/spec.md`, under which `customerId` is any text bounded as a label is (AR-1 of `billing`). Which identifier of a customer does `billing` hold?
- `#5-conventions` is how code is written (TL, 2026-10-20): not a unit and not an area rule; a candidate for a rule file, which `partition-rules` writes on the owner's word. Not written by this procedure. Resolved on 2026-10-20: one line in `.apm/instructions/backend-api-contract-customer.instructions.md`, scoped to `src/main/java/com/commerce/fraud/**` (TL).
- `#4-data` names the tables `customer_account` and `fraud_review`: no schema documents are declared for this project (`paths.schemas`), nothing is mapped.
- `customer` has no code in this project yet (TL, 2026-10-20): declared in `docs/partition.yml` without a code path.
- No coupling outside the scope: the scope is the whole source.
- No area file for `customer`: each spec keeps its own words, as the old spec uses them; no word and no rule goes to an area file (BA, 2026-10-20). The copied rules of `customer` and `fraud` are confirmed as written, with no other rule (BA, 2026-10-20).

## Coverage

| Unit | Grouping | Bounded context |
| --- | --- | --- |
| #2-customer-accounts | 2. Customer accounts | customer |
| #3-fraud-review | 3. Fraud review | fraud |

## History

| Date | Version | Provenance | Nature | Detail |
| ---- | ------- | ---------- | ------ | ------ |
