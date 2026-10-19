# Boundary workshop — invoice: "Cancelled"

Held on 2026-10-19 by Claire Martin (architect), Paul Durand (product owner), Léa Petit (business analyst).
Material: prepared by partition-reshape on 2026-10-19 (the two meanings of "Cancelled", `docs/sources/billing.md` Glossary).

| Item | Decision |
| --- | --- |
| Move | Fission: `invoice` becomes `invoice` and `payment`, both in area `billing` |
| Why | "Cancelled" is, of an unpaid invoice, "voided before any payment, at no fee", and of a paid invoice, "reversed within 24 hours of being paid" (the PRD's glossary): two meanings, two contexts |
| Units | REQ-4 and REQ-5 go to `payment`; REQ-1 to REQ-3 stay in `invoice` |
| Integration | `payment` calls `invoice` in process, through `InvoicePayments`: read an invoice as `getInvoice` shows it; record a payment, refused when the invoice is no longer issued; record a payment cancelled |
| API | `payment` serves `payInvoice`, the tag `Payment` of the contract |
| Contract | `InvoicePayments`, an interface of `invoice`'s application layer, frozen for the move |
| Rules | The rules copied from REQ-4 and REQ-5 (BR-5, BR-6), the rules written for paying and DEC-1, CardPay, go to `payment`; the others stay in `invoice`. Paying refuses before calling CardPay (`payment`); `invoice` records a payment only on an invoice still issued, so of a pay and a cancel at the same moment the first to commit holds (`invoice`). Needed by both, so in the area file: the `Persistence` line "every amount is stored in minor units, with its currency" (Rule C), the bound "non-blank, at most 255 characters" (the label's rule, step 04), and the shared words (amount, minor units, currency, refused). The time and the charge reference of a payment go to `payment` |
| Code of `payment` | `src/main/java/com/commerce/payment/**`, rich: a payment moves from made to cancelled within 24 hours |
| Decided by | Claire Martin (architect), Paul Durand (product owner), Léa Petit (business analyst) |
