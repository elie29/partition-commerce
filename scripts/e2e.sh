#!/usr/bin/env bash

# Feasibility check, end to end: commerce-app and payment-app both up, payment-app calling the real commerce-app.
# Runs the cases reachable from outside through docs/contracts/billing.openapi.json, and exits non-zero when any answer
# differs. Needs the two jars built (./mvnw package), curl, jq and uuidgen.
set -uo pipefail

REPO=$(cd "$(dirname "$0")/.." && pwd)
COMMERCE_PORT=${COMMERCE_PORT:-8080}
PAYMENT_PORT=${PAYMENT_PORT:-8081}
COMMERCE=http://localhost:$COMMERCE_PORT
PAYMENT=http://localhost:$PAYMENT_PORT
WORK=$(mktemp -d)
cd "$WORK"

java -jar "$REPO"/commerce-app/target/commerce-app-*.jar --server.port="$COMMERCE_PORT" > commerce.log 2>&1 &
C=$!
java -jar "$REPO"/payment-app/target/payment-app-*.jar --server.port="$PAYMENT_PORT" \
  --commerce.billing-internal.base-url="$COMMERCE" > payment.log 2>&1 &
P=$!

failed=0
finish() {
  kill -CONT "$C" 2>/dev/null
  kill "$C" "$P" 2>/dev/null
  wait 2>/dev/null
  if [ "$failed" -ne 0 ]; then
    echo "--- commerce.log (tail)"; tail -40 "$WORK/commerce.log"
    echo "--- payment.log (tail)"; tail -40 "$WORK/payment.log"
  fi
  rm -rf "$WORK"
}
trap finish EXIT

wait_up() {
  for _ in $(seq 90); do
    curl -sf "$1/actuator/health" > /dev/null && return 0
    sleep 1
  done
  echo "FAIL $1 never came up"; failed=1; exit 1
}

# call METHOD URL [BODY]: sets STATUS, BODY and SECONDS_TAKEN
call() {
  local out
  out=$(curl -s -o "$WORK/body" -w '%{http_code} %{time_total}' -X "$1" "$2" -H 'Accept: application/json' \
    ${3:+-H 'Content-Type: application/json' -d "$3"})
  STATUS=${out% *}
  SECONDS_TAKEN=${out#* }
  BODY=$(cat "$WORK/body")
}

# expect NAME STATUS [JQ_FILTER EXPECTED]
expect() {
  local actual=""
  [ $# -ge 4 ] && actual=$(echo "$BODY" | jq -r "$3" 2>/dev/null)
  if [ "$STATUS" = "$2" ] && { [ $# -lt 4 ] || [ "$actual" = "$4" ]; }; then
    echo "ok   $1 ($STATUS${4:+, $4})"
  else
    echo "FAIL $1: expected $2${4:+, $4}, got $STATUS${4:+, $actual} $BODY"; failed=1
  fi
}

issue() {
  call POST "$COMMERCE/invoices" "{\"orderId\":\"order-$(uuidgen)\",\"customerId\":\"cust-e2e\",\"lines\":[{\"label\":\"Widget\",\"price\":{\"amountMinor\":1500,\"currency\":\"EUR\"}}]}"
  expect "issue an invoice on commerce-app" 201 .status ISSUED
  ID=$(echo "$BODY" | jq -r .invoiceId)
}

PAY='{"cardToken":"tok_visa_4242"}'

wait_up "$COMMERCE"
wait_up "$PAYMENT"

issue; PAID=$ID
call POST "$PAYMENT/invoices/$PAID/pay" "$PAY";     expect "pay it on payment-app" 200 .status PAID
call GET "$COMMERCE/invoices/$PAID";                expect "commerce-app records it paid (BR-5)" 200 .status PAID
call POST "$PAYMENT/invoices/$PAID/pay" "$PAY";     expect "pay it again: refused (BR-20)" 409
call POST "$PAYMENT/invoices/$PAID/refunds" '{"amount":{"amountMinor":500,"currency":"EUR"}}'
expect "refund part of it" 201 .status SENT
call POST "$PAYMENT/invoices/$(uuidgen | tr 'A-Z' 'a-z')/pay" "$PAY"
expect "pay an unknown invoice: commerce-app answers 404 (BR-21)" 404

issue; CANCELLED=$ID
call POST "$COMMERCE/invoices/$CANCELLED/cancel";   expect "cancel it on commerce-app" 200 .status CANCELLED
call POST "$PAYMENT/invoices/$CANCELLED/pay" "$PAY"; expect "pay a cancelled invoice: refused (BR-20)" 409

issue; WAITING=$ID
kill -STOP "$C"
call POST "$PAYMENT/invoices/$WAITING/pay" "$PAY";  expect "commerce-app hangs: pay fails (BR-5)" 500
awk -v s="$SECONDS_TAKEN" 'BEGIN { exit !(s < 4) }' \
  && echo "ok   payment-app gave up in ${SECONDS_TAKEN}s (2-second limit)" \
  || { echo "FAIL payment-app waited ${SECONDS_TAKEN}s, more than the 2-second limit"; failed=1; }
kill -CONT "$C"
call GET "$COMMERCE/invoices/$WAITING";             expect "the invoice stays issued" 200 .status ISSUED

kill "$C"; wait "$C" 2>/dev/null
call POST "$PAYMENT/invoices/$WAITING/pay" "$PAY";  expect "commerce-app down: pay fails (BR-5)" 500
call POST "$PAYMENT/invoices/$PAID/refunds" '{"amount":{"amountMinor":100,"currency":"EUR"}}'
expect "commerce-app down: refund fails (BR-27)" 500

[ "$failed" -eq 0 ] && echo "e2e: all cases pass" || echo "e2e: failures above"
exit "$failed"
