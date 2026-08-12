package com.higenis.keyboard;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Custom keyboard view that draws keys on a {@link Canvas}.
 * <p>
 * Does not own language or layout data generation. The controller pushes
 * immutable row lists via {@link #setKeys(List)} and shift via
 * {@link #setShiftState(ShiftState)}.
 */
public class KeyboardView extends View {

    public interface OnKeyboardActionListener {
        void onKeyPressed(KeyModel key);
    }

    private final Paint mKeyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mActionKeyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mPressedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mShiftActivePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint.FontMetrics mFontMetrics = new Paint.FontMetrics();
    private final RectF mTempRect = new RectF();

    private final List<PositionedKey> mPositionedKeys = new ArrayList<PositionedKey>();

    /** Current row model list (usually a static immutable table from KeyboardLayout). */
    private List<List<KeyModel>> mRows = Collections.emptyList();

    private ShiftState mShiftState = ShiftState.OFF;
    private OnKeyboardActionListener mListener;

    private PositionedKey mPressedAtKey;
    private PositionedKey mDownKey;

    private float mKeyCornerRadius;
    private float mKeyHorizontalGap;
    private float mKeyVerticalGap;
    private float mPaddingLeft;
    private float mPaddingTop;
    private float mPaddingRight;
    private float mPaddingBottom;
    private float mLabelTextSize;
    private float mActionLabelTextSize;
    private float mIconActionLabelTextSize;

    private int mColorKeyboardBg;
    private int mColorKey;
    private int mColorActionKey;
    private int mColorPressed;
    private int mColorShiftActive;
    private int mColorLabel;
    private int mColorActionLabel;

    private boolean mNeedsLayout = true;
    private int mLastWidth;
    private int mLastHeight;

    public KeyboardView(Context context) {
        super(context);
        init(context);
    }

    public KeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public KeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        Resources res = context.getResources();

        mKeyCornerRadius = res.getDimension(R.dimen.key_corner_radius);
        mKeyHorizontalGap = res.getDimension(R.dimen.key_horizontal_gap);
        mKeyVerticalGap = res.getDimension(R.dimen.key_vertical_gap);
        mPaddingLeft = res.getDimension(R.dimen.keyboard_padding_horizontal);
        mPaddingRight = mPaddingLeft;
        mPaddingTop = res.getDimension(R.dimen.keyboard_padding_vertical);
        mPaddingBottom = mPaddingTop;
        mLabelTextSize = res.getDimension(R.dimen.key_label_text_size);
        mActionLabelTextSize = res.getDimension(R.dimen.key_action_label_text_size);
        mIconActionLabelTextSize = res.getDimension(R.dimen.key_icon_action_label_text_size);

        mColorKeyboardBg = context.getColor(R.color.keyboard_background);
        mColorKey = context.getColor(R.color.key_background);
        mColorActionKey = context.getColor(R.color.key_action_background);
        mColorPressed = context.getColor(R.color.key_pressed_background);
        mColorShiftActive = context.getColor(R.color.key_shift_active_background);
        mColorLabel = context.getColor(R.color.key_label);
        mColorActionLabel = context.getColor(R.color.key_action_label);

        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(mColorKeyboardBg);

        mKeyPaint.setStyle(Paint.Style.FILL);
        mKeyPaint.setColor(mColorKey);

        mActionKeyPaint.setStyle(Paint.Style.FILL);
        mActionKeyPaint.setColor(mColorActionKey);

        mPressedPaint.setStyle(Paint.Style.FILL);
        mPressedPaint.setColor(mColorPressed);

        mShiftActivePaint.setStyle(Paint.Style.FILL);
        mShiftActivePaint.setColor(mColorShiftActive);

        mLabelPaint.setTextAlign(Paint.Align.CENTER);
        mLabelPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        mLabelPaint.setColor(mColorLabel);
        mLabelPaint.setTextSize(mLabelTextSize);

        setClickable(true);
        setFocusable(false);
    }

    public void setOnKeyboardActionListener(OnKeyboardActionListener listener) {
        mListener = listener;
    }

    /**
     * Installs key rows from the controller. When {@code rows} is the same
     * instance as before (static English/Korean tables), geometry is reused.
     */
    public void setKeys(List<List<KeyModel>> rows) {
        if (rows == null) {
            rows = Collections.emptyList();
        }
        if (mRows == rows && !mPositionedKeys.isEmpty()) {
            return;
        }
        mRows = rows;
        mPressedAtKey = null;
        mDownKey = null;
        rebuildPositionedKeys();
        mNeedsLayout = true;
        if (mLastWidth > 0 && mLastHeight > 0) {
            computeKeyPositions(mLastWidth, mLastHeight);
            mNeedsLayout = false;
        }
        invalidate();
    }

    /**
     * Mirrors controller Shift state for labels / Shift-key styling.
     * Does not rebuild key geometry.
     */
    public void setShiftState(ShiftState shiftState) {
        if (shiftState == null) {
            shiftState = ShiftState.OFF;
        }
        if (mShiftState == shiftState) {
            return;
        }
        mShiftState = shiftState;
        invalidate();
    }

    public ShiftState getShiftState() {
        return mShiftState;
    }

    private void rebuildPositionedKeys() {
        mPositionedKeys.clear();
        for (int r = 0; r < mRows.size(); r++) {
            List<KeyModel> row = mRows.get(r);
            for (int c = 0; c < row.size(); c++) {
                mPositionedKeys.add(new PositionedKey(row.get(c)));
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mLastWidth = w;
        mLastHeight = h;
        if (!mRows.isEmpty() && mPositionedKeys.isEmpty()) {
            rebuildPositionedKeys();
        }
        computeKeyPositions(w, h);
        mNeedsLayout = false;
    }

    private void computeKeyPositions(int viewWidth, int viewHeight) {
        if (viewWidth <= 0 || viewHeight <= 0 || mRows.isEmpty()) {
            return;
        }
        if (mPositionedKeys.isEmpty()) {
            rebuildPositionedKeys();
        }

        final int rowCount = mRows.size();
        final float contentWidth = viewWidth - mPaddingLeft - mPaddingRight;
        final float contentHeight = viewHeight - mPaddingTop - mPaddingBottom;
        final float totalVerticalGaps = mKeyVerticalGap * (rowCount - 1);
        final float rowHeight = (contentHeight - totalVerticalGaps) / rowCount;

        int keyIndex = 0;
        float top = mPaddingTop;

        for (int r = 0; r < rowCount; r++) {
            List<KeyModel> row = mRows.get(r);
            final int keyCount = row.size();

            float totalWeight = 0f;
            for (int i = 0; i < keyCount; i++) {
                totalWeight += row.get(i).getWidthWeight();
            }
            if (totalWeight <= 0f) {
                totalWeight = 1f;
            }

            final float totalHorizontalGaps = keyCount > 1
                    ? mKeyHorizontalGap * (keyCount - 1)
                    : 0f;
            final float availableWidth = contentWidth - totalHorizontalGaps;
            final float unitWidth = availableWidth / totalWeight;

            float left = mPaddingLeft;
            final float bottom = top + rowHeight;

            for (int i = 0; i < keyCount; i++) {
                PositionedKey positioned = mPositionedKeys.get(keyIndex++);
                float keyWidth = unitWidth * positioned.getModel().getWidthWeight();
                float right = left + keyWidth;
                if (i == keyCount - 1) {
                    right = viewWidth - mPaddingRight;
                }
                positioned.setBounds(left, top, right, bottom);
                left = right + mKeyHorizontalGap;
            }

            top = bottom + mKeyVerticalGap;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mNeedsLayout && getWidth() > 0 && getHeight() > 0) {
            computeKeyPositions(getWidth(), getHeight());
            mNeedsLayout = false;
        }

        canvas.drawRect(0, 0, getWidth(), getHeight(), mBackgroundPaint);

        final int count = mPositionedKeys.size();
        for (int i = 0; i < count; i++) {
            PositionedKey key = mPositionedKeys.get(i);
            if (key.getModel().getType() == KeyType.SPACER) {
                continue;
            }
            drawKey(canvas, key, key == mPressedAtKey);
        }
    }

    private void drawKey(Canvas canvas, PositionedKey key, boolean pressed) {
        final RectF bounds = key.getBounds();
        final KeyModel model = key.getModel();
        final boolean shiftKey = model.getCode() == KeyCodes.SHIFT;
        final boolean shiftLatched = shiftKey
                && (mShiftState == ShiftState.ONCE || mShiftState == ShiftState.LOCKED);

        Paint fillPaint;
        if (pressed) {
            fillPaint = mPressedPaint;
        } else if (shiftLatched) {
            fillPaint = mShiftActivePaint;
        } else if (model.getType() == KeyType.ACTION || model.getType() == KeyType.SPACE) {
            fillPaint = mActionKeyPaint;
        } else {
            fillPaint = mKeyPaint;
        }

        mTempRect.set(bounds);
        canvas.drawRoundRect(mTempRect, mKeyCornerRadius, mKeyCornerRadius, fillPaint);

        if (model.getType() == KeyType.SPACE) {
            return;
        }

        final String text = resolveDisplayLabel(model);
        drawLabel(canvas, text, bounds, model);
    }

    private String resolveDisplayLabel(KeyModel model) {
        if (model.getCode() == KeyCodes.SHIFT) {
            return "\u21E7";
        }
        return model.resolveLabel(mShiftState);
    }

    private void drawLabel(Canvas canvas, String label, RectF bounds, KeyModel model) {
        if (label == null || label.length() == 0) {
            return;
        }
        final boolean actionStyle = model.getType() == KeyType.ACTION;
        final float textSize;
        if (isGlyphActionKey(model.getCode())) {
            textSize = mIconActionLabelTextSize;
        } else if (actionStyle) {
            textSize = mActionLabelTextSize;
        } else {
            textSize = mLabelTextSize;
        }
        mLabelPaint.setTextSize(textSize);
        mLabelPaint.setColor(actionStyle ? mColorActionLabel : mColorLabel);

        mLabelPaint.getFontMetrics(mFontMetrics);
        float centerX = bounds.centerX();
        float centerY = bounds.centerY()
                - (mFontMetrics.ascent + mFontMetrics.descent) / 2f;
        canvas.drawText(label, centerX, centerY, mLabelPaint);
    }

    /** Enter / Backspace / Shift / Hide use symbol glyphs that need a larger paint size. */
    private static boolean isGlyphActionKey(int code) {
        return code == KeyCodes.ENTER
                || code == KeyCodes.BACKSPACE
                || code == KeyCodes.SHIFT
                || code == KeyCodes.HIDE_KEYBOARD;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        final float x = event.getX();
        final float y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                PositionedKey hit = findKeyAt(x, y);
                mDownKey = hit;
                mPressedAtKey = hit;
                invalidate();
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                if (mDownKey != null) {
                    boolean inside = mDownKey.contains(x, y);
                    PositionedKey nextPressed = inside ? mDownKey : null;
                    if (nextPressed != mPressedAtKey) {
                        mPressedAtKey = nextPressed;
                        invalidate();
                    }
                }
                return true;
            }
            case MotionEvent.ACTION_UP: {
                PositionedKey hit = findKeyAt(x, y);
                if (mDownKey != null && hit == mDownKey) {
                    fireKeyPressed(mDownKey.getModel());
                }
                mDownKey = null;
                mPressedAtKey = null;
                invalidate();
                return true;
            }
            case MotionEvent.ACTION_CANCEL: {
                mDownKey = null;
                mPressedAtKey = null;
                invalidate();
                return true;
            }
            default:
                return super.onTouchEvent(event);
        }
    }

    private void fireKeyPressed(KeyModel key) {
        if (key == null || !key.isTouchable()) {
            return;
        }
        if (mListener != null) {
            mListener.onKeyPressed(key);
        }
    }

    private PositionedKey findKeyAt(float x, float y) {
        for (int i = 0; i < mPositionedKeys.size(); i++) {
            PositionedKey key = mPositionedKeys.get(i);
            if (key.contains(x, y)) {
                return key;
            }
        }
        return null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int desiredHeight = (int) getResources().getDimension(R.dimen.keyboard_height);
        int minH = (int) getResources().getDimension(R.dimen.keyboard_min_height);
        int maxH = (int) getResources().getDimension(R.dimen.keyboard_max_height);
        desiredHeight = Math.max(minH, Math.min(desiredHeight, maxH));

        int height;
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }
}
