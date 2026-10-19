# Reshape — fission: `invoice` becomes `invoice` and `payment`

> **Decided on:** 2026-10-19, by Claire Martin (architect), Paul Durand (product owner), Léa Petit (business analyst), from workshop: `docs/sources/workshop-invoice-fission.md`; validated by name to be carried out on 2026-10-19
>
> **From:** `invoice` (area `billing`) **To:** `invoice` (REQ-1 to REQ-3) and `payment` (REQ-4, REQ-5), both in area `billing`
>
> **Raised by:** a workshop on the two meanings of "Cancelled" in the glossary of `docs/sources/billing.md`, 2026-10-19

## Why here

"Cancelled" is given two meanings by the PRD's glossary (`docs/sources/billing.md`, Glossary): two meanings, two contexts (workshop). A payment is an entity of its own (architect, 2026-10-19).

| Term | Meaning on this side (`invoice`) | Meaning on the other side (`payment`) |
| ---- | -------------------------------- | ------------------------------------- |
| Cancelled | of an unpaid invoice: voided before any payment, at no fee; an invoice whose payment is cancelled is not cancelled for BR-1 (PO) | reversed within 24 hours of the payment (BA) |

## Challenge

| Finding | Improvement proposed | Outcome |
| --- | --- | --- |
| 1. **R1 fails for `payment` on C2.** The result: "a payment moves from made to cancelled within 24 hours". The source names no payment entity: "The customer pays an issued invoice by card" (REQ-4), "The customer can cancel a paid invoice" (REQ-5) act on the invoice (`billing.md:26,31`). From the source alone: C2 no, C3 no (REQ-4 and REQ-5 need "issued invoice", "paid invoice", "amount"), C4 no ("the invoice is then unpaid again", REQ-5, changes the invoice), C5 yes (CardPay). The decision does not make C2 hold. | Either the product owner amends the PRD so that REQ-4 and REQ-5 name the payment they act on, and the delta of `partition-shape` re-evaluates C2; or the architect keeps the fission against C2, recorded in `Kept against the criteria` of `bounded-contexts.md` with the reason. | Kept against C2 (R1): "Cancelled" carries two meanings, and the PRD will name the payment; a payment is an entity of its own (architect, 2026-10-19) |
| 2. **R4: the invoice's state is written for `payment`.** The result: `payment` records a payment and a payment cancelled through `InvoicePayments`. Against: R4, and the alternative rejected on 2026-10-19, "the invoice's state would be written from both sides (R4)" (`bounded-contexts.md`, _Alternatives rejected_). | Record the discussion: the result keeps every write of the invoice in `invoice`, behind `InvoicePayments`. The line of `Alternatives rejected` and the line of `Decisions not to split` this fission overturns move to `History`, dated. | Taken: every write of the invoice's state stays inside `invoice`, behind `InvoicePayments`; the earlier rejection moves to `History` (architect, 2026-10-19) |
| 3. **The source still gives the invoice both meanings.** The result moves the second "cancelled" to the payment ("a payment cancelled"). The PRD says the invoice is cancelled: "A paid invoice is cancelled only within 24 hours of being paid" (REQ-5, `billing.md:31-32`) against "An invoice is cancelled only before any payment" (REQ-3, `billing.md:20`), kept as written knowingly (`bounded-contexts.md`, _Gaps detected_). BR-1 ("not cancelled") and BR-18 read the invoice's "cancelled" (attack finding 5 of the cancel feature). | No wording proposed: the rules are copied from the source. The product owner says which requirement holds and amends the PRD; until then the spec of `payment` copies REQ-5 with the clash. | `invoice`'s "cancelled" is voided only: an invoice whose payment is cancelled is not cancelled for BR-1. The PRD will name the payment in a later version; until then REQ-3 and REQ-5 stay as written (PO, 2026-10-19) |
| 4. **The amount's storage is a rule of code, not an area rule.** The result puts "every amount is stored in minor units, with its currency" (Rule C) in the area file. It is already `.claude/rules/backend-database.md:7` (architect, 2026-10-19), scoped to `src/main/resources/db/migration/**`, which `payment`'s migrations fall under; "how code is written here" goes to a rule file, never an area rule (`partition-spec`, _Where a statement belongs_). "Rule C" names nothing in the project. | Leave it in the rule file; nothing goes to the area file for it. | Not taken: the line is what both contexts store, not only how code is written; `payment` stores the amount charged. It goes to the area file word for word; the rule file keeps the column convention only (architect, 2026-10-19) |
| 5. **"step 04" names nothing in the project.** The result puts "non-blank, at most 255 characters" (the label's rule, step 04) in the area file. That bound is BR-12 of `invoice` (architect, 2026-10-19); BR-5 (charge reference), BR-11 and BR-22 (card token) refer to it. | Confirm it is BR-12. Then BR-12 leaves the spec of `invoice` for the area file in the same change, and BR-5, BR-11 and BR-22 refer to the area rule. | Taken: BR-12's bound moves to the area file; "Blank" moves with it (architect, 2026-10-19) |
| 6. **"Refused" is `invoice`'s today.** The result puts "refused" in the area file. The `invoice` Language says "Declined … Not a refusal: a refusal is `invoice`'s (PO, 2026-10-19)"; after the fission `payment` refuses too (BR-20, BR-22). "Minor units" is in no Language today; it is a word of the rule of code (finding 4). | The product owner rewrites the line "Declined" when "refused" goes to the area file; the architect says whether "minor units" is a business word. | Taken: "Refused" and "Payment" are shared words of the area file (BA); "minor units" is a business word, with "amount" and "currency" (architect, 2026-10-19) |
| 7. **"The rules written for paying" names none, and BR-3 and BR-5 fall on both sides.** BR-3 (from REQ-2, `invoice`) carries the pay's record: time, charge reference, the card token in no log. BR-5 (from REQ-4, `payment`) carries the conditional record that the result gives to `invoice`. | The architect confirms or corrects, rule by rule: to `payment` BR-5 (the charge), BR-6, BR-20, BR-21, BR-22, the pay part of BR-3, the words Paid, Payment, Card token, Charge reference, Declined, Time of the payment, DEC-1, the dependency on CardPay, and the open questions on CardPay, the charge kept nowhere, retries after a 402, the card token at DEBUG and attack finding 6 of the issue-and-read feature; to `invoice` the rest, the conditional record of BR-5 included. | Taken as proposed; a rule keeps its number when it moves; the dated attack on paying moves to `payment` with the rules it attacked (PO, BA, 2026-10-19) |
| 8. **The record of a payment is no longer one commit.** The result: the time and the charge reference go to `payment`; `invoice` records a payment, refused when the invoice is no longer issued. Today one conditional update writes the status, the charge reference and the time (`JdbcInvoices.java:77-79`, V3); "Paid: … its charge is recorded" (Language). Split in two stores: the invoice `PAID` and the payment's record failing leaves a paid invoice with no charge recorded, or the reverse. | The architect states what "record a payment" of `InvoicePayments` carries and which transaction holds both writes; written into the frozen contract. | Taken: the time and the charge reference go to `payment`'s own table (V4), written in the same transaction as `recordPayment`, the first to commit holds both; the invoice row's columns go with the move; the refusal of recording a cancelled payment is decided with REQ-5, until then a `default` method that throws (architect, 2026-10-19) |
| 9. **`payInvoice` is generated into `invoice`.** The result: `payment` serves `payInvoice`, tag `Payment`. The generator (`pom.xml:96-101`) generates every operation into one `InvoicesApi` in `com.commerce.invoice.inbound.api`, which `InvoiceController` implements (`InvoiceController.java:23`); "a controller implements the generated interface" (`.claude/rules/backend-api-contract.md`). `payInvoice` answers `invoice`'s `Invoice` schema. | Generate by tag, each interface into its bounded context's package, as a precondition task; the architect says whether `payment` may answer the schema `Invoice` that `invoice` owns. | Taken: one interface per tag, in the precondition plan (TL); `payment` answers with the contract's `Invoice` schema (architect, 2026-10-19) |
| 10. **The form of `payment` rests on a rule with no trigger.** "Rich: a payment moves from made to cancelled within 24 hours" rests on BR-6, which has no trigger and no expected effect; REQ-5 has no operation in contract 1.2.0; the fee and the pay after a reversal are open questions. | Keep the form; carry these questions to `Open questions` of the spec of `payment`. | Taken (architect, 2026-10-19). The pay after a reversal is settled: the invoice can be paid again, "unpaid again" is issued again, and is not cancelled by the accountant (PO, 2026-10-19) |

## Integration

| Crosses the boundary | Direction | Form (event, call, file) | Contract |
| -------------------- | --------- | ------------------------ | -------- |
| Read an invoice as `getInvoice` shows it | `payment` → `invoice` | call, in process | `InvoicePayments` |
| Record a payment, refused when the invoice is no longer issued; in the caller's transaction | `payment` → `invoice` | call, in process | `InvoicePayments` |
| Record a payment cancelled: not a modification (BR-3), it changes the status (PO); built with REQ-5 | `payment` → `invoice` | call, in process | `InvoicePayments` |
| `payInvoice` (tag `Payment`), answered with the contract's `Invoice` schema | caller → `payment` | HTTP | `docs/contracts/billing.openapi.json` 1.2.0 |

`InvoicePayments` is written and frozen first, through its own plan (`docs/plans/invoice-fission-preconditions.md`); its frozen text is the Java interface in `invoice`'s application layer (architect, 2026-10-19). Frozen on 2026-10-19, `src/main/java/com/commerce/invoice/application/InvoicePayments.java`:

```java
Optional<InvoiceView> find(UUID invoiceId);           // as getInvoice shows it; empty when no such invoice
InvoiceView recordPayment(UUID invoiceId);            // ISSUED -> PAID, nothing else (BR-3), in the caller's transaction;
                                                      // throws InvoiceNotIssued when no longer ISSUED
default InvoiceView recordPaymentCancelled(UUID invoiceId); // PAID -> ISSUED (REQ-5); throws UnsupportedOperationException until REQ-5
```

`InvoiceView` and `LineData` are frozen with it, as they stand. One adapter implements it: `InvoicePaymentsService` (GoF Adapter over the port `Invoices`).

## Rules

| Rule | Was in | Goes to |
| ---- | ------ | ------- |
| BR-5 (the charge; copied from REQ-4), BR-6 (copied from REQ-5) | `invoice` | `payment`, numbers kept |
| BR-20, BR-21, BR-22; the pay part of BR-3 (the time, the charge reference, the card token in no log) | `invoice` | `payment`, numbers kept |
| DEC-1, the dependency on CardPay | `invoice` | `payment` |
| The words Paid, Payment, Card token, Charge reference, Declined, Time of the payment | `invoice` (Language) | `payment`, except Payment: area file |
| The open questions on CardPay, the charge kept nowhere, retries after a 402, the card token at DEBUG; attack finding 6 of the issue-and-read feature; the dated attack on paying | `invoice` | `payment` |
| A payment recorded only on an invoice still issued; of a pay and a cancel at the same moment the first to commit holds (the conditional record of BR-5) | `invoice` | `invoice` |
| Recording a cancelled payment changes the status and is not a modification (BR-3) | — | `invoice` |
| The time and the charge reference of a payment, and the amount charged: stored in `payment`'s own table (V4) | `invoice` (V3 columns) | `payment` |
| "Every amount is stored in minor units, with its currency: a `BIGINT` and a three-letter ISO 4217 code, never a decimal" | `.claude/rules/backend-database.md:7` | area file `billing`, word for word; the rule file keeps the column convention |
| Non-blank, at most 255 characters (BR-12), with the word Blank | `invoice` | area file `billing` |
| The words Amount, Minor units, Currency, Refused, Payment | `invoice` (Language), or none | area file `billing` |
| The other rules | `invoice` | `invoice` |

## Preconditions

Carried out by `docs/plans/invoice-fission-preconditions.md`.

- [x] Every caller reaches it through an interface (preconditions plan closed 2026-10-19, gate SHIP WITH CONDITIONS): `payInvoice` through `PaymentApi`, implemented by `PayInvoiceController`, done 2026-10-19 (plan Task 2); `payment` reaches `invoice` through `InvoicePayments`, written 2026-10-19; the pay is rewired onto it by the move (V4 gives the charge reference its home)
- [x] One adapter implements that interface: `InvoicePaymentsService`, 2026-10-19
- [x] The contract is written and frozen: `InvoicePayments`, 2026-10-19 (signatures under `Integration`)
- [x] For a fission, the new bounded context has its durable spec and its own language: `docs/specs/billing/payment/spec.md`, 2026-10-19
- [x] Each rule both sides need has one home: AR-1, AR-2 and the shared words in `docs/specs/billing/area.md`, 2026-10-19; BR-23 in `invoice`, BR-24 in `payment`
- [x] For a merge, the person who carries the hand-written sections over: not a merge
- [x] For a removal, nothing calls it any more: not a removal

## Proof that it was a move

Carried out by `docs/plans/invoice-fission.md`.

- Verification: `git status --short -- . ':!src/main/java/com/commerce/payment' ':!src/test/java/com/commerce/payment'` against `4b3a86f`, the commit before the move; then `git diff --quiet 4b3a86f --` the callers and the contract
- Expected: outside `payment`, only the wiring (`pom.xml`, `ArchitectureTest.java`), what `invoice` loses (8 files deleted, 9 changed, its `PayInvoiceTest` moved, `InvoicePaymentsTest`), the migrations V4 and V5, the plan; no caller of `invoice` and not its contract
- Real output (2026-10-19): the list above, file for file (`docs/plans/invoice-fission.md` Task 6); `git diff --quiet` on `InvoicePayments`, `InvoiceView`, `LineData`, `InvoiceNotIssued`, `InvoiceController`, the three use cases of `invoice`, `billing.openapi.json`, `application.yaml` and the four test files of `invoice` that call it: `exit=0`. No caller changed

## After

- [x] C4 model updated and regenerated: the component `payment` in `commerce-app`, `payment -> invoicing` through `InvoicePayments`, CardPay called by `payment`, 2026-10-19
- [x] `docs/partition.yml` declares `payment`, area `billing`, code `src/main/java/com/commerce/payment/**`, form rich, 2026-10-19
- [x] Budget measured again: `docs/budget/2026-10-19.md` — `invoice` 5 881, `payment` 5 089, both far under 32 000; the fission was raised by a word, not a gauge
