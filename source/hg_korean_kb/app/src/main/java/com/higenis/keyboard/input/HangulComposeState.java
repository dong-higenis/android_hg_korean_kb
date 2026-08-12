package com.higenis.keyboard.input;

/**
 * Explicit composition stages for {@link HangulComposer}.
 * Index fields alone are not enough for standalone vowels / consonant-only.
 */
public enum HangulComposeState {
    /** No active composition. */
    EMPTY,

    /** Single consonant (초성 후보) shown as jamo, e.g. "ㄱ". */
    INITIAL,

    /** 초성 + 중성 syllable without 종성, e.g. "가". */
    INITIAL_MEDIAL,

    /** Full syllable with 종성, e.g. "간". */
    INITIAL_MEDIAL_FINAL,

    /** Vowel typed with no leading consonant, e.g. "ㅏ". */
    STANDALONE_VOWEL
}
