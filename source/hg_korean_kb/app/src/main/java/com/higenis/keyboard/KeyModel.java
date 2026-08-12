package com.higenis.keyboard;

/**
 * Pure key definition — no screen coordinates.
 * <p>
 * {@link #code} is an {@code int} Unicode code unit / special key code
 * (BMP Hangul jamo fit in {@code char}; {@code int} stays for special keys
 * and future expansion).
 * <p>
 * {@link #label} / {@link #shiftedLabel} are display strings. Layout tables
 * are immutable — never mutate these fields at runtime. {@link KeyboardView}
 * picks the visible label from {@link ShiftState}.
 */
public final class KeyModel {

    private final int code;
    private final String label;
    /** Alternate label when Shift is ONCE or LOCKED; {@code null} if unchanged. */
    private final String shiftedLabel;
    private final float widthWeight;
    private final KeyType type;
    private final int iconResId;

    public KeyModel(int code, String label, String shiftedLabel,
            float widthWeight, KeyType type) {
        this(code, label, shiftedLabel, widthWeight, type, 0);
    }

    public KeyModel(int code, String label, String shiftedLabel,
            float widthWeight, KeyType type, int iconResId) {
        this.code = code;
        this.label = label != null ? label : "";
        this.shiftedLabel = shiftedLabel;
        this.widthWeight = widthWeight;
        this.type = type != null ? type : KeyType.CHARACTER;
        this.iconResId = iconResId;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getShiftedLabel() {
        return shiftedLabel;
    }

    public boolean hasShiftedLabel() {
        return shiftedLabel != null && shiftedLabel.length() > 0;
    }

    public float getWidthWeight() {
        return widthWeight;
    }

    public KeyType getType() {
        return type;
    }

    public int getIconResId() {
        return iconResId;
    }

    public boolean hasIcon() {
        return iconResId != 0;
    }

    public boolean isTouchable() {
        return type != KeyType.SPACER && code != KeyCodes.NONE;
    }

    /**
     * Label to show / log for the given shift state (does not mutate this model).
     */
    public String resolveLabel(ShiftState shiftState) {
        if (shiftState != null
                && shiftState != ShiftState.OFF
                && hasShiftedLabel()) {
            return shiftedLabel;
        }
        return label;
    }

    /** English letter: lowercase code/label, uppercase shiftedLabel. */
    public static KeyModel latinLetter(char lower) {
        char base = Character.toLowerCase(lower);
        char upper = Character.toUpperCase(base);
        return new KeyModel(base, String.valueOf(base), String.valueOf(upper),
                KeyWeights.NORMAL, KeyType.CHARACTER);
    }

    /** Punctuation / symbol with no shift alternate. */
    public static KeyModel character(char ch, String label) {
        return character(ch, label, KeyWeights.NORMAL);
    }

    /** Punctuation / symbol with an explicit width weight. */
    public static KeyModel character(char ch, String label, float weight) {
        return new KeyModel(ch, label, null, weight, KeyType.CHARACTER);
    }

    /** Hangul jamo without a shifted form. */
    public static KeyModel hangul(char jamo) {
        return new KeyModel(jamo, String.valueOf(jamo), null,
                KeyWeights.NORMAL, KeyType.CHARACTER);
    }

    /** Hangul jamo with shifted keycap (ㅂ→ㅃ, ㅐ→ㅒ, …). */
    public static KeyModel hangul(char jamo, char shiftedJamo) {
        return new KeyModel(jamo, String.valueOf(jamo), String.valueOf(shiftedJamo),
                KeyWeights.NORMAL, KeyType.CHARACTER);
    }

    public static KeyModel action(int code, String label, float weight) {
        return new KeyModel(code, label, null, weight, KeyType.ACTION);
    }

    public static KeyModel space() {
        return new KeyModel(KeyCodes.SPACE, "", null, KeyWeights.SPACE, KeyType.SPACE);
    }

    /**
     * Dismiss soft keyboard. Label is a temporary glyph; may later use {@link #iconResId}.
     */
    public static KeyModel hideKeyboard() {
        return action(KeyCodes.HIDE_KEYBOARD, "\u25BC", KeyWeights.HIDE);
    }

    public static KeyModel spacer(float weight) {
        return new KeyModel(KeyCodes.NONE, "", null, weight, KeyType.SPACER);
    }
}
