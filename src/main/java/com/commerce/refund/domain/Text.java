package com.commerce.refund.domain;

/** The one definition in {@code refund} of a text bounded by AR-1: the charge reference, a customer under fraud review. */
final class Text {

    static final int MAX_CHARACTERS = 255;

    private Text() {
    }

    /** A character is a Unicode code point (area language). */
    static String require(String value, String rule) {
        if (value == null || isBlank(value) || value.codePointCount(0, value.length()) > MAX_CHARACTERS) {
            throw new InvalidRefund(rule);
        }
        return value;
    }

    /** Blank is empty or only white space, non-breaking spaces included, which {@link String#isBlank()} keeps. */
    private static boolean isBlank(String value) {
        return value.codePoints().allMatch(c -> Character.isWhitespace(c) || Character.isSpaceChar(c));
    }
}
