package com.higenis.keyboard;

import android.os.SystemClock;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.higenis.keyboard.input.CompositionResult;
import com.higenis.keyboard.input.HangulComposer;

/**
 * Routes key events from {@link KeyboardView} to {@link InputConnection}
 * and owns {@link KeyboardState} + {@link HangulComposer} orchestration.
 * <p>
 * Does not cache {@link InputConnection}. HangulComposer stays pure Java;
 * all IC calls happen here. Composer never reads editor text via
 * {@code getTextBeforeCursor}.
 * <p>
 * Soft-keyboard dismiss goes through {@link KeyboardHost} — this class never
 * casts to {@link android.inputmethodservice.InputMethodService}.
 */
public class KeyboardController implements KeyboardView.OnKeyboardActionListener {

    public static final long SHIFT_DOUBLE_TAP_TIMEOUT_MS = 400L;

    private final InputSession mInputSession;
    private final PreferencesHelper mPreferences;
    private final KeyboardHost mHost;
    private final KeyboardState mState = new KeyboardState();
    private final HangulComposer mHangulComposer = new HangulComposer();

    private KeyboardView mKeyboardView;
    private long mLastShiftTapUptimeMs;
    private int mIgnoreSelectionUpdates;

    public KeyboardController(InputSession inputSession, PreferencesHelper preferences,
            KeyboardHost host) {
        if (inputSession == null) {
            throw new IllegalArgumentException("inputSession == null");
        }
        if (preferences == null) {
            throw new IllegalArgumentException("preferences == null");
        }
        if (host == null) {
            throw new IllegalArgumentException("host == null");
        }
        mInputSession = inputSession;
        mPreferences = preferences;
        mHost = host;

        mState.setLanguage(mPreferences.getLanguageMode());
        mState.setShiftState(ShiftState.OFF);
        mState.setLayoutPage(LayoutPage.LETTERS);
    }

    public void attachKeyboardView(KeyboardView keyboardView) {
        mKeyboardView = keyboardView;
        if (mKeyboardView != null) {
            mKeyboardView.setOnKeyboardActionListener(this);
            syncViewFromState();
        }
    }

    public KeyboardState getState() {
        return mState;
    }

    public HangulComposer getHangulComposer() {
        return mHangulComposer;
    }

    /**
     * New editor session: HangulComposer reset (previous field already finished),
     * {@link InputTypeHelper} selects NUMBER / PHONE / LETTERS. Language from
     * Preferences is never modified by numeric fields.
     */
    public void onStartInputView(EditorInfo info, boolean restarting) {
        mHangulComposer.reset();
        mIgnoreSelectionUpdates = 0;
        mLastShiftTapUptimeMs = 0L;

        InputTypeHelper.Analysis analysis = InputTypeHelper.analyze(info);
        mState.applyFieldAnalysis(analysis);

        syncViewFromState();
        if (DebugLog.DEBUG) {
            DebugLog.d("KeyboardController.onStartInputView restarting=" + restarting
                    + " " + analysis + " " + mState);
        }
    }

    public void onFinishInputView(boolean finishingInput) {
        finishHangulComposition();
        mIgnoreSelectionUpdates = 0;
        mLastShiftTapUptimeMs = 0L;
    }

    public void onFinishInput() {
        finishHangulComposition();
        mIgnoreSelectionUpdates = 0;
        mState.setShiftState(ShiftState.OFF);
        mLastShiftTapUptimeMs = 0L;
        syncViewFromState();
    }

    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
            int newSelStart, int newSelEnd,
            int candidatesStart, int candidatesEnd) {
        if (mIgnoreSelectionUpdates > 0) {
            mIgnoreSelectionUpdates--;
            return;
        }
        if (!mHangulComposer.hasComposition()) {
            return;
        }

        if (candidatesStart < 0 || candidatesEnd < 0) {
            mHangulComposer.reset();
            if (DebugLog.DEBUG) {
                DebugLog.d("Hangul composition reset: editor has no composing span");
            }
            return;
        }

        if (newSelStart < candidatesStart || newSelEnd > candidatesEnd
                || newSelStart > candidatesEnd || newSelEnd < candidatesStart) {
            abandonHangulComposition();
            if (DebugLog.DEBUG) {
                DebugLog.d("Hangul composition abandoned: selection left composing region");
            }
        }
    }

    @Override
    public void onKeyPressed(KeyModel key) {
        if (key == null || !key.isTouchable()) {
            return;
        }

        final int code = key.getCode();
        switch (code) {
            case KeyCodes.SHIFT:
                handleShift();
                return;
            case KeyCodes.BACKSPACE:
                handleBackspace();
                return;
            case KeyCodes.ENTER:
                handleEnter();
                return;
            case KeyCodes.SPACE:
                handleSpace();
                return;
            case KeyCodes.LANGUAGE:
                handleLanguageToggle();
                return;
            case KeyCodes.SWITCH_TO_SYMBOLS:
                if (mState.getInputMode() == InputMode.TEXT) {
                    switchToSymbols1FromLetters();
                }
                return;
            case KeyCodes.SWITCH_TO_SYMBOLS_2:
                if (mState.getInputMode() == InputMode.TEXT) {
                    switchLayoutPage(LayoutPage.SYMBOLS_2);
                }
                return;
            case KeyCodes.SWITCH_TO_SYMBOLS_1:
                if (mState.getInputMode() == InputMode.TEXT) {
                    switchLayoutPage(LayoutPage.SYMBOLS_1);
                }
                return;
            case KeyCodes.SWITCH_TO_LETTERS:
                if (mState.getInputMode() == InputMode.TEXT) {
                    switchToLetters();
                }
                return;
            case KeyCodes.HIDE_KEYBOARD:
                handleHideKeyboard();
                return;
            default:
                break;
        }

        if (key.getType() == KeyType.CHARACTER) {
            handleCharacter(key);
        } else {
            DebugLog.d(KeyCodes.toDebugName(code) + " pressed");
        }
    }

    /**
     * Finish Hangul composition, clear one-shot/locked Shift, then ask the host
     * to dismiss the soft keyboard ({@code requestHideSelf}).
     */
    private void handleHideKeyboard() {
        finishHangulComposition();
        mState.setShiftState(ShiftState.OFF);
        mLastShiftTapUptimeMs = 0L;
        syncViewFromState();
        if (DebugLog.DEBUG) {
            DebugLog.d("HIDE_KEYBOARD");
        }
        mHost.requestHideKeyboard();
    }

    /**
     * ?123 from LETTERS: finish Hangul, then open SYMBOLS_1. Language unchanged.
     */
    private void switchToSymbols1FromLetters() {
        finishHangulComposition();
        mState.setLayoutPage(LayoutPage.SYMBOLS_1);
        mState.setShiftState(ShiftState.OFF);
        mLastShiftTapUptimeMs = 0L;
        syncViewFromState();
        if (DebugLog.DEBUG) {
            DebugLog.d("Layout → SYMBOLS_1 (lang=" + mState.getLanguage() + ")");
        }
    }

    /** ABC: return to LETTERS for the current language. */
    private void switchToLetters() {
        mState.setLayoutPage(LayoutPage.LETTERS);
        mState.setShiftState(ShiftState.OFF);
        mLastShiftTapUptimeMs = 0L;
        syncViewFromState();
        if (DebugLog.DEBUG) {
            DebugLog.d("Layout → LETTERS (lang=" + mState.getLanguage() + ")");
        }
    }

    private void switchLayoutPage(LayoutPage page) {
        mState.setLayoutPage(page);
        mState.setShiftState(ShiftState.OFF);
        mLastShiftTapUptimeMs = 0L;
        syncViewFromState();
        if (DebugLog.DEBUG) {
            DebugLog.d("Layout → " + page);
        }
    }

    /**
     * 한/영: toggle language + prefs. Stay on current Symbols page if any.
     * Ignored on NUMBER / PHONE (those layouts have no 한/영 key).
     */
    private void handleLanguageToggle() {
        if (mState.isFieldDrivenKeypad()) {
            return;
        }

        finishHangulComposition();

        LanguageMode next = (mState.getLanguage() == LanguageMode.KOREAN)
                ? LanguageMode.ENGLISH
                : LanguageMode.KOREAN;

        mState.setLanguage(next);
        mState.setShiftState(ShiftState.OFF);
        mLastShiftTapUptimeMs = 0L;
        mPreferences.setLanguageMode(next);

        syncViewFromState();
        if (DebugLog.DEBUG) {
            DebugLog.d("Language → " + next + " page=" + mState.getLayoutPage());
        }
    }

    private void handleCharacter(KeyModel key) {
        // Symbols / NUMBER / PHONE: never feed HangulComposer.
        if (!mState.isLettersPage()) {
            commitCharacterText(key);
            return;
        }

        if (mState.getLanguage() == LanguageMode.KOREAN && isHangulCompatibilityJamo(key)) {
            handleKoreanJamoKey(key);
            return;
        }

        if (mState.getLanguage() == LanguageMode.KOREAN) {
            finishHangulComposition();
        }
        commitCharacterText(key);
    }

    private void handleKoreanJamoKey(KeyModel key) {
        String effective = key.resolveLabel(mState.getShiftState());
        if (effective == null || effective.length() == 0) {
            return;
        }
        int jamoCode = effective.charAt(0);

        CompositionResult result = mHangulComposer.process(jamoCode);
        applyCompositionResult(result);
        clearOneShotShiftIfNeeded();
    }

    private void commitCharacterText(KeyModel key) {
        String text = key.resolveLabel(mState.getShiftState());
        if (text == null || text.length() == 0) {
            if (key.getCode() <= 0) {
                return;
            }
            text = String.valueOf((char) key.getCode());
        }

        InputConnection ic = mInputSession.getInputConnection();
        if (ic == null) {
            DebugLog.w("commitText skipped: InputConnection is null");
            return;
        }

        boolean ok = ic.commitText(text, 1);
        expectSelectionUpdate();
        if (!ok && DebugLog.DEBUG) {
            DebugLog.d("commitText failed len=" + text.length());
        }

        // Symbols pages keep Shift OFF; letters may clear one-shot Shift.
        if (mState.isLettersPage()) {
            clearOneShotShiftIfNeeded();
        }
    }

    private static boolean isHangulCompatibilityJamo(KeyModel key) {
        int c = key.getCode();
        return c >= 0x3131 && c <= 0x318E;
    }

    private void clearOneShotShiftIfNeeded() {
        if (mState.getShiftState() == ShiftState.ONCE) {
            mState.setShiftState(ShiftState.OFF);
            mLastShiftTapUptimeMs = 0L;
            syncViewFromState();
        }
    }

    private void handleSpace() {
        finishHangulComposition();

        InputConnection ic = mInputSession.getInputConnection();
        if (ic == null) {
            DebugLog.w("SPACE skipped: InputConnection is null");
            return;
        }
        boolean ok = ic.commitText(" ", 1);
        expectSelectionUpdate();
        if (!ok && DebugLog.DEBUG) {
            DebugLog.d("commitText failed for SPACE");
        }
    }

    private void handleBackspace() {
        if (mHangulComposer.hasComposition()) {
            CompositionResult result = mHangulComposer.backspace();
            applyCompositionResult(result);
            return;
        }
        deleteBackwardNormal();
    }

    private void deleteBackwardNormal() {
        InputConnection ic = mInputSession.getInputConnection();
        if (ic == null) {
            DebugLog.w("BACKSPACE skipped: InputConnection is null");
            return;
        }

        CharSequence selected = ic.getSelectedText(0);
        if (selected != null && selected.length() > 0) {
            boolean ok = ic.commitText("", 1);
            expectSelectionUpdate();
            if (!ok && DebugLog.DEBUG) {
                DebugLog.d("commitText(\"\") for selection delete failed");
            }
            return;
        }

        boolean ok = ic.deleteSurroundingTextInCodePoints(1, 0);
        expectSelectionUpdate();
        if (!ok) {
            ok = ic.deleteSurroundingText(1, 0);
            if (!ok && DebugLog.DEBUG) {
                DebugLog.d("deleteSurroundingTextInCodePoints/Text failed");
            }
        }
    }

    private void handleEnter() {
        finishHangulComposition();

        InputConnection ic = mInputSession.getInputConnection();
        if (ic == null) {
            DebugLog.w("ENTER skipped: InputConnection is null");
            return;
        }

        EditorInfo ei = mInputSession.getEditorInfo();
        if (ei == null) {
            commitNewline(ic);
            return;
        }

        final int action = ei.imeOptions & EditorInfo.IME_MASK_ACTION;
        final boolean noEnterAction =
                (ei.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0;
        final boolean multiLine =
                (ei.inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0;

        if (!multiLine
                && !noEnterAction
                && action != EditorInfo.IME_ACTION_NONE
                && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
            boolean ok = ic.performEditorAction(action);
            expectSelectionUpdate();
            if (!ok) {
                if (DebugLog.DEBUG) {
                    DebugLog.d("performEditorAction(" + action + ") failed; fallback newline");
                }
                commitNewline(ic);
            }
            return;
        }

        commitNewline(ic);
    }

    private void commitNewline(InputConnection ic) {
        boolean ok = ic.commitText("\n", 1);
        expectSelectionUpdate();
        if (!ok && DebugLog.DEBUG) {
            DebugLog.d("commitText failed for newline");
        }
    }

    private void handleShift() {
        if (!mState.isLettersPage()) {
            return;
        }

        final long now = SystemClock.uptimeMillis();
        final ShiftState current = mState.getShiftState();

        if (current == ShiftState.LOCKED) {
            mState.setShiftState(ShiftState.OFF);
            mLastShiftTapUptimeMs = 0L;
        } else if (current == ShiftState.ONCE) {
            if (mLastShiftTapUptimeMs > 0L
                    && (now - mLastShiftTapUptimeMs) <= SHIFT_DOUBLE_TAP_TIMEOUT_MS) {
                mState.setShiftState(ShiftState.LOCKED);
                mLastShiftTapUptimeMs = 0L;
            } else {
                mState.setShiftState(ShiftState.OFF);
                mLastShiftTapUptimeMs = 0L;
            }
        } else {
            mState.setShiftState(ShiftState.ONCE);
            mLastShiftTapUptimeMs = now;
        }

        syncViewFromState();
        if (DebugLog.DEBUG) {
            DebugLog.d("Shift → " + mState.getShiftState());
        }
    }

    private void finishHangulComposition() {
        if (!mHangulComposer.hasComposition()) {
            return;
        }
        CompositionResult result = mHangulComposer.flush();
        applyCompositionResult(result);
    }

    private void abandonHangulComposition() {
        InputConnection ic = mInputSession.getInputConnection();
        if (ic != null) {
            ic.finishComposingText();
            expectSelectionUpdate();
        }
        mHangulComposer.reset();
    }

    private void applyCompositionResult(CompositionResult result) {
        if (result == null || result.getAction() == CompositionResult.Action.NONE) {
            return;
        }

        InputConnection ic = mInputSession.getInputConnection();
        if (ic == null) {
            DebugLog.w("CompositionResult skipped: InputConnection is null action="
                    + result.getAction());
            return;
        }

        switch (result.getAction()) {
            case UPDATE_COMPOSING: {
                boolean ok = ic.setComposingText(result.getComposingText(), 1);
                expectSelectionUpdate();
                if (!ok && DebugLog.DEBUG) {
                    DebugLog.d("setComposingText failed action=UPDATE_COMPOSING");
                }
                break;
            }
            case COMMIT_AND_COMPOSE: {
                boolean okCommit = ic.commitText(result.getCommitText(), 1);
                if (!okCommit && DebugLog.DEBUG) {
                    DebugLog.d("commitText failed action=COMMIT_AND_COMPOSE");
                }
                boolean okCompose = ic.setComposingText(result.getComposingText(), 1);
                expectSelectionUpdate();
                if (!okCompose && DebugLog.DEBUG) {
                    DebugLog.d("setComposingText failed action=COMMIT_AND_COMPOSE");
                }
                break;
            }
            case FINISH_COMPOSING: {
                boolean ok = ic.finishComposingText();
                expectSelectionUpdate();
                if (!ok && DebugLog.DEBUG) {
                    DebugLog.d("finishComposingText failed");
                }
                break;
            }
            case CLEAR_COMPOSING: {
                clearCurrentComposition(ic);
                break;
            }
            case NONE:
            default:
                break;
        }
    }

    private void clearCurrentComposition(InputConnection ic) {
        if (ic == null) {
            return;
        }
        ic.setComposingText("", 1);
        ic.finishComposingText();
        expectSelectionUpdate();
    }

    private void expectSelectionUpdate() {
        mIgnoreSelectionUpdates = 2;
    }

    private void syncViewFromState() {
        if (mKeyboardView == null) {
            return;
        }
        mKeyboardView.setKeys(KeyboardLayout.getRows(mState));
        mKeyboardView.setShiftState(mState.getShiftState());
    }
}
