package com.higenis.keyboard.input;

/**
 * Instruction for {@code KeyboardController} to update {@code InputConnection}.
 * <p>
 * InputConnection semantics (Android 10):
 * <ul>
 *   <li>{@code setComposingText(t, 1)} replaces the composing span with {@code t}.</li>
 *   <li>{@code commitText(t, 1)} while composing replaces that span with {@code t}
 *       and commits it — it does <em>not</em> insert {@code t} in addition to the
 *       old composing text. Then {@code setComposingText(next, 1)} starts a new span.</li>
 *   <li>{@code finishComposingText()} commits the current composing span in place
 *       without requiring a second copy via {@code commitText}.</li>
 *   <li>Clearing ({@link Action#CLEAR_COMPOSING}): {@code setComposingText("", 1)}
 *       then {@code finishComposingText()}. This clears only the composing span;
 *       it must not be implemented with {@code deleteSurroundingText}, which would
 *       risk deleting already-committed neighbors.</li>
 * </ul>
 */
public final class CompositionResult {

    public enum Action {
        NONE,
        UPDATE_COMPOSING,
        COMMIT_AND_COMPOSE,
        FINISH_COMPOSING,
        CLEAR_COMPOSING
    }

    private static final CompositionResult NONE_INSTANCE =
            new CompositionResult(Action.NONE, "", "");

    private final Action mAction;
    private final String mCommitText;
    private final String mComposingText;

    private CompositionResult(Action action, String commitText, String composingText) {
        mAction = action != null ? action : Action.NONE;
        mCommitText = commitText != null ? commitText : "";
        mComposingText = composingText != null ? composingText : "";
    }

    public Action getAction() {
        return mAction;
    }

    public String getCommitText() {
        return mCommitText;
    }

    public String getComposingText() {
        return mComposingText;
    }

    public static CompositionResult none() {
        return NONE_INSTANCE;
    }

    public static CompositionResult updateComposing(String composingText) {
        return new CompositionResult(Action.UPDATE_COMPOSING, "", composingText);
    }

    public static CompositionResult commitAndCompose(String commitText, String composingText) {
        return new CompositionResult(Action.COMMIT_AND_COMPOSE, commitText, composingText);
    }

    public static CompositionResult finishComposing() {
        return new CompositionResult(Action.FINISH_COMPOSING, "", "");
    }

    public static CompositionResult clearComposing() {
        return new CompositionResult(Action.CLEAR_COMPOSING, "", "");
    }

    @Override
    public String toString() {
        // Lengths only — never include typed text (password / PII safe).
        return "CompositionResult{" + mAction
                + " commitLen=" + mCommitText.length()
                + " composingLen=" + mComposingText.length() + "}";
    }
}

