package com.commerce.invoice.domain;

/** The one definition of a text the spec bounds as a label (BR-12): BR-11, BR-22, the charge reference (BR-5). */
final class Text {

    static final int MAX_CHARACTERS = 255;

    private Text() {
    }

    /** A character is a Unicode code point. */
    static String require(String value, String rule) {
        if (value == null || isBlank(value) || value.codePointCount(0, value.length()) > MAX_CHARACTERS) {
            throw new InvalidInvoice(rule);
        }
        return value;
    }

    /** Blank is empty or only white space, non-breaking spaces included, which {@link String#isBlank()} keeps. */
    private static boolean isBlank(String value) {
        return value.codePoints().allMatch(c -> Character.isWhitespace(c) || Character.isSpaceChar(c));
    }
}
