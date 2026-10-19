package com.commerce.invoice.domain;

/** The one definition of a text the spec bounds: non-blank, at most 255 characters (BR-11, BR-12). */
final class Text {

    static final int MAX_CHARACTERS = 255;

    private Text() {
    }

    /** Blank is empty or only white space; a character is a Unicode code point. */
    static String require(String value, String rule) {
        if (value == null || value.isBlank() || value.codePointCount(0, value.length()) > MAX_CHARACTERS) {
            throw new InvalidInvoice(rule);
        }
        return value;
    }
}
