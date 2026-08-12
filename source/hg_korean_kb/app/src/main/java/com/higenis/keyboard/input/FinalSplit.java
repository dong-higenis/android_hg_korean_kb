package com.higenis.keyboard.input;

/**
 * Result of splitting a jongseong when a vowel follows, or for Backspace.
 * <p>
 * Compound example (ㅄ): remainingFinal=ㅂ, nextInitial=ㅅ, compound=true.
 * Simple example (ㄴ): remainingFinal=0 (none), nextInitial=ㄴ, compound=false.
 */
public final class FinalSplit {

    private final int mRemainingFinalIndex;
    private final int mNextInitialIndex;
    private final boolean mCompound;

    public FinalSplit(int remainingFinalIndex, int nextInitialIndex, boolean compound) {
        mRemainingFinalIndex = remainingFinalIndex;
        mNextInitialIndex = nextInitialIndex;
        mCompound = compound;
    }

    /** Final left on the previous syllable (0 if none). */
    public int getRemainingFinalIndex() {
        return mRemainingFinalIndex;
    }

    /** Choseong index for the new syllable. */
    public int getNextInitialIndex() {
        return mNextInitialIndex;
    }

    public boolean isCompound() {
        return mCompound;
    }
}
