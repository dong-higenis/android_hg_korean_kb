package com.higenis.keyboard;

/**
 * Shift / Caps Lock state for alphabetic input.
 * <p>
 * Transition rules live in {@link KeyboardController}, not in the view.
 */
public enum ShiftState {
    /** Lowercase letters. */
    OFF,

    /** Next letter only is uppercase, then returns to {@link #OFF}. */
    ONCE,

    /** Caps Lock — letters stay uppercase until Shift is pressed again. */
    LOCKED
}
