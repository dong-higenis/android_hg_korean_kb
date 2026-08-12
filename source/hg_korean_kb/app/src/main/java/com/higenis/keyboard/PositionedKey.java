package com.higenis.keyboard;

import android.graphics.RectF;

/**
 * A {@link KeyModel} placed into keyboard coordinates for hit-testing and draw.
 * <p>
 * {@link #bounds} is owned by this instance and mutated only during layout
 * passes inside {@link KeyboardView} — never allocate a new RectF per frame.
 */
public final class PositionedKey {

    private final KeyModel model;
    private final RectF bounds = new RectF();

    public PositionedKey(KeyModel model) {
        this.model = model;
    }

    public KeyModel getModel() {
        return model;
    }

    public RectF getBounds() {
        return bounds;
    }

    public void setBounds(float left, float top, float right, float bottom) {
        bounds.set(left, top, right, bottom);
    }

    public boolean contains(float x, float y) {
        return model.isTouchable() && bounds.contains(x, y);
    }
}
