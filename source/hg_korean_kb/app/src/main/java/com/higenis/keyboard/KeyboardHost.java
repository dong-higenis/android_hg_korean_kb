package com.higenis.keyboard;

/**
 * Bridge from {@link KeyboardController} to the owning {@link android.inputmethodservice.InputMethodService}
 * without casting or holding the service type.
 */
public interface KeyboardHost {

    /** Hide the soft input (typically {@code requestHideSelf(0)}). */
    void requestHideKeyboard();
}
