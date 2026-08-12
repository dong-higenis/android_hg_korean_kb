package com.higenis.keyboard.input;

/**
 * Hangul 2-set composition state machine (pure Java).
 * <p>
 * Manages only the <em>current</em> syllable / jamo under composition.
 * Compound jungseong / jongseong use explicit tables in {@link HangulTables}.
 * Does not auto-insert ㅇ, spell-correct, or touch Android APIs.
 */
public final class HangulComposer {

    private HangulComposeState mState = HangulComposeState.EMPTY;

    private int mInitialIndex = HangulTables.INDEX_NONE;
    private int mMedialIndex = HangulTables.INDEX_NONE;
    private int mFinalIndex = HangulTables.FINAL_NONE;

    public CompositionResult process(int jamoCode) {
        if (HangulTables.isVowel(jamoCode)) {
            return processVowel(jamoCode);
        }
        if (HangulTables.getInitialIndex(jamoCode) >= 0
                || HangulTables.canBeFinalConsonant(jamoCode)) {
            return processConsonant(jamoCode);
        }
        return flush();
    }

    public CompositionResult backspace() {
        switch (mState) {
            case EMPTY:
                return CompositionResult.none();

            case STANDALONE_VOWEL: {
                MedialSplit medialSplit = HangulTables.splitMedial(mMedialIndex);
                if (medialSplit != null) {
                    mMedialIndex = medialSplit.getFirstMedialIndex();
                    return CompositionResult.updateComposing(getComposingText());
                }
                resetInternal();
                return CompositionResult.clearComposing();
            }

            case INITIAL:
                resetInternal();
                return CompositionResult.clearComposing();

            case INITIAL_MEDIAL: {
                // 과 → 고 (remove second medial), 고 → ㄱ
                MedialSplit medialSplit = HangulTables.splitMedial(mMedialIndex);
                if (medialSplit != null) {
                    mMedialIndex = medialSplit.getFirstMedialIndex();
                    return CompositionResult.updateComposing(getComposingText());
                }
                mMedialIndex = HangulTables.INDEX_NONE;
                mState = HangulComposeState.INITIAL;
                return CompositionResult.updateComposing(getComposingText());
            }

            case INITIAL_MEDIAL_FINAL: {
                // 값 → 갑 (strip second jongseong), 갑 → 가
                FinalSplit finalSplit = HangulTables.splitFinal(mFinalIndex);
                if (finalSplit != null && finalSplit.isCompound()) {
                    mFinalIndex = finalSplit.getRemainingFinalIndex();
                    return CompositionResult.updateComposing(getComposingText());
                }
                mFinalIndex = HangulTables.FINAL_NONE;
                mState = HangulComposeState.INITIAL_MEDIAL;
                return CompositionResult.updateComposing(getComposingText());
            }

            default:
                resetInternal();
                return CompositionResult.clearComposing();
        }
    }

    public CompositionResult flush() {
        if (!hasComposition()) {
            return CompositionResult.none();
        }
        resetInternal();
        return CompositionResult.finishComposing();
    }

    public boolean hasComposition() {
        return mState != HangulComposeState.EMPTY;
    }

    public String getComposingText() {
        switch (mState) {
            case EMPTY:
                return "";
            case INITIAL:
                return String.valueOf(HangulTables.initialAt(mInitialIndex));
            case STANDALONE_VOWEL:
                return String.valueOf(HangulTables.medialAt(mMedialIndex));
            case INITIAL_MEDIAL:
                return String.valueOf(HangulTables.composeSyllable(
                        mInitialIndex, mMedialIndex, HangulTables.FINAL_NONE));
            case INITIAL_MEDIAL_FINAL:
                return String.valueOf(HangulTables.composeSyllable(
                        mInitialIndex, mMedialIndex, mFinalIndex));
            default:
                return "";
        }
    }

    public void reset() {
        resetInternal();
    }

    public HangulComposeState getState() {
        return mState;
    }

    // -------------------------------------------------------------------------
    // Consonant
    // -------------------------------------------------------------------------

    private CompositionResult processConsonant(int jamoCode) {
        switch (mState) {
            case EMPTY:
                return startInitial(jamoCode);

            case STANDALONE_VOWEL: {
                String commit = getComposingText();
                resetInternal();
                CompositionResult started = startInitial(jamoCode);
                return CompositionResult.commitAndCompose(commit, started.getComposingText());
            }

            case INITIAL: {
                String commit = getComposingText();
                resetInternal();
                CompositionResult started = startInitial(jamoCode);
                return CompositionResult.commitAndCompose(commit, started.getComposingText());
            }

            case INITIAL_MEDIAL: {
                int finalIndex = HangulTables.getFinalIndex(jamoCode);
                if (finalIndex > HangulTables.FINAL_NONE) {
                    mFinalIndex = finalIndex;
                    mState = HangulComposeState.INITIAL_MEDIAL_FINAL;
                    return CompositionResult.updateComposing(getComposingText());
                }
                String commit = getComposingText();
                resetInternal();
                CompositionResult started = startInitial(jamoCode);
                return CompositionResult.commitAndCompose(commit, started.getComposingText());
            }

            case INITIAL_MEDIAL_FINAL: {
                int combined = HangulTables.combineFinal(mFinalIndex, jamoCode);
                if (combined > HangulTables.FINAL_NONE) {
                    mFinalIndex = combined;
                    return CompositionResult.updateComposing(getComposingText());
                }
                String commit = getComposingText();
                resetInternal();
                CompositionResult started = startInitial(jamoCode);
                return CompositionResult.commitAndCompose(commit, started.getComposingText());
            }

            default:
                return CompositionResult.none();
        }
    }

    // -------------------------------------------------------------------------
    // Vowel
    // -------------------------------------------------------------------------

    private CompositionResult processVowel(int jamoCode) {
        final int medialIndex = HangulTables.getMedialIndex(jamoCode);
        if (medialIndex < 0) {
            return CompositionResult.none();
        }

        switch (mState) {
            case EMPTY:
                mMedialIndex = medialIndex;
                mState = HangulComposeState.STANDALONE_VOWEL;
                return CompositionResult.updateComposing(getComposingText());

            case STANDALONE_VOWEL: {
                int combined = HangulTables.combineMedial(mMedialIndex, medialIndex);
                if (combined >= 0) {
                    mMedialIndex = combined;
                    return CompositionResult.updateComposing(getComposingText());
                }
                String commit = getComposingText();
                mMedialIndex = medialIndex;
                mState = HangulComposeState.STANDALONE_VOWEL;
                return CompositionResult.commitAndCompose(commit, getComposingText());
            }

            case INITIAL:
                mMedialIndex = medialIndex;
                mState = HangulComposeState.INITIAL_MEDIAL;
                return CompositionResult.updateComposing(getComposingText());

            case INITIAL_MEDIAL: {
                int combined = HangulTables.combineMedial(mMedialIndex, medialIndex);
                if (combined >= 0) {
                    mMedialIndex = combined;
                    return CompositionResult.updateComposing(getComposingText());
                }
                // 가 + ㅓ → commit 가, compose ㅓ (no auto ㅇ)
                String commit = getComposingText();
                resetInternal();
                mMedialIndex = medialIndex;
                mState = HangulComposeState.STANDALONE_VOWEL;
                return CompositionResult.commitAndCompose(commit, getComposingText());
            }

            case INITIAL_MEDIAL_FINAL: {
                // Single: 간+ㅏ → 가|나 ; Compound: 값+ㅏ → 갑|사
                FinalSplit split = HangulTables.splitFinal(mFinalIndex);
                if (split == null || split.getNextInitialIndex() < 0) {
                    String commit = getComposingText();
                    resetInternal();
                    mMedialIndex = medialIndex;
                    mState = HangulComposeState.STANDALONE_VOWEL;
                    return CompositionResult.commitAndCompose(commit, getComposingText());
                }

                String commit = String.valueOf(HangulTables.composeSyllable(
                        mInitialIndex, mMedialIndex, split.getRemainingFinalIndex()));

                mInitialIndex = split.getNextInitialIndex();
                mMedialIndex = medialIndex;
                mFinalIndex = HangulTables.FINAL_NONE;
                mState = HangulComposeState.INITIAL_MEDIAL;
                return CompositionResult.commitAndCompose(commit, getComposingText());
            }

            default:
                return CompositionResult.none();
        }
    }

    private CompositionResult startInitial(int jamoCode) {
        int initial = HangulTables.getInitialIndex(jamoCode);
        if (initial < 0) {
            return CompositionResult.none();
        }
        mInitialIndex = initial;
        mMedialIndex = HangulTables.INDEX_NONE;
        mFinalIndex = HangulTables.FINAL_NONE;
        mState = HangulComposeState.INITIAL;
        return CompositionResult.updateComposing(getComposingText());
    }

    private void resetInternal() {
        mState = HangulComposeState.EMPTY;
        mInitialIndex = HangulTables.INDEX_NONE;
        mMedialIndex = HangulTables.INDEX_NONE;
        mFinalIndex = HangulTables.FINAL_NONE;
    }
}
