package com.higenis.keyboard;

/**
 * Visual / behavioral category of a key.
 */
public enum KeyType {
    /** Letter, digit, or punctuation that inserts text. */
    CHARACTER,

    /** Modifier or command key (Shift, Backspace, Enter, 한/영, ?123). */
    ACTION,

    /** Space bar (wider key, distinct style optional). */
    SPACE,

    /**
     * Invisible spacer used to indent a row (for example row 2 of QWERTY).
     * Not drawn and not touchable.
     */
    SPACER
}
