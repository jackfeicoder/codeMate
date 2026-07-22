package com.codemate.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OpenAiCompatibleClientTest {
    private final OpenAiCompatibleClient client = new OpenAiCompatibleClient(
            new OkHttpClient(),
            new ObjectMapper(),
            "https://api.example.com/v1/",
            "test-credential",
            "example-model"
    );

    @Test
    void buildsChatCompletionRequest() {
        JsonNode request = client.buildRequest(List.of(
                LlmMessage.system("You are codeMate."),
                LlmMessage.user("Summarize this project.")
        ));

        assertEquals("example-model", request.path("model").asText());
        assertEquals(false, request.path("stream").asBoolean());
        assertEquals("system", request.path("messages").path(0).path("role").asText());
        assertEquals("You are codeMate.", request.path("messages").path(0).path("content").asText());
        assertEquals("user", request.path("messages").path(1).path("role").asText());
    }

    @Test
    void rejectsEmptyMessages() {
        assertThrows(IllegalArgumentException.class, () -> client.buildRequest(List.of()));
    }

    @Test
    void parsesChatCompletionContent() {
        String response = """
                {
                  "choices": [
                    {
                      "message": {
                        "role": "assistant",
                        "content": "Hello from codeMate."
                      }
                    }
                  ]
                }
                """;

        assertEquals("Hello from codeMate.", client.parseContent(response));
    }

    @Test
    void rejectsResponseWithoutContent() {
        assertThrows(LlmException.class, () -> client.parseContent("{\"choices\":[]}"));
    }
}
