# billing

The language and the area rules shared by the bounded contexts of this area. A rule one bounded context owns stays in its spec; a statement that spans areas or layers goes to the invariant register.

## Bounded contexts

| Bounded context | Spec                |
| --------------- | ------------------- |
| invoice         | `invoice/spec.md`   |
| payment         | `payment/spec.md`   |

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
| AR-1 | A bounded text is non-blank and holds at most 255 characters. | A text a rule of the area bounds by AR-1: a label, `orderId`, `customerId` (`invoice`); a card token, a charge reference (`payment`) | A text blank or longer is refused, as the rule that bounds it says | invoice, payment | architect, 2026-10-19 (the bound of BR-12); `refund` merged into `payment`: 2026-10-20 |

AR-3 — moved on 2026-10-20 (BA): BR-32 of `payment/spec.md`, the one bounded context it binds since `refund` merged into `payment`.

AR-2 — removed on 2026-10-19 ([architect]): INV-DATA-001 of `../invariants.md`; the `BIGINT` column in the database rule; three letters and whole numbers in the words Currency and Minor units.
