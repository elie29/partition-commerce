# billing

The language and the area rules shared by the bounded contexts of this area. A rule one bounded context owns stays in its spec; a statement that spans areas or layers goes to the invariant register.

## Bounded contexts

| Bounded context | Spec                |
| --------------- | ------------------- |
| invoice         | `invoice/spec.md`   |
| payment         | `payment/spec.md`   |
| refund          | `refund/spec.md`    |

## Language

One meaning per word, no synonyms. A word used by one bounded context only stays in its spec.

| Term | Meaning in this area |
| ---- | -------------------- |
| Amount | A sum of money in one currency, stored in minor units; of an invoice, the sum of the prices of its lines; of a payment, the amount charged (BA, 2026-10-19). |
| Blank | Empty or only white space; a non-breaking space (U+00A0, U+2007, U+202F) is white space (PO, BA, 2026-10-19). |
| Character | A Unicode code point: an emoji is one character (architect, 2026-10-19). |
| Currency | A three-letter ISO 4217 code a context accepts (BR-9) (BA, 2026-10-19). |
| Minor units | The smallest unit of a currency, counted as a whole number (BA, 2026-10-19). |
| Payment | A card charge CardPay accepted, against one invoice; a declined charge is no payment (BA, 2026-10-19). |
| Refused | What a bounded context of `billing` answers to a request one of its rules does not accept; nothing changes. Not Declined: CardPay declines a card (BA, 2026-10-19). |

## Area rules

| # | Rule | Trigger | Expected effect | Binds | Source |
| --- | --- | --- | --- | --- | --- |
| AR-1 | A bounded text is non-blank and holds at most 255 characters. | A text a rule of the area bounds by AR-1: a label, `orderId`, `customerId` (`invoice`); a card token, a charge reference (`payment`); the charge reference (`refund`) | A text blank or longer is refused, as the rule that bounds it says | invoice, payment, refund | architect, 2026-10-19 (the bound of BR-12); binds `refund`: architect, 2026-10-19 |
| AR-2 | Every amount is stored in minor units, with its currency: a `BIGINT` and a three-letter ISO 4217 code, never a decimal. | An amount stored: the prices and the amount of an invoice (`invoice`), the amount charged (`payment`), the amount of a refund (`refund`) | Stored as a whole number of minor units and the code of its currency | invoice, payment, refund | architect, 2026-10-19 (from `.apm/instructions/backend-database.instructions.md`); binds `refund`: architect, 2026-10-19 |
| AR-3 | A customer under fraud review can neither pay nor be refunded. | A pay (`payment`) or a refund (`refund`) of an invoice whose customer, the invoice's `customerId`, is under fraud review: listed in `application.yaml` under `commerce.fraud-review.customers`, matched exactly, until the risk team has a system. The risk team puts a customer under review and lifts it by editing the list, read at start-up: an edit counts at the next restart. A missing list, or a blank or overlong entry (AR-1), stops the start | Refused: 403, no body; nothing is recorded and CardPay is not called; no log names a customer under review | payment, refund | from source: REQ-4, REQ-6 (1.1); trigger: PO, 2026-10-19; list: PO, TL, architect, 2026-10-19; confirmed: owner, 2026-10-19 |
