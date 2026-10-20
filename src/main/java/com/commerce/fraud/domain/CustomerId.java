package com.commerce.fraud.domain;

/**
 * The one definition in {@code fraud} of BR-7: the {@code customerId} of a place, the text billing holds, kept as it is,
 * non-blank and at most 255 characters, a character a Unicode code point. Its text is never written to a log (BR-9).
 */
public record CustomerId(String value) {

    static final int MAX_CHARACTERS = 255;

    /** @throws InvalidCustomerId when the text is missing, blank or longer */
    public CustomerId {
        if (value == null || isBlank(value) || value.codePointCount(0, value.length()) > MAX_CHARACTERS) {
            throw new InvalidCustomerId();
        }
    }

    /** Blank is empty or only white space, non-breaking spaces included, which {@link String#isBlank()} keeps. */
    private static boolean isBlank(String value) {
        return value.codePoints().allMatch(c -> Character.isWhitespace(c) || Character.isSpaceChar(c));
    }

    @Override
    public String toString() {
        return "CustomerId[…]";
    }
}
