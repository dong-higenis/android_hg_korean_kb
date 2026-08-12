package com.higenis.keyboard;

/**
 * Mutable IME UI / input mode state — single source of truth for language,
 * shift, layout page, and current editor {@link InputMode}.
 * <p>
 * Language is persisted; layout page and input mode are per-field and not
 * stored in Preferences.
 */
public final class KeyboardState {

    private LanguageMode mLanguage = LanguageMode.KOREAN;
    private ShiftState mShiftState = ShiftState.OFF;
    private LayoutPage mLayoutPage = LayoutPage.LETTERS;
    private InputMode mInputMode = InputMode.TEXT;
    private boolean mNumberSigned;
    private boolean mNumberDecimal;
    private boolean mPasswordField;
    private boolean mEmailField;
    private boolean mUriField;

    public LanguageMode getLanguage() {
        return mLanguage;
    }

    public void setLanguage(LanguageMode language) {
        mLanguage = language != null ? language : LanguageMode.KOREAN;
    }

    public ShiftState getShiftState() {
        return mShiftState;
    }

    public void setShiftState(ShiftState shiftState) {
        mShiftState = shiftState != null ? shiftState : ShiftState.OFF;
    }

    public LayoutPage getLayoutPage() {
        return mLayoutPage;
    }

    public void setLayoutPage(LayoutPage layoutPage) {
        mLayoutPage = layoutPage != null ? layoutPage : LayoutPage.LETTERS;
    }

    public InputMode getInputMode() {
        return mInputMode;
    }

    public boolean isNumberSigned() {
        return mNumberSigned;
    }

    public boolean isNumberDecimal() {
        return mNumberDecimal;
    }

    public boolean isPasswordField() {
        return mPasswordField;
    }

    public boolean isEmailField() {
        return mEmailField;
    }

    public boolean isUriField() {
        return mUriField;
    }

    /**
     * Apply {@link InputTypeHelper} analysis for a new editor session.
     * Does not modify {@link #mLanguage}.
     */
    public void applyFieldAnalysis(InputTypeHelper.Analysis analysis) {
        if (analysis == null) {
            analysis = InputTypeHelper.analyze(null);
        }
        mInputMode = analysis.mode;
        mNumberSigned = analysis.numberSigned;
        mNumberDecimal = analysis.numberDecimal;
        mPasswordField = analysis.password;
        mEmailField = analysis.email;
        mUriField = analysis.uri;
        mShiftState = ShiftState.OFF;
        mLayoutPage = analysis.initialLayoutPage();
    }

    public boolean isLettersPage() {
        return mLayoutPage == LayoutPage.LETTERS;
    }

    public boolean isSymbolsPage() {
        return mLayoutPage == LayoutPage.SYMBOLS_1
                || mLayoutPage == LayoutPage.SYMBOLS_2;
    }

    /** NUMBER / PHONE — no ?123 / 한/영 / HangulComposer. */
    public boolean isFieldDrivenKeypad() {
        return mLayoutPage == LayoutPage.NUMBER || mLayoutPage == LayoutPage.PHONE;
    }

    @Override
    public String toString() {
        return "KeyboardState{lang=" + mLanguage
                + ", shift=" + mShiftState
                + ", page=" + mLayoutPage
                + ", inputMode=" + mInputMode
                + ", signed=" + mNumberSigned
                + ", decimal=" + mNumberDecimal
                + "}";
    }
}
