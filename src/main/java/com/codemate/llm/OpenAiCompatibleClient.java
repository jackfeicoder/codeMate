package com.codemate.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class OpenAiCompatibleClient implements LlmClient {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String apiKey;
    private final String model;

    public OpenAiCompatibleClient(String baseUrl, String apiKey, String model) {
        this(
                new OkHttpClient.Builder()
                        .connectTimeout(Duration.ofSeconds(20))
                        .readTimeout(Duration.ofSeconds(120))
                        .build(),
                new ObjectMapper(),
                baseUrl,
                apiKey,
                model
        );
    }

    OpenAiCompatibleClient(
            OkHttpClient httpClient,
            ObjectMapper objectMapper,
            String baseUrl,
            String apiKey,
            String model
    ) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.baseUrl = requireText(baseUrl, "baseUrl");
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = requireText(model, "model");
    }

    @Override
    public LlmResponse complete(List<LlmMessage> messages) {
        return execute(messages, false, StreamListener.NO_OP);
    }

    @Override
    public LlmResponse stream(List<LlmMessage> messages, StreamListener listener) {
        return execute(messages, true, listener == null ? StreamListener.NO_OP : listener);
    }

    private LlmResponse execute(List<LlmMessage> messages, boolean stream, StreamListener listener) {
        if (apiKey.isBlank()) {
            throw new LlmException("Missing CODEMATE_API_KEY");
        }

        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(buildRequest(messages, stream));
        } catch (IOException e) {
            throw new LlmException("Failed to serialize LLM request", e);
        }

        Request request = new Request.Builder()
                .url(chatCompletionsUrl())
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestJson, JSON))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            ResponseBody body = response.body();
            if (!response.isSuccessful()) {
                String responseText = body == null ? "" : body.string();
                throw new LlmException("LLM request failed with HTTP " + response.code() + ": " + responseText);
            }
            if (body == null) {
                throw new LlmException("LLM response body was empty");
            }
            if (stream) {
                return parseStream(body.source(), listener);
            }
            String responseText = body.string();
            return new LlmResponse(parseContent(responseText));
        } catch (IOException e) {
            throw new LlmException("LLM request failed", e);
        }
    }

    ObjectNode buildRequest(List<LlmMessage> messages) {
        return buildRequest(messages, false);
    }

    ObjectNode buildRequest(List<LlmMessage> messages, boolean stream) {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("messages must not be empty");
        }

        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", model);
        root.put("stream", stream);

        ArrayNode messageNodes = root.putArray("messages");
        for (LlmMessage message : messages) {
            ObjectNode messageNode = messageNodes.addObject();
            messageNode.put("role", message.role());
            messageNode.put("content", message.content());
        }

        return root;
    }

    LlmResponse parseStream(BufferedSource source, StreamListener listener) {
        StreamListener streamListener = listener == null ? StreamListener.NO_OP : listener;
        StringBuilder content = new StringBuilder();
        int inputTokens = 0;
        int outputTokens = 0;

        try {
            while (!source.exhausted()) {
                String line = source.readUtf8Line();
                if (line == null) {
                    break;
                }

                String trimmed = line.trim();
                if (trimmed.isEmpty() || !trimmed.startsWith("data:")) {
                    continue;
                }

                String payload = trimmed.substring("data:".length()).trim();
                if (payload.isEmpty()) {
                    continue;
                }
                if ("[DONE]".equals(payload)) {
                    break;
                }

                JsonNode root = objectMapper.readTree(payload);
                JsonNode error = root.path("error");
                if (!error.isMissingNode() && !error.isNull()) {
                    throw new LlmException("LLM stream returned error: " + error);
                }

                JsonNode usage = root.path("usage");
                if (!usage.isMissingNode()) {
                    inputTokens = usage.path("prompt_tokens").asInt(inputTokens);
                    outputTokens = usage.path("completion_tokens").asInt(outputTokens);
                }

                String delta = root.path("choices").path(0).path("delta").path("content").asText("");
                if (!delta.isEmpty()) {
                    content.append(delta);
                    streamListener.onContentDelta(delta);
                }
            }
        } catch (IOException e) {
            throw new LlmException("Failed to parse LLM stream", e);
        }

        return new LlmResponse(content.toString(), inputTokens, outputTokens);
    }

    String parseContent(String responseText) {
        try {
            JsonNode root = objectMapper.readTree(responseText);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.isNull()) {
                throw new LlmException("LLM response did not contain choices[0].message.content");
            }
            return content.asText();
        } catch (IOException e) {
            throw new LlmException("Failed to parse LLM response", e);
        }
    }

    private String chatCompletionsUrl() {
        return baseUrl.replaceAll("/+$", "") + "/chat/completions";
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
