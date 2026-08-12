package com.higenis.keyboard;

import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

/**
 * Holds the active editor session for the IME.
 * <p>
 * {@link EditorInfo} is cached for the current field. {@link InputConnection}
 * is never stored long-term — callers obtain a fresh connection through
 * {@link ConnectionProvider} because the framework may replace it.
 */
public final class InputSession {

    /**
     * Supplies the current {@link InputConnection} from the IME service.
     */
    public interface ConnectionProvider {
        InputConnection getCurrentInputConnection();
    }

    private ConnectionProvider mConnectionProvider;
    private EditorInfo mEditorInfo;
    private boolean mInputViewStarted;

    /**
     * Binds the provider that returns live InputConnections.
     * Called once from {@link HGKeyboardService#onCreate()}.
     */
    public void attach(ConnectionProvider provider) {
        mConnectionProvider = provider;
    }

    /**
     * Called from {@link HGKeyboardService#onStartInputView}.
     */
    public void onStartInputView(EditorInfo info, boolean restarting) {
        mEditorInfo = info;
        mInputViewStarted = true;
        if (DebugLog.DEBUG) {
            DebugLog.d("InputSession.onStartInputView restarting=" + restarting
                    + " " + formatEditorInfo(info));
        }
    }

    /**
     * Called from {@link HGKeyboardService#onFinishInputView}.
     */
    public void onFinishInputView(boolean finishingInput) {
        mInputViewStarted = false;
        if (DebugLog.DEBUG) {
            DebugLog.d("InputSession.onFinishInputView finishingInput=" + finishingInput);
        }
        if (finishingInput) {
            mEditorInfo = null;
        }
    }

    /**
     * Called from {@link HGKeyboardService#onFinishInput}.
     */
    public void onFinishInput() {
        mInputViewStarted = false;
        mEditorInfo = null;
        if (DebugLog.DEBUG) {
            DebugLog.d("InputSession.onFinishInput");
        }
    }

    public boolean isInputViewStarted() {
        return mInputViewStarted;
    }

    /**
     * @return current {@link EditorInfo}, or {@code null} if no active field
     */
    public EditorInfo getEditorInfo() {
        return mEditorInfo;
    }

    /**
     * @return live {@link InputConnection}, or {@code null} if unavailable
     */
    public InputConnection getInputConnection() {
        if (mConnectionProvider == null) {
            return null;
        }
        return mConnectionProvider.getCurrentInputConnection();
    }

    /**
     * Convenience: connection only when the input view session is active.
     */
    public InputConnection getActiveInputConnection() {
        if (!mInputViewStarted) {
            return null;
        }
        return getInputConnection();
    }

    static String formatEditorInfo(EditorInfo info) {
        if (info == null) {
            return "EditorInfo=null";
        }
        return "inputType=0x" + Integer.toHexString(info.inputType)
                + " imeOptions=0x" + Integer.toHexString(info.imeOptions)
                + " actionId=" + info.actionId
                + " packageName=" + info.packageName
                + " fieldId=" + info.fieldId;
    }
}
