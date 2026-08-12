package com.higenis.keyboard.input;

/**
 * Unicode Hangul syllable tables, index helpers, and explicit compound
 * medial/final combine·split tables (no index arithmetic guesses).
 * <p>
 * Pure Java — no Android dependencies.
 */
public final class HangulTables {

    public static final int HANGUL_BASE = 0xAC00;
    public static final int INITIAL_COUNT = 19;
    public static final int MEDIAL_COUNT = 21;
    public static final int FINAL_COUNT = 28;

    public static final int INDEX_NONE = -1;
    public static final int FINAL_NONE = 0;

    // Medial indices (Unicode order)
    public static final int V_A = 0;      // ㅏ
    public static final int V_AE = 1;     // ㅐ
    public static final int V_YA = 2;     // ㅑ
    public static final int V_YAE = 3;    // ㅒ
    public static final int V_EO = 4;     // ㅓ
    public static final int V_E = 5;      // ㅔ
    public static final int V_YEO = 6;    // ㅕ
    public static final int V_YE = 7;     // ㅖ
    public static final int V_O = 8;      // ㅗ
    public static final int V_WA = 9;     // ㅘ
    public static final int V_WAE = 10;   // ㅙ
    public static final int V_OE = 11;    // ㅚ
    public static final int V_YO = 12;    // ㅛ
    public static final int V_U = 13;     // ㅜ
    public static final int V_WO = 14;    // ㅝ
    public static final int V_WE = 15;    // ㅞ
    public static final int V_WI = 16;    // ㅟ
    public static final int V_YU = 17;    // ㅠ
    public static final int V_EU = 18;    // ㅡ
    public static final int V_UI = 19;    // ㅢ
    public static final int V_I = 20;     // ㅣ

    // Final indices used in compound tables
    public static final int T_G = 1;      // ㄱ
    public static final int T_GG = 2;     // ㄲ
    public static final int T_GS = 3;     // ㄳ
    public static final int T_N = 4;      // ㄴ
    public static final int T_NJ = 5;     // ㄵ
    public static final int T_NH = 6;     // ㄶ
    public static final int T_D = 7;      // ㄷ
    public static final int T_L = 8;      // ㄹ
    public static final int T_LG = 9;     // ㄺ
    public static final int T_LM = 10;    // ㄻ
    public static final int T_LB = 11;    // ㄼ
    public static final int T_LS = 12;    // ㄽ
    public static final int T_LT = 13;    // ㄾ
    public static final int T_LP = 14;    // ㄿ
    public static final int T_LH = 15;    // ㅀ
    public static final int T_M = 16;     // ㅁ
    public static final int T_B = 17;     // ㅂ
    public static final int T_BS = 18;    // ㅄ
    public static final int T_S = 19;     // ㅅ
    public static final int T_SS = 20;    // ㅆ
    public static final int T_NG = 21;    // ㅇ
    public static final int T_J = 22;     // ㅈ
    public static final int T_CH = 23;    // ㅊ
    public static final int T_K = 24;     // ㅋ
    public static final int T_T = 25;     // ㅌ
    public static final int T_P = 26;     // ㅍ
    public static final int T_H = 27;     // ㅎ

    // Compatibility Jamo: ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ
    private static final char[] INITIALS = {
            '\u3131', '\u3132', '\u3134', '\u3137', '\u3138',
            '\u3139', '\u3141', '\u3142', '\u3143', '\u3145',
            '\u3146', '\u3147', '\u3148', '\u3149', '\u314A',
            '\u314B', '\u314C', '\u314D', '\u314E'
    };

    // ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ
    private static final char[] MEDIALS = {
            '\u314F', '\u3150', '\u3151', '\u3152', '\u3153', '\u3154', '\u3155', '\u3156',
            '\u3157', '\u3158', '\u3159', '\u315A', '\u315B',
            '\u315C', '\u315D', '\u315E', '\u315F', '\u3160',
            '\u3161', '\u3162', '\u3163'
    };

    // Index 0 = none; then ㄱㄲㄳㄴㄵㄶㄷㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅄㅅㅆㅇㅈㅊㅋㅌㅍㅎ
    private static final char[] FINALS = {
            '\0',
            '\u3131', '\u3132', '\u3133', '\u3134', '\u3135', '\u3136', '\u3137',
            '\u3139', '\u313A', '\u313B', '\u313C', '\u313D', '\u313E', '\u313F', '\u3140',
            '\u3141', '\u3142', '\u3144', '\u3145', '\u3146', '\u3147', '\u3148',
            '\u314A', '\u314B', '\u314C', '\u314D', '\u314E'
    };

    /**
     * Compound medial: {first, second, combined}.
     * ㅗ+ㅏ→ㅘ, ㅗ+ㅐ→ㅙ, ㅗ+ㅣ→ㅚ, ㅜ+ㅓ→ㅝ, ㅜ+ㅔ→ㅞ, ㅜ+ㅣ→ㅟ, ㅡ+ㅣ→ㅢ
     */
    private static final int[][] MEDIAL_COMBINE = {
            {V_O, V_A, V_WA},
            {V_O, V_AE, V_WAE},
            {V_O, V_I, V_OE},
            {V_U, V_EO, V_WO},
            {V_U, V_E, V_WE},
            {V_U, V_I, V_WI},
            {V_EU, V_I, V_UI},
    };

    /**
     * Compound final: {firstFinal, secondJamoCompat, combinedFinal}.
     * second stored as Compatibility Jamo code point.
     */
    private static final int[][] FINAL_COMBINE = {
            {T_G, '\u3145', T_GS},  // ㄱ+ㅅ→ㄳ
            {T_N, '\u3148', T_NJ},  // ㄴ+ㅈ→ㄵ
            {T_N, '\u314E', T_NH},  // ㄴ+ㅎ→ㄶ
            {T_L, '\u3131', T_LG},  // ㄹ+ㄱ→ㄺ
            {T_L, '\u3141', T_LM},  // ㄹ+ㅁ→ㄻ
            {T_L, '\u3142', T_LB},  // ㄹ+ㅂ→ㄼ
            {T_L, '\u3145', T_LS},  // ㄹ+ㅅ→ㄽ
            {T_L, '\u314C', T_LT},  // ㄹ+ㅌ→ㄾ
            {T_L, '\u314D', T_LP},  // ㄹ+ㅍ→ㄿ
            {T_L, '\u314E', T_LH},  // ㄹ+ㅎ→ㅀ
            {T_B, '\u3145', T_BS},  // ㅂ+ㅅ→ㅄ
    };

    /**
     * Compound final split: {compound, remainingFinal, secondJamoCompat}.
     * nextInitial is derived from secondJamoCompat via {@link #getInitialIndex}.
     */
    private static final int[][] FINAL_SPLIT = {
            {T_GS, T_G, '\u3145'},
            {T_NJ, T_N, '\u3148'},
            {T_NH, T_N, '\u314E'},
            {T_LG, T_L, '\u3131'},
            {T_LM, T_L, '\u3141'},
            {T_LB, T_L, '\u3142'},
            {T_LS, T_L, '\u3145'},
            {T_LT, T_L, '\u314C'},
            {T_LP, T_L, '\u314D'},
            {T_LH, T_L, '\u314E'},
            {T_BS, T_B, '\u3145'},
    };

    private HangulTables() {
    }

    public static boolean isConsonant(int code) {
        return getInitialIndex(code) >= 0 || getFinalIndex(code) > 0
                || (code >= 0x3131 && code <= 0x314E);
    }

    public static boolean isVowel(int code) {
        return getMedialIndex(code) >= 0;
    }

    public static int getInitialIndex(int code) {
        for (int i = 0; i < INITIALS.length; i++) {
            if (INITIALS[i] == code) {
                return i;
            }
        }
        return INDEX_NONE;
    }

    public static int getMedialIndex(int code) {
        for (int i = 0; i < MEDIALS.length; i++) {
            if (MEDIALS[i] == code) {
                return i;
            }
        }
        return INDEX_NONE;
    }

    public static int getFinalIndex(int code) {
        for (int i = 1; i < FINALS.length; i++) {
            if (FINALS[i] == code) {
                return i;
            }
        }
        return INDEX_NONE;
    }

    public static boolean canBeFinalConsonant(int code) {
        return getFinalIndex(code) > FINAL_NONE;
    }

    public static char initialAt(int index) {
        return INITIALS[index];
    }

    public static char medialAt(int index) {
        return MEDIALS[index];
    }

    public static char finalAt(int index) {
        return FINALS[index];
    }

    public static char composeSyllable(int initialIndex, int medialIndex, int finalIndex) {
        if (initialIndex < 0 || initialIndex >= INITIAL_COUNT
                || medialIndex < 0 || medialIndex >= MEDIAL_COUNT
                || finalIndex < 0 || finalIndex >= FINAL_COUNT) {
            throw new IllegalArgumentException(
                    "Invalid Hangul indices L=" + initialIndex
                            + " V=" + medialIndex + " T=" + finalIndex);
        }
        int syllable = HANGUL_BASE
                + (initialIndex * MEDIAL_COUNT + medialIndex) * FINAL_COUNT
                + finalIndex;
        return (char) syllable;
    }

    /**
     * Combine two jungseong indices into a compound medial.
     *
     * @return combined medial index, or {@link #INDEX_NONE}
     */
    public static int combineMedial(int firstMedialIndex, int secondMedialIndex) {
        for (int i = 0; i < MEDIAL_COMBINE.length; i++) {
            if (MEDIAL_COMBINE[i][0] == firstMedialIndex
                    && MEDIAL_COMBINE[i][1] == secondMedialIndex) {
                return MEDIAL_COMBINE[i][2];
            }
        }
        return INDEX_NONE;
    }

    /**
     * Split a compound jungseong for Backspace (explicit table, not index--).
     *
     * @return split pair, or {@code null} if not a compound medial
     */
    public static MedialSplit splitMedial(int medialIndex) {
        for (int i = 0; i < MEDIAL_COMBINE.length; i++) {
            if (MEDIAL_COMBINE[i][2] == medialIndex) {
                return new MedialSplit(MEDIAL_COMBINE[i][0], MEDIAL_COMBINE[i][1]);
            }
        }
        return null;
    }

    /**
     * Combine a current final with a newly typed consonant jamo.
     *
     * @param firstFinalIndex current jongseong index
     * @param secondJamoCode  Compatibility Jamo code of the new consonant
     * @return combined final index, or {@link #INDEX_NONE}
     */
    public static int combineFinal(int firstFinalIndex, int secondJamoCode) {
        for (int i = 0; i < FINAL_COMBINE.length; i++) {
            if (FINAL_COMBINE[i][0] == firstFinalIndex
                    && FINAL_COMBINE[i][1] == secondJamoCode) {
                return FINAL_COMBINE[i][2];
            }
        }
        return INDEX_NONE;
    }

    /**
     * Split a jongseong for vowel-follow / Backspace.
     * <ul>
     *   <li>Compound: remaining = first half as final, nextInitial = second as choseong</li>
     *   <li>Simple: remaining = {@link #FINAL_NONE}, nextInitial = that jamo as choseong</li>
     * </ul>
     *
     * @return split info, or {@code null} if the final cannot become a choseong
     */
    public static FinalSplit splitFinal(int finalIndex) {
        if (finalIndex <= FINAL_NONE) {
            return null;
        }

        for (int i = 0; i < FINAL_SPLIT.length; i++) {
            if (FINAL_SPLIT[i][0] == finalIndex) {
                int nextInitial = getInitialIndex(FINAL_SPLIT[i][2]);
                if (nextInitial < 0) {
                    return null;
                }
                return new FinalSplit(FINAL_SPLIT[i][1], nextInitial, true);
            }
        }

        // Simple final: entire jongseong moves to next choseong.
        int asInitial = getInitialIndex(finalAt(finalIndex));
        if (asInitial < 0) {
            return null;
        }
        return new FinalSplit(FINAL_NONE, asInitial, false);
    }
}
