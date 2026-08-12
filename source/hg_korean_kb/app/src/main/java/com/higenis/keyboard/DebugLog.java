package com.higenis.keyboard;

import android.util.Log;

/**
 * Centralized debug logging for HG Keyboard.
 * <p>
 * {@link #DEBUG} defaults to {@code false} for product images. Enable only on
 * eng/userdebug during bring-up. Never log typed characters / password text
 * even when DEBUG is true — call sites must pass metadata only (lengths, enums).
 */
public final class DebugLog {

    public static final String TAG = "HGKeyboard";

    /**
     * Verbose IME logs. Keep {@code false} in shipping builds.
     * Set {@code true} temporarily for eng bring-up if needed.
     */
    public static final boolean DEBUG = false;

    private DebugLog() {
        // no instance
    }

    public static void d(String message) {
        if (DEBUG) {
            Log.d(TAG, message);
        }
    }

    public static void i(String message) {
        if (DEBUG) {
            Log.i(TAG, message);
        }
    }

    public static void w(String message) {
        if (DEBUG) {
            Log.w(TAG, message);
        }
    }

    public static void e(String message) {
        Log.e(TAG, message);
    }

    public static void e(String message, Throwable tr) {
        Log.e(TAG, message, tr);
    }
}
