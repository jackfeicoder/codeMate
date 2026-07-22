package com.codemate.llm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LlmMessageTest {
    @Test
    void createsCommonRoles() {
        assertEquals("system", LlmMessage.system("rules").role());
        assertEquals("user", LlmMessage.user("hello").role());
        assertEquals("assistant", LlmMessage.assistant("hi").role());
        assertEquals("tool", LlmMessage.tool("done").role());
    }

    @Test
    void rejectsBlankRole() {
        assertThrows(IllegalArgumentException.class, () -> new LlmMessage(" ", "content"));
    }
}
