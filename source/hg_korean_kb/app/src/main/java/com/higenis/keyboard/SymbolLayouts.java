package com.higenis.keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable SYMBOLS_1 / SYMBOLS_2 row tables shared by all languages.
 * Glyphs use {@code \\u} escapes for encoding-safe AOSP builds.
 */
public final class SymbolLayouts {

    private SymbolLayouts() {
    }

    public static List<List<KeyModel>> symbols1() {
        return SYMBOLS_1_ROWS;
    }

    public static List<List<KeyModel>> symbols2() {
        return SYMBOLS_2_ROWS;
    }

    private static final List<List<KeyModel>> SYMBOLS_1_ROWS = buildSymbols1();
    private static final List<List<KeyModel>> SYMBOLS_2_ROWS = buildSymbols2();

    private static List<List<KeyModel>> buildSymbols1() {
        List<List<KeyModel>> rows = new ArrayList<List<KeyModel>>(4);

        // 1 2 3 4 5 6 7 8 9 0
        rows.add(row(
                ch('1'), ch('2'), ch('3'), ch('4'), ch('5'),
                ch('6'), ch('7'), ch('8'), ch('9'), ch('0')
        ));

        // @ # ₩ _ & - + ( ) /
        rows.add(row(
                ch('@'), ch('#'), ch('\u20A9'), ch('_'), ch('&'),
                ch('-'), ch('+'), ch('('), ch(')'), ch('/')
        ));

        // #+=  * " ' : ; ! ?  BACKSPACE
        rows.add(row(
                KeyModel.action(KeyCodes.SWITCH_TO_SYMBOLS_2, "#+=", KeyWeights.PAGE),
                ch('*'), ch('\"'), ch('\''), ch(':'),
                ch(';'), ch('!'), ch('?'),
                KeyModel.action(KeyCodes.BACKSPACE, "\u232B", KeyWeights.BACKSPACE)
        ));

        rows.add(symbolsBottomRow());
        return Collections.unmodifiableList(rows);
    }

    private static List<List<KeyModel>> buildSymbols2() {
        List<List<KeyModel>> rows = new ArrayList<List<KeyModel>>(4);

        // Prefer common / font-safe glyphs; keep structure for page switching.
        // ~ ` | • √ π ÷ × § °
        rows.add(row(
                ch('~'), ch('`'), ch('|'), ch('\u2022'), ch('\u221A'),
                ch('\u03C0'), ch('\u00F7'), ch('\u00D7'), ch('\u00A7'), ch('\u00B0')
        ));

        // £ ¥ € ¢ ^ ° = { } \  — degree already on row1; use ^ ° = { } \
        // User asked: £ ¢ € ¥ ^ ° = { } \
        rows.add(row(
                ch('\u00A3'), ch('\u00A2'), ch('\u20AC'), ch('\u00A5'), ch('^'),
                ch('\u00B0'), ch('='), ch('{'), ch('}'), ch('\\')
        ));

        // 123  % © ® ™ ✓ [ ] < > BACKSPACE
        rows.add(row(
                KeyModel.action(KeyCodes.SWITCH_TO_SYMBOLS_1, "123", KeyWeights.PAGE),
                ch('%'), ch('\u00A9'), ch('\u00AE'), ch('\u2122'),
                ch('\u2713'), ch('['), ch(']'), ch('<'), ch('>'),
                KeyModel.action(KeyCodes.BACKSPACE, "\u232B", KeyWeights.BACKSPACE)
        ));

        rows.add(symbolsBottomRow());
        return Collections.unmodifiableList(rows);
    }

    /** ABC | 한/영 | , | Space | . | Enter | ▼ */
    private static List<KeyModel> symbolsBottomRow() {
        return row(
                KeyModel.action(KeyCodes.SWITCH_TO_LETTERS, "ABC", KeyWeights.PAGE),
                KeyModel.action(KeyCodes.LANGUAGE, "한/영", KeyWeights.LANGUAGE),
                KeyModel.character(',', ",", KeyWeights.PUNCT),
                KeyModel.space(),
                KeyModel.character('.', ".", KeyWeights.PUNCT),
                KeyModel.action(KeyCodes.ENTER, "\u21B5", KeyWeights.ENTER),
                KeyModel.hideKeyboard()
        );
    }

    private static KeyModel ch(char c) {
        return KeyModel.character(c, String.valueOf(c));
    }

    private static List<KeyModel> row(KeyModel... keys) {
        List<KeyModel> list = new ArrayList<KeyModel>(keys.length);
        for (KeyModel key : keys) {
            list.add(key);
        }
        return Collections.unmodifiableList(list);
    }
}
