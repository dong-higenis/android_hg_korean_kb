package com.higenis.keyboard;

import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

/**
 * HG Keyboard IME entry point.
 * <p>
 * Owns the {@link InputMethodService} lifecycle and wires
 * {@link InputSession}, {@link PreferencesHelper}, {@link KeyboardView},
 * and {@link KeyboardController}. Key handling lives in the controller.
 */
public class HGKeyboardService extends InputMethodService {

    private InputSession mInputSession;
    private PreferencesHelper mPreferences;
    private KeyboardController mController;
    private KeyboardView mKeyboardView;

    @Override
    public void onCreate() {
        super.onCreate();
        mInputSession = new InputSession();
        mInputSession.attach(new InputSession.ConnectionProvider() {
            @Override
            public InputConnection getCurrentInputConnection() {
                return HGKeyboardService.this.getCurrentInputConnection();
            }
        });
        mPreferences = new PreferencesHelper(this);
        mController = new KeyboardController(
                mInputSession,
                mPreferences,
                new KeyboardHost() {
                    @Override
                    public void requestHideKeyboard() {
                        // Public API: dismiss this IME (no Navigation Bar on target devices).
                        requestHideSelf(0);
                    }
                });
        DebugLog.d("HGKeyboardService.onCreate language="
                + mController.getState().getLanguage());
    }

    /**
     * Never enter fullscreen extract mode (landscape / short displays).
     * Keeps the app's own EditText visible above the keyboard.
     */
    @Override
    public boolean onEvaluateFullscreenMode() {
        return false;
    }

    @Override
    public View onCreateInputView() {
        DebugLog.d("HGKeyboardService.onCreateInputView");
        View root = getLayoutInflater().inflate(R.layout.input_view, null);
        mKeyboardView = root.findViewById(R.id.keyboard_view);
        mController.attachKeyboardView(mKeyboardView);
        return root;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        mInputSession.onStartInputView(info, restarting);
        // InputType → LETTERS / NUMBER / PHONE; HangulComposer reset (no write to new IC).
        mController.onStartInputView(info, restarting);
        if (DebugLog.DEBUG) {
            DebugLog.d("onStartInputView restarting=" + restarting
                    + " " + InputSession.formatEditorInfo(info)
                    + " " + mController.getState());
        }
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        // Finish Hangul while still bound to the leaving editor.
        mController.onFinishInputView(finishingInput);
        mInputSession.onFinishInputView(finishingInput);
        super.onFinishInputView(finishingInput);
        DebugLog.d("onFinishInputView finishingInput=" + finishingInput);
    }

    @Override
    public void onFinishInput() {
        mController.onFinishInput();
        mInputSession.onFinishInput();
        super.onFinishInput();
        DebugLog.d("onFinishInput");
    }

    /**
     * Detect caret/selection leaving the Hangul composing region so composer
     * state cannot drift from the editor (API 29 public callback).
     */
    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
            int newSelStart, int newSelEnd,
            int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);
        mController.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);
    }

    public InputSession getInputSession() {
        return mInputSession;
    }

    public KeyboardController getController() {
        return mController;
    }

    public KeyboardView getKeyboardView() {
        return mKeyboardView;
    }
}
