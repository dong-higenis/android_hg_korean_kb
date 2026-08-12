package com.higenis.keyboard.input;

/**
 * Pure-Java HangulComposer tests with a small InputConnection simulator.
 * Jamo / expected syllables use {@code \\u} escapes for encoding safety.
 */
public final class HangulComposerTest {

    private static final char G = '\u3131';
    private static final char GG = '\u3132';
    private static final char N = '\u3134';
    private static final char D = '\u3137';
    private static final char DD = '\u3138';
    private static final char R = '\u3139';
    private static final char M = '\u3141';
    private static final char B = '\u3142';
    private static final char S = '\u3145';
    private static final char NG = '\u3147';
    private static final char J = '\u3148';
    private static final char JJ = '\u3149';
    private static final char H = '\u314E';
    private static final char T = '\u314C';
    private static final char P = '\u314D';

    private static final char A = '\u314F';
    private static final char AE = '\u3150';
    private static final char EO = '\u3153';
    private static final char E = '\u3154';
    private static final char YEO = '\u3155';
    private static final char O = '\u3157';
    private static final char U = '\u315C';
    private static final char EU = '\u3161';
    private static final char I = '\u3163';

    private HangulComposerTest() {
    }

    public static void runAll() {
        testBasicStillWorks();
        testCompoundMedials();
        testCompoundFinals();
        testMedialBackspace();
        testFinalBackspace();
        testGwaenBackspace();
        testJongseongMove();
        testRealWords();
        testNoSpellCorrection();
        testIntermediateResults();
        System.out.println("HangulComposerTest: all passed");
        // Related host checks (InputType masks) — no Android runtime required.
        com.higenis.keyboard.InputTypeHelperTest.runAll();
    }

    private static void testBasicStillWorks() {
        assertFull(seq(G, A), syl(G, A));
        assertFull(seq(G, A, N), syl(G, A, N));
        assertFull(seq(H, A, N, G, EU, R), syl(H, A, N) + syl(G, EU, R));
        assertFull(seq(GG, A), syl(GG, A));
        assertFull(seq(JJ, A), syl(JJ, A));
        assertFull(seq(A), String.valueOf(A));
        assertFull(seq(DD), String.valueOf(DD));
    }

    private static void testCompoundMedials() {
        assertEq(sylMedial(G, HangulTables.V_WA), fullOf(G, O, A));   // 과
        assertEq(sylMedial(G, HangulTables.V_WAE), fullOf(G, O, AE)); // 괘
        assertEq(sylMedial(G, HangulTables.V_OE), fullOf(G, O, I));   // 괴
        assertEq(sylMedial(G, HangulTables.V_WO), fullOf(G, U, EO));  // 궈
        assertEq(sylMedial(G, HangulTables.V_WE), fullOf(G, U, E));   // 궤
        assertEq(sylMedial(G, HangulTables.V_WI), fullOf(G, U, I));   // 귀
        assertEq(sylMedial(NG, HangulTables.V_UI), fullOf(NG, EU, I)); // 의

        assertFull(seq(G, A, EO), syl(G, A) + EO);
        assertFull(seq(G, O, A, I), sylMedial(G, HangulTables.V_WA) + I);
    }

    private static void testCompoundFinals() {
        assertFull(seq(G, A, B, S), sylFinal(G, A, HangulTables.T_BS)); // 값
        assertFull(seq(NG, A, N, J), sylFinal(NG, A, HangulTables.T_NJ)); // 앉
        assertFull(seq(NG, A, N, H), sylFinal(NG, A, HangulTables.T_NH)); // 않
        assertFull(seq(NG, I, R, G), sylFinal(NG, I, HangulTables.T_LG)); // 읽
        assertFull(seq(S, A, R, M), sylFinal(S, A, HangulTables.T_LM)); // 삶
        assertFull(seq(N, EO, R, B), sylFinal(N, EO, HangulTables.T_LB)); // 넓
        assertFull(seq(H, A, R, T), sylFinal(H, A, HangulTables.T_LT)); // 핥
        assertFull(seq(NG, EU, R, P), sylFinal(NG, EU, HangulTables.T_LP)); // 읊
        assertFull(seq(S, I, R, H), sylFinal(S, I, HangulTables.T_LH)); // 싫
        assertFull(seq(NG, EO, B, S), sylFinal(NG, EO, HangulTables.T_BS)); // 없

        assertFull(seq(G, A, N, G), syl(G, A, N) + G);
    }

    private static void testMedialBackspace() {
        HangulComposer c = new HangulComposer();
        TestInputBuffer buf = new TestInputBuffer();
        feed(c, buf, G, O, AE);
        assertEq(sylMedial(G, HangulTables.V_WAE), buf.getFullText()); // 괘
        buf.apply(c.backspace());
        assertEq(syl(G, O), buf.getFullText()); // 고
        buf.apply(c.backspace());
        assertEq(String.valueOf(G), buf.getFullText());
        buf.apply(c.backspace());
        assertEq("", buf.getFullText());
    }

    private static void testFinalBackspace() {
        HangulComposer c = new HangulComposer();
        TestInputBuffer buf = new TestInputBuffer();
        feed(c, buf, G, A, B, S);
        assertEq(sylFinal(G, A, HangulTables.T_BS), buf.getFullText());
        buf.apply(c.backspace());
        assertEq(syl(G, A, B), buf.getFullText());
        buf.apply(c.backspace());
        assertEq(syl(G, A), buf.getFullText());
        buf.apply(c.backspace());
        assertEq(String.valueOf(G), buf.getFullText());
        buf.apply(c.backspace());
        assertEq("", buf.getFullText());

        c = new HangulComposer();
        buf = new TestInputBuffer();
        feed(c, buf, NG, A, N, J);
        assertEq(sylFinal(NG, A, HangulTables.T_NJ), buf.getFullText());
        buf.apply(c.backspace());
        assertEq(syl(NG, A, N), buf.getFullText());
        buf.apply(c.backspace());
        assertEq(syl(NG, A), buf.getFullText());
        buf.apply(c.backspace());
        assertEq(String.valueOf(NG), buf.getFullText());
        buf.apply(c.backspace());
        assertEq("", buf.getFullText());

        c = new HangulComposer();
        buf = new TestInputBuffer();
        feed(c, buf, NG, I, R, G);
        assertEq(sylFinal(NG, I, HangulTables.T_LG), buf.getFullText());
        buf.apply(c.backspace());
        assertEq(syl(NG, I, R), buf.getFullText());
        buf.apply(c.backspace());
        assertEq(syl(NG, I), buf.getFullText());
        buf.apply(c.backspace());
        assertEq(String.valueOf(NG), buf.getFullText());
    }

    private static void testGwaenBackspace() {
        // 괜 = ㄱ ㅗ ㅐ ㄴ → 괘 → 고 → ㄱ → empty
        HangulComposer c = new HangulComposer();
        TestInputBuffer buf = new TestInputBuffer();
        feed(c, buf, G, O, AE, N);
        assertEq(sylFinal(G, HangulTables.V_WAE, HangulTables.T_N), buf.getFullText());
        buf.apply(c.backspace());
        assertEq(sylMedial(G, HangulTables.V_WAE), buf.getFullText());
        buf.apply(c.backspace());
        assertEq(syl(G, O), buf.getFullText());
        buf.apply(c.backspace());
        assertEq(String.valueOf(G), buf.getFullText());
        buf.apply(c.backspace());
        assertEq("", buf.getFullText());
    }

    private static void testJongseongMove() {
        assertFull(seq(G, A, N, A), syl(G, A) + syl(N, A));
        assertFull(seq(G, A, B, S, A), syl(G, A, B) + syl(S, A));
        assertFull(seq(NG, A, N, J, A), syl(NG, A, N) + syl(J, A));
        assertFull(seq(NG, EO, B, S, EO), syl(NG, EO, B) + syl(S, EO));
    }

    private static void testRealWords() {
        assertFull(seq(H, A, N, G, EU, R), syl(H, A, N) + syl(G, EU, R));
        assertFull(seq(NG, A, N, N, YEO, NG), syl(NG, A, N) + syl(N, YEO, NG));
        assertFull(seq(D, AE, H, A, N, M, I, N, G, U, G),
                syl(D, AE) + syl(H, A, N) + syl(M, I, N) + syl(G, U, G));
        assertFull(seq(M, EO, G, NG, EO), syl(M, EO, G) + syl(NG, EO));
        assertFull(seq(NG, EO, B, S, NG, EO), sylFinal(NG, EO, HangulTables.T_BS) + syl(NG, EO));
        assertFull(seq(NG, A, N, J, NG, A), sylFinal(NG, A, HangulTables.T_NJ) + syl(NG, A));
        assertFull(seq(NG, I, R, G, NG, EO), sylFinal(NG, I, HangulTables.T_LG) + syl(NG, EO));
    }

    private static void testNoSpellCorrection() {
        assertFull(seq(M, EO, G, EO), syl(M, EO) + syl(G, EO)); // 머거
    }

    private static void testIntermediateResults() {
        HangulComposer c = new HangulComposer();
        CompositionResult r;

        r = c.process(G);
        assertEq(CompositionResult.Action.UPDATE_COMPOSING, r.getAction());
        assertEq(String.valueOf(G), r.getComposingText());

        r = c.process(O);
        assertEq(CompositionResult.Action.UPDATE_COMPOSING, r.getAction());
        assertEq(syl(G, O), r.getComposingText());

        r = c.process(A);
        assertEq(CompositionResult.Action.UPDATE_COMPOSING, r.getAction());
        assertEq(sylMedial(G, HangulTables.V_WA), r.getComposingText());

        c.reset();
        c.process(G);
        c.process(A);
        c.process(B);
        r = c.process(S);
        assertEq(CompositionResult.Action.UPDATE_COMPOSING, r.getAction());
        assertEq(sylFinal(G, A, HangulTables.T_BS), r.getComposingText());

        r = c.process(A);
        assertEq(CompositionResult.Action.COMMIT_AND_COMPOSE, r.getAction());
        assertEq(syl(G, A, B), r.getCommitText());
        assertEq(syl(S, A), r.getComposingText());
    }

    // ------------------------------------------------------------------ helpers

    private static char[] seq(char... jamos) {
        return jamos;
    }

    private static String fullOf(char... jamos) {
        HangulComposer c = new HangulComposer();
        TestInputBuffer buf = new TestInputBuffer();
        feed(c, buf, jamos);
        buf.apply(c.flush());
        return buf.getFullText();
    }

    private static void assertFull(char[] jamos, String expected) {
        assertEq(expected, fullOf(jamos));
    }

    private static void feed(HangulComposer c, TestInputBuffer buf, char... jamos) {
        for (int i = 0; i < jamos.length; i++) {
            buf.apply(c.process(jamos[i]));
        }
    }

    private static String syl(char choseong, char jungseong) {
        return String.valueOf(HangulTables.composeSyllable(
                HangulTables.getInitialIndex(choseong),
                HangulTables.getMedialIndex(jungseong),
                HangulTables.FINAL_NONE));
    }

    private static String syl(char choseong, char jungseong, char jongseong) {
        return String.valueOf(HangulTables.composeSyllable(
                HangulTables.getInitialIndex(choseong),
                HangulTables.getMedialIndex(jungseong),
                HangulTables.getFinalIndex(jongseong)));
    }

    /** Choseong jamo + compound medial index (no final). */
    private static String sylMedial(char choseong, int medialIndex) {
        return String.valueOf(HangulTables.composeSyllable(
                HangulTables.getInitialIndex(choseong),
                medialIndex,
                HangulTables.FINAL_NONE));
    }

    private static String sylFinal(char choseong, char jungseong, int finalIndex) {
        return String.valueOf(HangulTables.composeSyllable(
                HangulTables.getInitialIndex(choseong),
                HangulTables.getMedialIndex(jungseong),
                finalIndex));
    }

    private static String sylFinal(char choseong, int medialIndex, int finalIndex) {
        return String.valueOf(HangulTables.composeSyllable(
                HangulTables.getInitialIndex(choseong),
                medialIndex,
                finalIndex));
    }

    private static void assertEq(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("expected=[" + expected + "] actual=[" + actual + "]"
                    + " expectedHex=" + toHex(String.valueOf(expected))
                    + " actualHex=" + toHex(String.valueOf(actual)));
        }
    }

    private static String toHex(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i > 0) {
                b.append(' ');
            }
            b.append(Integer.toHexString(s.charAt(i)));
        }
        return b.toString();
    }

    /**
     * Simulates Android InputConnection composing/commit semantics for tests.
     */
    static final class TestInputBuffer {
        private final StringBuilder mCommitted = new StringBuilder();
        private String mComposing = "";

        void apply(CompositionResult result) {
            if (result == null) {
                return;
            }
            switch (result.getAction()) {
                case NONE:
                    break;
                case UPDATE_COMPOSING:
                    mComposing = result.getComposingText();
                    break;
                case COMMIT_AND_COMPOSE:
                    mCommitted.append(result.getCommitText());
                    mComposing = result.getComposingText();
                    break;
                case FINISH_COMPOSING:
                    mCommitted.append(mComposing);
                    mComposing = "";
                    break;
                case CLEAR_COMPOSING:
                    mComposing = "";
                    break;
                default:
                    break;
            }
        }

        String getFullText() {
            return mCommitted.toString() + mComposing;
        }
    }
}
