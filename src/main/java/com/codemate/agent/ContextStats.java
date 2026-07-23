package com.codemate.agent;

/** A lightweight view of the conversation currently sent to the model. */
public record ContextStats(int messageCount, int characters, int maxCharacters, int trimmedTurns) {
    public int usagePercent() {
        return maxCharacters <= 0 ? 0 : Math.min(100, (int) Math.round(characters * 100.0 / maxCharacters));
    }
}
