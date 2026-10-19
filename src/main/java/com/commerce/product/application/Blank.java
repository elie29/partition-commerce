package com.commerce.product.application;

/** The one definition in {@code product} of Blank (`docs/specs/catalog/area.md`). */
final class Blank {

    private Blank() {
    }

    /** Empty or only white space, non-breaking spaces included, which {@link String#isBlank()} keeps. */
    static boolean is(String value) {
        return value.codePoints().allMatch(c -> Character.isWhitespace(c) || Character.isSpaceChar(c));
    }
}
