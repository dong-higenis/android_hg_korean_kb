package com.higenis.keyboard;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;

/**
 * Parses {@link EditorInfo#inputType} using Android bit masks (API 29 public).
 * Never compares raw {@code inputType == TYPE_CLASS_*} equality.
 */
public final class InputTypeHelper {

    /**
     * Immutable analysis of the current editor field.
     */
    public static final class Analysis {
        public final InputMode mode;
        public final boolean numberSigned;
        public final boolean numberDecimal;
        public final boolean password;
        public final boolean email;
        public final boolean uri;

        Analysis(InputMode mode,
                boolean numberSigned,
                boolean numberDecimal,
                boolean password,
                boolean email,
                boolean uri) {
            this.mode = mode != null ? mode : InputMode.TEXT;
            this.numberSigned = numberSigned;
            this.numberDecimal = numberDecimal;
            this.password = password;
            this.email = email;
            this.uri = uri;
        }

        /** Initial {@link LayoutPage} for this field (Symbols never restored here). */
        public LayoutPage initialLayoutPage() {
            switch (mode) {
                case NUMBER:
                    return LayoutPage.NUMBER;
                case PHONE:
                    return LayoutPage.PHONE;
                case TEXT:
                default:
                    return LayoutPage.LETTERS;
            }
        }

        @Override
        public String toString() {
            return "Analysis{mode=" + mode
                    + ", signed=" + numberSigned
                    + ", decimal=" + numberDecimal
                    + ", password=" + password
                    + ", email=" + email
                    + ", uri=" + uri + "}";
        }
    }

    private static final Analysis FALLBACK_TEXT =
            new Analysis(InputMode.TEXT, false, false, false, false, false);

    private InputTypeHelper() {
    }

    public static Analysis analyze(EditorInfo info) {
        if (info == null) {
            return FALLBACK_TEXT;
        }

        final int inputType = info.inputType;
        if (inputType == InputType.TYPE_NULL) {
            return FALLBACK_TEXT;
        }

        final int inputClass = inputType & InputType.TYPE_MASK_CLASS;
        final int variation = inputType & InputType.TYPE_MASK_VARIATION;

        if (inputClass == InputType.TYPE_CLASS_NUMBER) {
            boolean signed = (inputType & InputType.TYPE_NUMBER_FLAG_SIGNED) != 0;
            boolean decimal = (inputType & InputType.TYPE_NUMBER_FLAG_DECIMAL) != 0;
            boolean password = (variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            return new Analysis(InputMode.NUMBER, signed, decimal, password, false, false);
        }

        if (inputClass == InputType.TYPE_CLASS_PHONE) {
            return new Analysis(InputMode.PHONE, false, false, false, false, false);
        }

        if (inputClass == InputType.TYPE_CLASS_TEXT) {
            boolean password = isTextPasswordVariation(variation);
            boolean email = isEmailVariation(variation);
            boolean uri = isUriVariation(variation);
            return new Analysis(InputMode.TEXT, false, false, password, email, uri);
        }

        // DATETIME and unknown classes → safe text fallback.
        return FALLBACK_TEXT;
    }

    private static boolean isTextPasswordVariation(int variation) {
        return variation == InputType.TYPE_TEXT_VARIATION_PASSWORD
                || variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                || variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD;
    }

    private static boolean isEmailVariation(int variation) {
        return variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                || variation == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS;
    }

    private static boolean isUriVariation(int variation) {
        return variation == InputType.TYPE_TEXT_VARIATION_URI;
    }
}
