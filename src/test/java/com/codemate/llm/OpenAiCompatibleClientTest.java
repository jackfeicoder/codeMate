package com.codemate.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okio.Buffer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
    void buildsStreamingChatCompletionRequest() {
        JsonNode request = client.buildRequest(List.of(LlmMessage.user("hello")), true);

        assertEquals(true, request.path("stream").asBoolean());
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

    @Test
    void parsesStreamingContentDeltas() {
        String sse = """
                data: {"choices":[{"delta":{"content":"Hello"}}]}

                data: {"choices":[{"delta":{"content":" from"}}]}

                data: {"choices":[{"delta":{"content":" codeMate"}}],"usage":{"prompt_tokens":3,"completion_tokens":4}}

                data: [DONE]

                """;
        List<String> deltas = new ArrayList<>();

        LlmResponse response = client.parseStream(new Buffer().writeUtf8(sse), deltas::add);

        assertEquals("Hello from codeMate", response.content());
        assertEquals(List.of("Hello", " from", " codeMate"), deltas);
        assertEquals(3, response.inputTokens());
        assertEquals(4, response.outputTokens());
    }

    @Test
    void streamingErrorRaisesLlmException() {
        String sse = "data: {\"error\":{\"message\":\"bad request\"}}\n\n";

        assertThrows(LlmException.class, () -> client.parseStream(new Buffer().writeUtf8(sse), StreamListener.NO_OP));
    }
}
