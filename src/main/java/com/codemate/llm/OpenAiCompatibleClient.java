package com.codemate.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.MediaType;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Protocol;
import okhttp3.TlsVersion;
import okio.BufferedSource;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Map;

import com.codemate.tool.ToolDefinition;

public class OpenAiCompatibleClient implements LlmClient {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String apiKey;
    private final String model;

    public OpenAiCompatibleClient(String baseUrl, String apiKey, String model) {
        this(baseUrl, apiKey, model, false);
    }

    public OpenAiCompatibleClient(String baseUrl, String apiKey, String model, boolean forceHttp11) {
        this(
                createHttpClient(forceHttp11),
                new ObjectMapper(),
                baseUrl,
                apiKey,
                model
        );
    }

    static OkHttpClient createHttpClient(boolean forceHttp11) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(20))
                .readTimeout(Duration.ofSeconds(120));
        if (forceHttp11) {
            builder.protocols(List.of(Protocol.HTTP_1_1));
            ConnectionSpec tls12 = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .build();
            builder.connectionSpecs(List.of(tls12));
        }
        return builder.build();
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
        return execute(messages, List.of(), false, StreamListener.NO_OP);
    }

    @Override
    public LlmResponse stream(List<LlmMessage> messages, StreamListener listener) {
        return execute(messages, List.of(), true, listener == null ? StreamListener.NO_OP : listener);
    }

    @Override
    public LlmResponse stream(List<LlmMessage> messages, List<ToolDefinition> tools, StreamListener listener) {
        return execute(messages, tools, true, listener == null ? StreamListener.NO_OP : listener);
    }

    private LlmResponse execute(List<LlmMessage> messages, List<ToolDefinition> tools, boolean stream, StreamListener listener) {
        if (apiKey.isBlank()) {
            throw new LlmException("Missing CODEMATE_API_KEY");
        }

        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(buildRequest(messages, tools, stream));
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
            return parseResponse(responseText);
        } catch (IOException e) {
            String detail = e.getMessage();
            throw new LlmException("LLM request failed" + (detail == null || detail.isBlank() ? "" : ": " + detail), e);
        }
    }

    ObjectNode buildRequest(List<LlmMessage> messages) {
        return buildRequest(messages, false);
    }

    ObjectNode buildRequest(List<LlmMessage> messages, boolean stream) {
        return buildRequest(messages, List.of(), stream);
    }

    ObjectNode buildRequest(List<LlmMessage> messages, List<ToolDefinition> tools, boolean stream) {
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

        if (tools != null && !tools.isEmpty()) {
            ArrayNode toolNodes = root.putArray("tools");
            for (ToolDefinition tool : tools) {
                ObjectNode toolNode = toolNodes.addObject();
                toolNode.put("type", "function");
                ObjectNode function = toolNode.putObject("function");
                function.put("name", tool.name());
                function.put("description", tool.description());
                function.set("parameters", objectMapper.valueToTree(tool.parameters()));
            }
            root.put("tool_choice", "auto");
        }

        return root;
    }

    LlmResponse parseStream(BufferedSource source, StreamListener listener) {
        StreamListener streamListener = listener == null ? StreamListener.NO_OP : listener;
        StringBuilder content = new StringBuilder();
        Map<Integer, ToolCallAccumulator> toolCalls = new LinkedHashMap<>();
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

                for (JsonNode toolCall : root.path("choices").path(0).path("delta").path("tool_calls")) {
                    int index = toolCall.path("index").asInt(toolCalls.size());
                    ToolCallAccumulator accumulator = toolCalls.computeIfAbsent(index, ignored -> new ToolCallAccumulator());
                    accumulator.id = textIfPresent(toolCall, "id", accumulator.id);
                    accumulator.name = textIfPresent(toolCall.path("function"), "name", accumulator.name);
                    accumulator.arguments.append(toolCall.path("function").path("arguments").asText(""));
                }
            }
        } catch (IOException e) {
            throw new LlmException("Failed to parse LLM stream", e);
        }

        return new LlmResponse(content.toString(), inputTokens, outputTokens, toToolCalls(toolCalls));
    }

    String parseContent(String responseText) {
        return parseResponse(responseText).content();
    }

    LlmResponse parseResponse(String responseText) {
        try {
            JsonNode root = objectMapper.readTree(responseText);
            JsonNode message = root.path("choices").path(0).path("message");
            if (message.isMissingNode()) {
                throw new LlmException("LLM response did not contain choices[0].message");
            }
            List<ToolCall> toolCalls = new ArrayList<>();
            for (JsonNode toolCall : message.path("tool_calls")) {
                toolCalls.add(new ToolCall(
                        toolCall.path("id").asText(""),
                        toolCall.path("function").path("name").asText(""),
                        toolCall.path("function").path("arguments").asText("{}")
                ));
            }
            JsonNode content = message.path("content");
            return new LlmResponse(content.isMissingNode() || content.isNull() ? "" : content.asText(), 0, 0, toolCalls);
        } catch (IOException e) {
            throw new LlmException("Failed to parse LLM response", e);
        }
    }

    private static List<ToolCall> toToolCalls(Map<Integer, ToolCallAccumulator> values) {
        return values.values().stream()
                .filter(value -> value.name != null && !value.name.isBlank())
                .map(value -> new ToolCall(value.id, value.name, value.arguments.toString()))
                .toList();
    }

    private static String textIfPresent(JsonNode node, String field, String fallback) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? fallback : value.asText(fallback);
    }

    private static final class ToolCallAccumulator {
        private String id = "";
        private String name = "";
        private final StringBuilder arguments = new StringBuilder();
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
