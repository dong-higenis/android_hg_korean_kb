package com.higenis.keyboard;

/**
 * Currently displayed key grid. Source of truth: {@link KeyboardState}.
 * Distinct from {@link InputMode} (editor field category).
 */
public enum LayoutPage {
    /** QWERTY letters — English or Korean via {@link LanguageMode}. */
    LETTERS,

    /** Primary ?123 digits / symbols (text fields only). */
    SYMBOLS_1,

    /** Secondary #+= symbols (text fields only). */
    SYMBOLS_2,

    /** Field-driven number keypad ({@link InputMode#NUMBER}). */
    NUMBER,

    /** Field-driven phone keypad ({@link InputMode#PHONE}). */
    PHONE
}
