package com.higenis.keyboard;

/**
 * In-IME language mode toggled by the 한/영 key.
 * <p>
 * Persistence uses stable tokens via {@link PreferencesHelper}, not
 * {@link #name()}.
 */
public enum LanguageMode {
    ENGLISH,
    KOREAN
}
