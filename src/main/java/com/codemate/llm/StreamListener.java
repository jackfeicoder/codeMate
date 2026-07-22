package com.codemate.llm;

public interface StreamListener {
    StreamListener NO_OP = delta -> {
    };

    void onContentDelta(String delta);
}
