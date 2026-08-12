package com.higenis.keyboard;

/**
 * Special key codes for HG Keyboard.
 * <p>
 * Character keys use their Unicode code point as {@code code}.
 * Special keys use negative constants so they never collide with Unicode.
 */
public final class KeyCodes {

    public static final int NONE = 0;

    public static final int SHIFT = -1;
    public static final int BACKSPACE = -2;
    public static final int ENTER = -3;
    public static final int SPACE = -4;
    public static final int LANGUAGE = -5;

    /** LETTERS → SYMBOLS_1 (?123). */
    public static final int SWITCH_TO_SYMBOLS = -6;

    /** SYMBOLS_1 → SYMBOLS_2 (#+=). */
    public static final int SWITCH_TO_SYMBOLS_2 = -7;

    /** SYMBOLS_2 → SYMBOLS_1 (123). */
    public static final int SWITCH_TO_SYMBOLS_1 = -8;

    /** SYMBOLS_* → LETTERS (ABC). */
    public static final int SWITCH_TO_LETTERS = -9;

    /** Dismiss soft keyboard (no Navigation Bar devices). */
    public static final int HIDE_KEYBOARD = -10;

    /** Prefer {@link #SWITCH_TO_SYMBOLS}. Kept as an alias for older call sites. */
    public static final int SYMBOLS = SWITCH_TO_SYMBOLS;

    private KeyCodes() {
        // no instance
    }

    /**
     * Debug name for a key code. Never returns typed character labels —
     * use code metadata only (safe for password / PII fields).
     */
    public static String toDebugName(int code) {
        switch (code) {
            case SHIFT:
                return "SHIFT";
            case BACKSPACE:
                return "BACKSPACE";
            case ENTER:
                return "ENTER";
            case SPACE:
                return "SPACE";
            case LANGUAGE:
                return "LANGUAGE";
            case SWITCH_TO_SYMBOLS:
                return "SWITCH_TO_SYMBOLS";
            case SWITCH_TO_SYMBOLS_2:
                return "SWITCH_TO_SYMBOLS_2";
            case SWITCH_TO_SYMBOLS_1:
                return "SWITCH_TO_SYMBOLS_1";
            case SWITCH_TO_LETTERS:
                return "SWITCH_TO_LETTERS";
            case HIDE_KEYBOARD:
                return "HIDE_KEYBOARD";
            case NONE:
                return "NONE";
            default:
                return "CODE(" + code + ")";
        }
    }
}
