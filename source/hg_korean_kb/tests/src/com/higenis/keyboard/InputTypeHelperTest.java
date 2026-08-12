package com.higenis.keyboard;

/**
 * Host JVM checks for InputType bit-mask rules (no android.jar required).
 * Constant values match {@link android.text.InputType} on API 29.
 */
public final class InputTypeHelperTest {

    // android.text.InputType (API 29)
    private static final int TYPE_MASK_CLASS = 0x0000000f;
    private static final int TYPE_CLASS_TEXT = 0x00000001;
    private static final int TYPE_CLASS_NUMBER = 0x00000002;
    private static final int TYPE_CLASS_PHONE = 0x00000003;
    private static final int TYPE_NUMBER_FLAG_SIGNED = 0x00001000;
    private static final int TYPE_NUMBER_FLAG_DECIMAL = 0x00002000;
    private static final int TYPE_NUMBER_VARIATION_PASSWORD = 0x00000010;
    private static final int TYPE_MASK_VARIATION = 0x000000ff;

    private InputTypeHelperTest() {
    }

    public static void runAll() {
        assertEq(InputMode.TEXT, classify(TYPE_CLASS_TEXT));
        assertEq(InputMode.NUMBER, classify(TYPE_CLASS_NUMBER));
        assertEq(InputMode.NUMBER, classify(TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_DECIMAL));
        assertEq(InputMode.NUMBER, classify(TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_SIGNED));
        assertEq(InputMode.NUMBER, classify(
                TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_SIGNED | TYPE_NUMBER_FLAG_DECIMAL));
        assertEq(InputMode.PHONE, classify(TYPE_CLASS_PHONE));
        assertEq(InputMode.NUMBER, classify(
                TYPE_CLASS_NUMBER | TYPE_NUMBER_VARIATION_PASSWORD));

        assertEq(Boolean.TRUE, Boolean.valueOf(hasFlag(
                TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_DECIMAL, TYPE_NUMBER_FLAG_DECIMAL)));
        assertEq(Boolean.FALSE, Boolean.valueOf(
                hasFlag(TYPE_CLASS_NUMBER, TYPE_NUMBER_FLAG_DECIMAL)));
        assertEq(Boolean.TRUE, Boolean.valueOf(hasFlag(
                TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_SIGNED, TYPE_NUMBER_FLAG_SIGNED)));

        int decimalField = TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_DECIMAL;
        // Raw equality is wrong for field detection:
        assertEq(Boolean.FALSE, Boolean.valueOf(decimalField == TYPE_CLASS_NUMBER));
        assertEq(TYPE_CLASS_NUMBER, decimalField & TYPE_MASK_CLASS);

        assertEq(Boolean.TRUE, Boolean.valueOf(
                (TYPE_NUMBER_VARIATION_PASSWORD & TYPE_MASK_VARIATION)
                        == TYPE_NUMBER_VARIATION_PASSWORD));

        // Layout mapping
        assertEq(LayoutPage.LETTERS, pageFor(InputMode.TEXT));
        assertEq(LayoutPage.NUMBER, pageFor(InputMode.NUMBER));
        assertEq(LayoutPage.PHONE, pageFor(InputMode.PHONE));

        System.out.println("InputTypeHelperTest: all passed");
    }

    private static InputMode classify(int inputType) {
        int inputClass = inputType & TYPE_MASK_CLASS;
        if (inputClass == TYPE_CLASS_NUMBER) {
            return InputMode.NUMBER;
        }
        if (inputClass == TYPE_CLASS_PHONE) {
            return InputMode.PHONE;
        }
        return InputMode.TEXT;
    }

    private static boolean hasFlag(int inputType, int flag) {
        return (inputType & flag) != 0;
    }

    private static LayoutPage pageFor(InputMode mode) {
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

    private static void assertEq(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("expected=" + expected + " actual=" + actual);
        }
    }
}
