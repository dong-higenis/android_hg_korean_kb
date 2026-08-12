package com.higenis.keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable NUMBER / PHONE keypads. Not the same as SYMBOLS_1 (?123).
 * <p>
 * Every row totals weight 3.0 so columns stay aligned with {@code 1 2 3}:
 * <ul>
 *   <li>digit rows: 1 + 1 + 1</li>
 *   <li>action row with Backspace: ⌫ + ↵ + ▼ (1+1+1)</li>
 *   <li>action row without Backspace: ↵ (2) + ▼ (1)</li>
 * </ul>
 */
public final class NumberLayouts {

    /** Matches two digit columns (under 1-2 / 4-5 / …). */
    private static final float ENTER_SPAN = 2.0f;

    private NumberLayouts() {
    }

    public static List<List<KeyModel>> getNumberLayout(boolean signed, boolean decimal) {
        if (signed && decimal) {
            return NUMBER_SIGNED_DECIMAL;
        }
        if (signed) {
            return NUMBER_SIGNED;
        }
        if (decimal) {
            return NUMBER_DECIMAL;
        }
        return NUMBER_INTEGER;
    }

    public static List<List<KeyModel>> getPhoneLayout() {
        return PHONE;
    }

    private static final List<List<KeyModel>> NUMBER_INTEGER = buildNumber(false, false);
    private static final List<List<KeyModel>> NUMBER_DECIMAL = buildNumber(false, true);
    private static final List<List<KeyModel>> NUMBER_SIGNED = buildNumber(true, false);
    private static final List<List<KeyModel>> NUMBER_SIGNED_DECIMAL = buildNumber(true, true);
    private static final List<List<KeyModel>> PHONE = buildPhone();

    private static List<List<KeyModel>> buildNumber(boolean signed, boolean decimal) {
        List<List<KeyModel>> rows = new ArrayList<List<KeyModel>>(5);

        rows.add(row(ch('1'), ch('2'), ch('3')));
        rows.add(row(ch('4'), ch('5'), ch('6')));
        rows.add(row(ch('7'), ch('8'), ch('9')));

        if (signed && decimal) {
            // - 0 .
            // ⌫ ↵ ▼
            rows.add(row(ch('-'), ch('0'), ch('.')));
            rows.add(row(backspace(), enter(), hide()));
        } else if (signed) {
            // - 0 ⌫
            // [  ↵  ] ▼
            rows.add(row(ch('-'), ch('0'), backspace()));
            rows.add(row(enterWide(), hide()));
        } else if (decimal) {
            // . 0 ⌫
            // [  ↵  ] ▼
            rows.add(row(ch('.'), ch('0'), backspace()));
            rows.add(row(enterWide(), hide()));
        } else {
            // (빈) 0 ⌫
            // [  ↵  ] ▼
            rows.add(row(spacer(), ch('0'), backspace()));
            rows.add(row(enterWide(), hide()));
        }

        return Collections.unmodifiableList(rows);
    }

    private static List<List<KeyModel>> buildPhone() {
        List<List<KeyModel>> rows = new ArrayList<List<KeyModel>>(5);

        rows.add(row(ch('1'), ch('2'), ch('3')));
        rows.add(row(ch('4'), ch('5'), ch('6')));
        rows.add(row(ch('7'), ch('8'), ch('9')));
        rows.add(row(ch('*'), ch('0'), ch('#')));
        rows.add(row(backspace(), enter(), hide()));

        return Collections.unmodifiableList(rows);
    }

    private static KeyModel backspace() {
        return KeyModel.action(KeyCodes.BACKSPACE, "\u232B", KeyWeights.NORMAL);
    }

    private static KeyModel enter() {
        return KeyModel.action(KeyCodes.ENTER, "\u21B5", KeyWeights.NORMAL);
    }

    /** Enter spanning two columns (weight 2) so ↵+▼ still totals 3. */
    private static KeyModel enterWide() {
        return KeyModel.action(KeyCodes.ENTER, "\u21B5", ENTER_SPAN);
    }

    private static KeyModel hide() {
        // Must use NORMAL (1.0), not KeyWeights.HIDE, or the 3-col grid drifts.
        return KeyModel.action(KeyCodes.HIDE_KEYBOARD, "\u25BC", KeyWeights.NORMAL);
    }

    private static KeyModel spacer() {
        return KeyModel.spacer(KeyWeights.NORMAL);
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
