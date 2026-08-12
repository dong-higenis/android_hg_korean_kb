package com.higenis.keyboard;

/**
 * Editor field category derived from {@link android.view.inputmethod.EditorInfo#inputType}.
 * Distinct from {@link LayoutPage}, which is the currently displayed key grid.
 */
public enum InputMode {
    /** TYPE_CLASS_TEXT and other fallbacks — Hangul/English LETTERS (+ Symbols). */
    TEXT,

    /** TYPE_CLASS_NUMBER — dedicated number keypad. */
    NUMBER,

    /** TYPE_CLASS_PHONE — phone keypad. */
    PHONE
}
