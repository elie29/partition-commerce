# Place, lift and read a fraud review, and `CustomerStatus` — design

> **Spec:** `docs/specs/customer/fraud/spec.md` (BR-1, BR-3 to BR-9; Language: Blocked, Customer, Lifted, Open, Under fraud review; the paragraph on what a review records)
>
> **Contract:** none: no operation in this version (Scope); `CustomerStatus`, in process, for `payment`
>
> **Plan:** `docs/plans/fraud-review-and-customer-status.md`

## `fraud` — rich (architect, 2026-10-20)

Proposed layer map, the one `invoice`, `payment` and `product` carry: `domain`, `application`, `inbound`, `outbound` under `src/main/java/com/commerce/fraud/`, with `application -> domain`, `inbound -> application`, `outbound -> application`, `outbound -> domain`. `inbound` holds no class in this version: the HTTP operations come with a later contract. `ArchitectureTest` reads the map from `docs/partition.yml`: no change to the test. Its `CONTRACTS` gain the `payment` → `fraud` line in `payment`'s plan, not here (answer J).

## Task 5 — place, lift and read

- **domain**
  - `ReviewStatus`: an enum, `OPEN` and `LIFTED` (Language: Open, Lifted).
  - `Review`: a record `(reviewId, customerId, status, placedAt, liftedAt)`; `liftedAt` is empty while open.
  - `Review.place(reviewId, customerId, placedAt)` makes an `OPEN` review. `lift(liftedAt)` returns it `LIFTED` with its time lifted. `isOpen()`. No method makes a review open again, no status setter: a place after a lift is a new review (BR-5).
  - `CustomerId.require(text)`: the one definition in `fraud` of BR-7, non-blank (Blank as `product` defines it: empty or only white space, non-breaking spaces included) and at most 255 characters, a character a code point. A text it refuses throws `InvalidCustomerId`, whose message names the rule, never the text (BR-9).
  - `toString` of `Review` names its identifier only (BR-9).
- **application**
  - `Reviews`, the port: `add(Review)`, throwing `OpenReviewHeld` when the store already holds an open review for the customer (BR-6); `findOpen(customerId) → Optional<Review>`; `lift(Review)`, a conditional update of a review still open; `ofCustomer(customerId) → List<Review>`.
  - `PlaceReviewUseCase.place(String customerId)`: BR-7 first, refused with `InvalidCustomerId`, nothing placed. A customer with an open review: nothing changes (BR-6). Otherwise a new `Review`, its UUID made by `fraud` (`#5-conventions`), its time placed from the `Clock` truncated to the millisecond (UTC). `OpenReviewHeld` from the store, the other place committed first: nothing changes (BR-6, answer Q). Returns nothing.
  - `LiftReviewUseCase.lift(String customerId)`: the customer's open review, lifted with its time lifted, truncated to the millisecond. No open review: nothing changes (answer L). Returns nothing.
  - `ReadReviewsUseCase.reviews(String customerId) → List<ReviewView>`: each with its identifier, open or lifted, its time placed and its time lifted (BR-8); an empty list for a customer never placed. `ReviewView` carries no `customerId` and names its identifier only in `toString`.
  - `CustomerStatus`, the interface of the contract with `payment`: `boolean blocked(String customerId)`, yes while an open review names it (BR-4). Implemented by `CustomerStatusUseCase` from `Reviews.findOpen`. A failure of the store reaches the caller as an exception; `payment` answers 500 (its BR-32, answer S).
  - Every use case is a Spring `@Service`; nothing reaches the three of the risk team but their tests (answer E). Nothing is checked about who calls (BR-1, BR-5). Lift, read and `CustomerStatus` take the text as given, not checked by BR-7: a text no review names lifts nothing, reads none, answers no.
- **outbound** `JdbcReviews`, through `JdbcClient`, on the table `fraud_review`:
  - `add`: one `INSERT`; the unique constraint on `open_customer_id` refuses a second open review, `DuplicateKeyException` becomes `OpenReviewHeld`.
  - `lift`: `UPDATE … SET status = 'LIFTED', lifted_at = :at, open_customer_id = NULL WHERE review_id = :id AND status = 'OPEN'`.
  - `findOpen`: `WHERE open_customer_id = :customerId`. `ofCustomer`: `WHERE customer_id = :customerId ORDER BY placed_at`.
- **Concurrency:** two places at the same moment: the unique constraint lets the first to commit hold, the other changes nothing (BR-6). A place and a lift crossing: the place sees an open review and changes nothing, or inserts once the lift has committed; the last to commit wins (BR-1, answer T).
- **Logs (BR-9):** the use cases log nothing; an exception names a review's identifier at most, never a `customerId`.

## Data

`V11__create_fraud_review.sql`, the table `#4-data` names:

- `review_id UUID PRIMARY KEY`; `customer_id VARCHAR(510) NOT NULL`, 255 code points as V1 sizes a text; `status VARCHAR(6) NOT NULL` with `CHECK (status IN ('OPEN', 'LIFTED'))`; `placed_at TIMESTAMP(3) WITH TIME ZONE NOT NULL`; `lifted_at TIMESTAMP(3) WITH TIME ZONE` (answer U).
- `open_customer_id VARCHAR(510)`, `UNIQUE`: the `customer_id` while open, empty once lifted; a `CHECK` ties it to `status` and `lifted_at`. It holds BR-6 in the store.
- An index on `customer_id` for the read. No foreign key to another context's table.

## Patterns (`docs/architecture/patterns-grid.md`)

- **Proposed P-7**, `fraud` / status of a review: no pattern. An enum held by the `Review` aggregate, one guarded method (`lift`), no status setter, as P-1 and P-6; P-1 cited, not reopened. Candidate that lost: State (GoF), two classes for one move.
- **Proposed P-8**, `fraud` / one open review per customer (BR-6): a nullable unique column `open_customer_id`, set while open. Candidates that lost: a check in the use case before the insert (two places at the same moment both pass it); a partial unique index on `customer_id WHERE status = 'OPEN'` (H2, the project's database, has none); a lock on a customer row (`fraud` holds no row per customer).
- **Ports and adapters:** `Reviews` and `CustomerStatus`; one implementation each, each with a boundary: the store, the contract with `payment`.

## Questions the design raised, settled

1. P-7 and P-8 as proposed; P-7 cites P-1 (architect, 2026-10-20).
2. The layer map as proposed: the four layers, `inbound` empty in this version (architect, 2026-10-20).
3. A read lists the reviews by time placed, oldest first (PO, 2026-10-20).
4. BR-7 bounds the place only; lift, read and `CustomerStatus` take the text as given (PO, TL, 2026-10-20).
5. Place and lift return nothing (architect, 2026-10-20).
6. Test classes: `PlaceReviewTest`, `LiftReviewTest`, `ReadReviewsTest`, `CustomerStatusTest`, the aggregate tested inside the class of its use case, on an in-memory database, no HTTP (TL, 2026-10-20).

Design validated as written (architect, PO, TL, 2026-10-20).

## Acceptance

Checked once, at delivery, through `./mvnw -q clean test`:

- `PlaceReviewTest`: a place records one open review with its UUID and its time placed to the millisecond; a second place changes nothing; ten places at the same moment leave one open review; a place after a lift opens a new review; a blank or 256-character `customerId` is refused, nothing placed; no output names the `customerId`.
- `LiftReviewTest`: a lift lifts the open review with its time lifted; a lift of a customer with no open review changes nothing; a lifted review is never open again.
- `ReadReviewsTest`: the reviews of a customer with identifier, open or lifted, times; none for a customer never placed; nothing of another customer.
- `CustomerStatusTest`: yes while a review is open; no once lifted; no for a customer never placed.
- `ArchitectureTest`: the layer map of `fraud`, its domain included.
- The tests written before this feature still pass.
