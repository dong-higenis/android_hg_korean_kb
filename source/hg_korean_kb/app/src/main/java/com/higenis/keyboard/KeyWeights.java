package com.higenis.keyboard;

/**
 * Relative horizontal weights for key width calculation.
 * <p>
 * Bottom LETTERS/SYMBOLS row includes Hide (▼); Space is slightly narrower
 * than the pre-Hide layout so 800x480 landscape remains usable.
 */
public final class KeyWeights {

    public static final float NORMAL = 1.0f;

    public static final float SHIFT = 1.4f;
    public static final float BACKSPACE = 1.4f;

    /** ?123 / ABC / #+= / 123 page switches. */
    public static final float PAGE = 1.4f;

    public static final float SYMBOLS = 1.1f;
    public static final float LANGUAGE = 1.1f;

    /** Comma / period on the bottom text row. */
    public static final float PUNCT = 0.7f;

    public static final float SPACE = 3.2f;
    public static final float ENTER = 1.1f;

    /** Keyboard dismiss (▼). Icon may replace the label later. */
    public static final float HIDE = 0.9f;

    /** Side spacer for centering the A-row (half-ish of a normal key). */
    public static final float ROW_INDENT = 0.5f;

    private KeyWeights() {
        // no instance
    }
}
