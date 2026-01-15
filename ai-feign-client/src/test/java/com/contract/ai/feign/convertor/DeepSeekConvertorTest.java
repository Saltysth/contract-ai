package com.contract.ai.feign.convertor;

import com.contract.ai.feign.dto.ChatRequest;
import com.contract.ai.feign.dto.ChatResponse;
import com.contract.ai.feign.dto.deepseek.DeepSeekChatRequest;
import com.contract.ai.feign.dto.deepseek.DeepSeekChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DeepSeek转换器测试
 */
class DeepSeekConvertorTest {

    private DeepSeekConvertor deepSeekConvertor;
    private ChatRequest standardChatRequest;
    private DeepSeekChatResponse deepSeekResponse;

    @BeforeEach
    void setUp() {
        deepSeekConvertor = new DeepSeekConvertor();

        standardChatRequest = ChatRequest.builder()
                .model("deepseek-chat")
                .messages(Arrays.asList(
                        ChatRequest.Message.textMessage("system", "You are a helpful assistant."),
                        ChatRequest.Message.textMessage("user", "Hello, how are you?")
                ))
                .maxTokens(1000)
                .temperature(0.7)
                .topP(0.8)
                .stream(false)
                .stop(Arrays.asList("END", "STOP"))
                .responseReformat(ChatRequest.ResponseReformat.builder().type("json_object").build())
                .n(1)
                .build();

        // 构建 DeepSeek 响应（使用秒级时间戳）
        long createdTimestamp = System.currentTimeMillis() / 1000;
        deepSeekResponse = new DeepSeekChatResponse();
        deepSeekResponse.setId("test-response-id");
        deepSeekResponse.setModel("deepseek-chat");
        deepSeekResponse.setCreated(createdTimestamp);
        deepSeekResponse.setObject("chat.completion");

        DeepSeekChatResponse.Choice choice = new DeepSeekChatResponse.Choice();
        choice.setIndex(0);
        choice.setFinishReason("stop");

        DeepSeekChatResponse.Message message = new DeepSeekChatResponse.Message();
        message.setRole("assistant");
        message.setContent("Hello! I'm doing well, thank you for asking.");
        choice.setMessage(message);

        deepSeekResponse.setChoices(Collections.singletonList(choice));

        DeepSeekChatResponse.Usage usage = new DeepSeekChatResponse.Usage();
        usage.setPromptTokens(25);
        usage.setCompletionTokens(18);
        usage.setTotalTokens(43);
        deepSeekResponse.setUsage(usage);
    }

    @Test
    void testConvertRequest() {
        DeepSeekChatRequest deepSeekRequest = deepSeekConvertor.convertRequest(standardChatRequest);

        assertNotNull(deepSeekRequest);
        assertEquals("deepseek-chat", deepSeekRequest.getModel());
        assertEquals(Integer.valueOf(1000), deepSeekRequest.getMaxTokens());
        assertEquals(Double.valueOf(0.7), deepSeekRequest.getTemperature());
        assertEquals(Double.valueOf(0.8), deepSeekRequest.getTopP());
        assertEquals(Boolean.FALSE, deepSeekRequest.getStream());
        assertEquals(Arrays.asList("END", "STOP"), deepSeekRequest.getStop());
        assertEquals(Integer.valueOf(1), deepSeekRequest.getN());

        // 验证消息转换
        assertNotNull(deepSeekRequest.getMessages());
        assertEquals(2, deepSeekRequest.getMessages().size());

        DeepSeekChatRequest.Message firstMessage = deepSeekRequest.getMessages().get(0);
        assertEquals("system", firstMessage.getRole());
        assertEquals("You are a helpful assistant.", firstMessage.getContent());

        DeepSeekChatRequest.Message secondMessage = deepSeekRequest.getMessages().get(1);
        assertEquals("user", secondMessage.getRole());
        assertEquals("Hello, how are you?", secondMessage.getContent());

        // 验证响应格式转换
        assertNotNull(deepSeekRequest.getResponseFormat());
        assertEquals("json_object", deepSeekRequest.getResponseFormat().getType());
    }

    @Test
    void testConvertRequestWithTextResponseFormat() {
        ChatRequest textRequest = ChatRequest.builder()
                .model("deepseek-chat")
                .messages(Collections.singletonList(ChatRequest.Message.textMessage("user", "Test")))
                .responseReformat(ChatRequest.ResponseReformat.builder().type("text").build())
                .build();

        DeepSeekChatRequest deepSeekRequest = deepSeekConvertor.convertRequest(textRequest);

        assertNotNull(deepSeekRequest.getResponseFormat());
        assertEquals("text", deepSeekRequest.getResponseFormat().getType());
    }

    @Test
    void testConvertRequestWithoutResponseFormat() {
        ChatRequest noFormatRequest = ChatRequest.builder()
                .model("deepseek-chat")
                .messages(Collections.singletonList(ChatRequest.Message.textMessage("user", "Test")))
                .build();

        DeepSeekChatRequest deepSeekRequest = deepSeekConvertor.convertRequest(noFormatRequest);

        assertNull(deepSeekRequest.getResponseFormat());
    }

    @Test
    void testConvertRequestWithNullInput() {
        DeepSeekChatRequest result = deepSeekConvertor.convertRequest(null);
        assertNull(result);
    }

    @Test
    void testConvertRequestWithExtensions() {
        // 测试包含扩展参数的请求转换
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("frequencyPenalty", 0.8);
        extensions.put("presencePenalty", 0.4);
        extensions.put("unknownParam", "shouldBeIgnored");

        ChatRequest requestWithExtensions = ChatRequest.builder()
                .model("deepseek-chat")
                .messages(Collections.singletonList(ChatRequest.Message.textMessage("user", "Test")))
                .extensions(extensions)
                .build();

        DeepSeekChatRequest deepSeekRequest = deepSeekConvertor.convertRequest(requestWithExtensions);

        assertNotNull(deepSeekRequest);
        assertEquals(Double.valueOf(0.8), deepSeekRequest.getFrequencyPenalty());
        assertEquals(Double.valueOf(0.4), deepSeekRequest.getPresencePenalty());
    }

    @Test
    void testConvertResponse() {
        ChatResponse response = deepSeekConvertor.convertResponse(deepSeekResponse);

        assertNotNull(response);
        assertEquals("test-response-id", response.getId());
        assertEquals("deepseek-chat", response.getModel());

        // 验证创建时间转换正确
        LocalDateTime expectedCreated = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochSecond(deepSeekResponse.getCreated()),
            ZoneId.systemDefault()
        );
        assertEquals(expectedCreated, response.getCreated());

        assertEquals("completed", response.getStatus()); // finish_reason="stop" -> status="completed"

        // 验证消息转换
        assertNotNull(response.getMessages());
        assertEquals(1, response.getMessages().size());

        ChatResponse.Message message = response.getMessages().get(0);
        assertEquals("assistant", message.getRole());
        assertEquals("Hello! I'm doing well, thank you for asking.", message.getContent());

        // 验证使用统计转换
        assertNotNull(response.getUsage());
        assertEquals(Integer.valueOf(25), response.getUsage().getPromptTokens());
        assertEquals(Integer.valueOf(18), response.getUsage().getCompletionTokens());
        assertEquals(Integer.valueOf(43), response.getUsage().getTotalTokens());
    }

    @Test
    void testConvertResponseWithLengthExceeded() {
        deepSeekResponse.getChoices().get(0).setFinishReason("length");

        ChatResponse response = deepSeekConvertor.convertResponse(deepSeekResponse);

        assertEquals("length_exceeded", response.getStatus());
    }

    @Test
    void testConvertResponseWithContentFilter() {
        deepSeekResponse.getChoices().get(0).setFinishReason("content_filter");

        ChatResponse response = deepSeekConvertor.convertResponse(deepSeekResponse);

        assertEquals("filtered", response.getStatus());
    }

    @Test
    void testConvertResponseWithEmptyChoices() {
        deepSeekResponse.setChoices(Collections.emptyList());

        ChatResponse response = deepSeekConvertor.convertResponse(deepSeekResponse);

        assertNotNull(response);
        assertEquals("test-response-id", response.getId());
        assertTrue(response.getMessages().isEmpty());
    }

    @Test
    void testConvertResponseWithNullChoices() {
        deepSeekResponse.setChoices(null);

        ChatResponse response = deepSeekConvertor.convertResponse(deepSeekResponse);

        assertNotNull(response);
        assertEquals("test-response-id", response.getId());
        assertTrue(response.getMessages().isEmpty());
    }

    @Test
    void testConvertResponseWithNullUsage() {
        deepSeekResponse.setUsage(null);

        ChatResponse response = deepSeekConvertor.convertResponse(deepSeekResponse);

        assertNotNull(response);
        assertEquals("test-response-id", response.getId());
        assertNull(response.getUsage());
    }

    @Test
    void testConvertResponseWithNullInput() {
        ChatResponse result = deepSeekConvertor.convertResponse(null);
        assertNull(result);
    }

    @Test
    void testConvertResponseWithExtensions() {
        // 测试包含特有字段的响应转换
        deepSeekResponse.setSystemFingerprint("fp_test123");
        deepSeekResponse.setObject("chat.completion");

        ChatResponse response = deepSeekConvertor.convertResponse(deepSeekResponse);

        assertNotNull(response);
        assertNotNull(response.getExtensions());
        assertEquals("fp_test123", response.getExtensions().get("systemFingerprint"));
        assertEquals("chat.completion", response.getExtensions().get("object"));
    }
}