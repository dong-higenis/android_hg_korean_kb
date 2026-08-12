package com.higenis.keyboard.input;

/**
 * Result of splitting a compound jungseong for Backspace.
 * Example: ㅘ → first=ㅗ, second=ㅏ.
 */
public final class MedialSplit {

    private final int mFirstMedialIndex;
    private final int mSecondMedialIndex;

    public MedialSplit(int firstMedialIndex, int secondMedialIndex) {
        mFirstMedialIndex = firstMedialIndex;
        mSecondMedialIndex = secondMedialIndex;
    }

    public int getFirstMedialIndex() {
        return mFirstMedialIndex;
    }

    public int getSecondMedialIndex() {
        return mSecondMedialIndex;
    }
}
