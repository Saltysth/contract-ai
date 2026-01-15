package com.contract.ai.core.strategy.impl.deepseek;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.contract.ai.core.exception.AiErrorCode;
import com.contract.ai.core.exception.AiException;
import com.contract.ai.core.registry.AiStrategyRegistry;
import com.contract.ai.feign.convertor.DeepSeekConvertor;
import com.contract.ai.feign.dto.ChatRequest;
import com.contract.ai.feign.dto.ChatResponse;
import com.contract.ai.feign.dto.deepseek.DeepSeekChatRequest;
import com.contract.ai.feign.dto.deepseek.DeepSeekChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * DeepSeek AI策略测试
 */
@ExtendWith(MockitoExtension.class)
class DeepSeekAiStrategyTest {

    @Mock
    private DeepSeekClient deepSeekClient;

    @Mock
    private AiStrategyRegistry strategyRegistry;

    @Mock
    private DeepSeekConvertor deepSeekConvertor;

    private DeepSeekAiStrategy deepSeekAiStrategy;

    private ChatRequest standardChatRequest;
    private DeepSeekChatResponse mockDeepSeekResponse;
    private ChatResponse expectedChatResponse;

    @BeforeEach
    void setUp() {
        // 手动创建策略实例
        deepSeekAiStrategy = new DeepSeekAiStrategy(deepSeekClient, strategyRegistry, deepSeekConvertor);

        // 设置启用的模型列表
        ReflectionTestUtils.setField(deepSeekAiStrategy, "enabledModels",
            Arrays.asList("deepseek-chat", "deepseek-reasoner"));

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
                .build();

        // 构建模拟的 DeepSeek 响应
        mockDeepSeekResponse = new DeepSeekChatResponse();
        mockDeepSeekResponse.setId("test-response-id");
        mockDeepSeekResponse.setModel("deepseek-chat");
        mockDeepSeekResponse.setCreated(System.currentTimeMillis() / 1000L);
        mockDeepSeekResponse.setObject("chat.completion");

        // 创建选择列表
        DeepSeekChatResponse.Choice choice = new DeepSeekChatResponse.Choice();
        choice.setIndex(0);
        choice.setFinishReason("stop");

        DeepSeekChatResponse.Message message = new DeepSeekChatResponse.Message();
        message.setRole("assistant");
        message.setContent("Hello! I'm doing well, thank you for asking. How can I help you today?");
        choice.setMessage(message);

        mockDeepSeekResponse.setChoices(Collections.singletonList(choice));

        // 创建使用统计
        DeepSeekChatResponse.Usage usage = new DeepSeekChatResponse.Usage();
        usage.setPromptTokens(20);
        usage.setCompletionTokens(15);
        usage.setTotalTokens(35);
        mockDeepSeekResponse.setUsage(usage);

        // 构建预期的标准响应
        expectedChatResponse = new ChatResponse();
        expectedChatResponse.setId("test-response-id");
        expectedChatResponse.setModel("deepseek-chat");
        expectedChatResponse.setCreated(LocalDateTime.now());
        expectedChatResponse.setStatus("completed");
        expectedChatResponse.setMessages(Collections.singletonList(
                new ChatResponse.Message("assistant", "Hello! I'm doing well, thank you for asking. How can I help you today?")
        ));

        ChatResponse.Usage expectedUsage = new ChatResponse.Usage();
        expectedUsage.setPromptTokens(20);
        expectedUsage.setCompletionTokens(15);
        expectedUsage.setTotalTokens(35);
        expectedChatResponse.setUsage(expectedUsage);
    }

    @Test
    void testGetModel() {
        List<String> models = deepSeekAiStrategy.getModel();
        assertFalse(models.isEmpty());
        assertEquals("deepseek-chat", models.get(0));
    }

    @Test
    void testSupports() {
        assertTrue(deepSeekAiStrategy.supports("deepseek-chat"));
        assertTrue(deepSeekAiStrategy.supports("deepseek-reasoner"));
        assertFalse(deepSeekAiStrategy.supports("gpt-4"));
        assertFalse(deepSeekAiStrategy.supports("unknown-model"));
    }

    @Test
    void testHandleChatSuccess() {
        // 模拟转换器调用
        when(deepSeekConvertor.convertRequest(any(ChatRequest.class)))
                .thenReturn(DeepSeekChatRequest.builder().build());
        when(deepSeekConvertor.convertResponse(any(DeepSeekChatResponse.class)))
                .thenReturn(expectedChatResponse);

        // 模拟成功调用
        when(deepSeekClient.chatCompletions(any(DeepSeekChatRequest.class)))
                .thenReturn(mockDeepSeekResponse);

        // 执行测试
        ChatResponse response = deepSeekAiStrategy.handleChat(standardChatRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals("test-response-id", response.getId());
        assertEquals("deepseek-chat", response.getModel());
        assertEquals("completed", response.getStatus());

        // 验证 API 被调用
        verify(deepSeekClient, times(1)).chatCompletions(any(DeepSeekChatRequest.class));
        verify(deepSeekConvertor, times(1)).convertRequest(any(ChatRequest.class));
        verify(deepSeekConvertor, times(1)).convertResponse(any(DeepSeekChatResponse.class));
    }

    @Test
    void testHandleChatWithJsonResponseFormat() {
        // 设置 JSON 响应格式
        ChatRequest jsonRequest = ChatRequest.builder()
                .model("deepseek-chat")
                .messages(Collections.singletonList(
                        ChatRequest.Message.textMessage("user", "Generate a JSON object")
                ))
                .responseReformat(ChatRequest.ResponseReformat.builder().type("json_object").build())
                .build();

        // 模拟转换器调用
        when(deepSeekConvertor.convertRequest(any(ChatRequest.class)))
                .thenReturn(DeepSeekChatRequest.builder().build());
        when(deepSeekConvertor.convertResponse(any(DeepSeekChatResponse.class)))
                .thenReturn(expectedChatResponse);

        // 模拟成功调用
        when(deepSeekClient.chatCompletions(any(DeepSeekChatRequest.class)))
                .thenReturn(mockDeepSeekResponse);

        // 执行测试
        ChatResponse response = deepSeekAiStrategy.handleChat(jsonRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals("test-response-id", response.getId());

        // 验证 API 被调用
        verify(deepSeekClient, times(1)).chatCompletions(any(DeepSeekChatRequest.class));
        verify(deepSeekConvertor, times(1)).convertRequest(any(ChatRequest.class));
        verify(deepSeekConvertor, times(1)).convertResponse(any(DeepSeekChatResponse.class));
    }

    @Test
    void testHandleChatApiException() {
        // 模拟转换器调用
        when(deepSeekConvertor.convertRequest(any(ChatRequest.class)))
                .thenReturn(DeepSeekChatRequest.builder().build());

        // 模拟 API 调用异常（使用不会映射到特定错误码的消息）
        when(deepSeekClient.chatCompletions(any(DeepSeekChatRequest.class)))
                .thenThrow(new RuntimeException("Network connection failed"));

        // 执行测试并验证异常
        AiException exception = assertThrows(AiException.class, () ->
            deepSeekAiStrategy.handleChat(standardChatRequest)
        );

        // 验证错误码类型
        assertEquals(AiErrorCode.AI_SERVICE_ERROR, exception.getErrorCode());
        verify(deepSeekClient, times(1)).chatCompletions(any(DeepSeekChatRequest.class));
        verify(deepSeekConvertor, times(1)).convertRequest(any(ChatRequest.class));
        verify(deepSeekConvertor, never()).convertResponse(any(DeepSeekChatResponse.class));
    }

    @Test
    void testHandleChatWithNullResponse() {
        // 模拟转换器调用
        when(deepSeekConvertor.convertRequest(any(ChatRequest.class)))
                .thenReturn(DeepSeekChatRequest.builder().build());

        // 模拟空响应
        when(deepSeekClient.chatCompletions(any(DeepSeekChatRequest.class)))
                .thenReturn(null);

        // 执行测试
        ChatResponse response = deepSeekAiStrategy.handleChat(standardChatRequest);

        // 应该返回 null 或处理空响应的逻辑
        assertNull(response);
        verify(deepSeekClient, times(1)).chatCompletions(any(DeepSeekChatRequest.class));
        verify(deepSeekConvertor, times(1)).convertRequest(any(ChatRequest.class));
        verify(deepSeekConvertor, never()).convertResponse(any(DeepSeekChatResponse.class));
    }

    @Test
    void testHandleChatWithEmptyChoices() {
        // 创建空选择列表的响应
        DeepSeekChatResponse emptyResponse = new DeepSeekChatResponse();
        emptyResponse.setId("empty-response");
        emptyResponse.setModel("deepseek-chat");
        emptyResponse.setChoices(Collections.emptyList());

        // 模拟转换器调用
        when(deepSeekConvertor.convertRequest(any(ChatRequest.class)))
                .thenReturn(DeepSeekChatRequest.builder().build());
        when(deepSeekConvertor.convertResponse(any(DeepSeekChatResponse.class)))
                .thenReturn(new ChatResponse());

        // 模拟 API 调用
        when(deepSeekClient.chatCompletions(any(DeepSeekChatRequest.class)))
                .thenReturn(emptyResponse);

        // 执行测试
        ChatResponse response = deepSeekAiStrategy.handleChat(standardChatRequest);

        // 验证结果
        assertNotNull(response);
        verify(deepSeekClient, times(1)).chatCompletions(any(DeepSeekChatRequest.class));
        verify(deepSeekConvertor, times(1)).convertRequest(any(ChatRequest.class));
        verify(deepSeekConvertor, times(1)).convertResponse(any(DeepSeekChatResponse.class));
    }
}