package com.higenis.keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides immutable keyboard row data for each language / page.
 * <p>
 * Prefer {@link #getRows(KeyboardState)} so NUMBER signed/decimal flags apply.
 */
public final class KeyboardLayout {

    private KeyboardLayout() {
        // no instance
    }

    public static List<List<KeyModel>> getRows(KeyboardState state) {
        if (state == null) {
            return getRows(LanguageMode.KOREAN, LayoutPage.LETTERS);
        }

        switch (state.getLayoutPage()) {
            case NUMBER:
                return NumberLayouts.getNumberLayout(
                        state.isNumberSigned(), state.isNumberDecimal());
            case PHONE:
                return NumberLayouts.getPhoneLayout();
            case SYMBOLS_1:
                return SymbolLayouts.symbols1();
            case SYMBOLS_2:
                return SymbolLayouts.symbols2();
            case LETTERS:
            default:
                return getLetterRows(state.getLanguage());
        }
    }

    public static List<List<KeyModel>> getRows(LanguageMode language, LayoutPage page) {
        if (page == null) {
            page = LayoutPage.LETTERS;
        }
        if (language == null) {
            language = LanguageMode.KOREAN;
        }

        switch (page) {
            case LETTERS:
                return getLetterRows(language);
            case SYMBOLS_1:
                return SymbolLayouts.symbols1();
            case SYMBOLS_2:
                return SymbolLayouts.symbols2();
            case NUMBER:
                return NumberLayouts.getNumberLayout(false, false);
            case PHONE:
                return NumberLayouts.getPhoneLayout();
            default:
                return getLetterRows(language);
        }
    }

    private static List<List<KeyModel>> getLetterRows(LanguageMode language) {
        if (language == LanguageMode.ENGLISH) {
            return ENGLISH_QWERTY_ROWS;
        }
        return KOREAN_2BUL_ROWS;
    }

    private static final List<List<KeyModel>> ENGLISH_QWERTY_ROWS = buildEnglishQwerty();
    private static final List<List<KeyModel>> KOREAN_2BUL_ROWS = buildKorean2Bul();

    private static List<List<KeyModel>> buildEnglishQwerty() {
        List<List<KeyModel>> rows = new ArrayList<List<KeyModel>>(5);

        // Number row shared with Korean LETTERS (always visible).
        rows.add(numberRow());

        rows.add(row(
                KeyModel.latinLetter('q'),
                KeyModel.latinLetter('w'),
                KeyModel.latinLetter('e'),
                KeyModel.latinLetter('r'),
                KeyModel.latinLetter('t'),
                KeyModel.latinLetter('y'),
                KeyModel.latinLetter('u'),
                KeyModel.latinLetter('i'),
                KeyModel.latinLetter('o'),
                KeyModel.latinLetter('p')
        ));

        rows.add(row(
                KeyModel.spacer(KeyWeights.ROW_INDENT),
                KeyModel.latinLetter('a'),
                KeyModel.latinLetter('s'),
                KeyModel.latinLetter('d'),
                KeyModel.latinLetter('f'),
                KeyModel.latinLetter('g'),
                KeyModel.latinLetter('h'),
                KeyModel.latinLetter('j'),
                KeyModel.latinLetter('k'),
                KeyModel.latinLetter('l'),
                KeyModel.spacer(KeyWeights.ROW_INDENT)
        ));

        rows.add(row(
                KeyModel.action(KeyCodes.SHIFT, "\u21E7", KeyWeights.SHIFT),
                KeyModel.latinLetter('z'),
                KeyModel.latinLetter('x'),
                KeyModel.latinLetter('c'),
                KeyModel.latinLetter('v'),
                KeyModel.latinLetter('b'),
                KeyModel.latinLetter('n'),
                KeyModel.latinLetter('m'),
                KeyModel.action(KeyCodes.BACKSPACE, "\u232B", KeyWeights.BACKSPACE)
        ));

        rows.add(lettersBottomRow());
        return Collections.unmodifiableList(rows);
    }

    private static List<List<KeyModel>> buildKorean2Bul() {
        List<List<KeyModel>> rows = new ArrayList<List<KeyModel>>(5);

        // Number row shared with English LETTERS (always visible).
        rows.add(numberRow());

        rows.add(row(
                KeyModel.hangul('ㅂ', 'ㅃ'),
                KeyModel.hangul('ㅈ', 'ㅉ'),
                KeyModel.hangul('ㄷ', 'ㄸ'),
                KeyModel.hangul('ㄱ', 'ㄲ'),
                KeyModel.hangul('ㅅ', 'ㅆ'),
                KeyModel.hangul('ㅛ'),
                KeyModel.hangul('ㅕ'),
                KeyModel.hangul('ㅑ'),
                KeyModel.hangul('ㅐ', 'ㅒ'),
                KeyModel.hangul('ㅔ', 'ㅖ')
        ));

        rows.add(row(
                KeyModel.spacer(KeyWeights.ROW_INDENT),
                KeyModel.hangul('ㅁ'),
                KeyModel.hangul('ㄴ'),
                KeyModel.hangul('ㅇ'),
                KeyModel.hangul('ㄹ'),
                KeyModel.hangul('ㅎ'),
                KeyModel.hangul('ㅗ'),
                KeyModel.hangul('ㅓ'),
                KeyModel.hangul('ㅏ'),
                KeyModel.hangul('ㅣ'),
                KeyModel.spacer(KeyWeights.ROW_INDENT)
        ));

        rows.add(row(
                KeyModel.action(KeyCodes.SHIFT, "\u21E7", KeyWeights.SHIFT),
                KeyModel.hangul('ㅋ'),
                KeyModel.hangul('ㅌ'),
                KeyModel.hangul('ㅊ'),
                KeyModel.hangul('ㅍ'),
                KeyModel.hangul('ㅠ'),
                KeyModel.hangul('ㅜ'),
                KeyModel.hangul('ㅡ'),
                KeyModel.action(KeyCodes.BACKSPACE, "\u232B", KeyWeights.BACKSPACE)
        ));

        rows.add(lettersBottomRow());
        return Collections.unmodifiableList(rows);
    }

    /** Top row on Korean / English LETTERS: 1 2 3 4 5 6 7 8 9 0 */
    private static List<KeyModel> numberRow() {
        return row(
                KeyModel.character('1', "1"),
                KeyModel.character('2', "2"),
                KeyModel.character('3', "3"),
                KeyModel.character('4', "4"),
                KeyModel.character('5', "5"),
                KeyModel.character('6', "6"),
                KeyModel.character('7', "7"),
                KeyModel.character('8', "8"),
                KeyModel.character('9', "9"),
                KeyModel.character('0', "0")
        );
    }

    /** ?123 | 한/영 | , | Space | . | Enter | ▼ */
    private static List<KeyModel> lettersBottomRow() {
        return row(
                KeyModel.action(KeyCodes.SWITCH_TO_SYMBOLS, "?123", KeyWeights.SYMBOLS),
                KeyModel.action(KeyCodes.LANGUAGE, "한/영", KeyWeights.LANGUAGE),
                KeyModel.character(',', ",", KeyWeights.PUNCT),
                KeyModel.space(),
                KeyModel.character('.', ".", KeyWeights.PUNCT),
                KeyModel.action(KeyCodes.ENTER, "\u21B5", KeyWeights.ENTER),
                KeyModel.hideKeyboard()
        );
    }

    private static List<KeyModel> row(KeyModel... keys) {
        List<KeyModel> list = new ArrayList<KeyModel>(keys.length);
        for (KeyModel key : keys) {
            list.add(key);
        }
        return Collections.unmodifiableList(list);
    }
}
