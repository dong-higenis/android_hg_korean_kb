package com.higenis.keyboard;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Persists IME preferences using the app's default Credential Protected Storage
 * ({@link Context#getSharedPreferences}). Safe with {@code directBootAware=false}.
 * <p>
 * Language is stored as a stable token ({@code "en"} / {@code "ko"}), not
 * {@link Enum#name()}, so renames of {@link LanguageMode} constants do not
 * break persisted values.
 */
public final class PreferencesHelper {

    private static final String PREFS_NAME = "hg_keyboard_preferences";
    private static final String KEY_LANGUAGE_MODE = "language_mode";

    /** Stable preference tokens — do not change once shipped. */
    private static final String VALUE_ENGLISH = "en";
    private static final String VALUE_KOREAN = "ko";

    /** Product default when no preference has been saved yet. */
    public static final LanguageMode DEFAULT_LANGUAGE = LanguageMode.KOREAN;

    private final SharedPreferences mPrefs;

    public PreferencesHelper(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context == null");
        }
        // Use application context to avoid leaking the Service instance.
        mPrefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public LanguageMode getLanguageMode() {
        String value = mPrefs.getString(KEY_LANGUAGE_MODE, null);
        return fromStorageValue(value);
    }

    /**
     * Persists language. Call only when the user changes 한/영 — not on every key.
     */
    public void setLanguageMode(LanguageMode mode) {
        if (mode == null) {
            mode = DEFAULT_LANGUAGE;
        }
        mPrefs.edit().putString(KEY_LANGUAGE_MODE, toStorageValue(mode)).apply();
    }

    static String toStorageValue(LanguageMode mode) {
        if (mode == LanguageMode.ENGLISH) {
            return VALUE_ENGLISH;
        }
        return VALUE_KOREAN;
    }

    static LanguageMode fromStorageValue(String value) {
        if (VALUE_ENGLISH.equals(value)) {
            return LanguageMode.ENGLISH;
        }
        if (VALUE_KOREAN.equals(value)) {
            return LanguageMode.KOREAN;
        }
        // Missing or unknown → product default (Korean).
        return DEFAULT_LANGUAGE;
    }
}
