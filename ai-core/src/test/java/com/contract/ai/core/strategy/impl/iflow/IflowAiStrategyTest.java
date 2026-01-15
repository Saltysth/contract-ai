package com.contract.ai.core.strategy.impl.iflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.contract.ai.feign.dto.ChatRequest;
import com.contract.ai.feign.dto.ChatResponse;
import com.contract.ai.feign.enums.PlatFormType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 心流平台AI策略测试
 */
@ExtendWith(MockitoExtension.class)
class IflowAiStrategyTest {

    @Mock
    private IflowClient iflowClient;

    @Mock
    private com.contract.ai.core.registry.AiStrategyRegistry strategyRegistry;

    private IflowAiStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new IflowAiStrategy(iflowClient, strategyRegistry);
        ReflectionTestUtils.setField(strategy, "enabledModels", Arrays.asList("GLM-4.6", "TBStars2-200B-A13B"));
    }

    @Test
    void testGetModel() {
        List<String> models = strategy.getModel();
        assertNotNull(models);
        assertEquals(2, models.size());
        assertEquals("GLM-4.6", models.get(0));
        assertEquals("TBStars2-200B-A13B", models.get(1));
    }

    @Test
    void testSupports() {
        assertTrue(strategy.supports("GLM-4.6"));
        assertTrue(strategy.supports("TBStars2-200B-A13B"));
        assertFalse(strategy.supports("gpt-3.5-turbo"));
    }

    @Test
    void testHandleChat_Success() {
        // 准备测试数据
        List<ChatRequest.Message> messages = Arrays.asList(
            ChatRequest.Message.textMessage("system", "你是一个专业的AI助手。"),
            ChatRequest.Message.textMessage("user", "请介绍一下人工智能的发展历史")
        );

        ChatRequest request = ChatRequest.builder()
            .platform(PlatFormType.IFLOW)
            .model("GLM-4.6")
            .messages(messages)
            .build();
        request.setTemperature(0.7);
        request.setMaxTokens(1000);

        // 模拟心流平台响应
        IflowResponse mockResponse = new IflowResponse();
        mockResponse.setId("chat_123456789");
        mockResponse.setModel("GLM-4.6");
        mockResponse.setCreated(System.currentTimeMillis());
        mockResponse.setObject("chat.completion");

        IflowResponse.Choice choice = new IflowResponse.Choice();
        choice.setIndex(0);
        choice.setFinishReason("stop");

        IflowResponse.Message message = new IflowResponse.Message();
        message.setRole("assistant");
        message.setContent("人工智能的发展历史可以追溯到20世纪50年代...");
        choice.setMessage(message);

        mockResponse.setChoices(Arrays.asList(choice));

        IflowResponse.Usage usage = new IflowResponse.Usage();
        usage.setPromptTokens(50);
        usage.setCompletionTokens(100);
        usage.setTotalTokens(150);
        mockResponse.setUsage(usage);

        when(iflowClient.chatCompletions(any(IflowRequest.class))).thenReturn(mockResponse);

        // 执行测试
        ChatResponse response = strategy.handleChat(request);

        // 验证结果
        assertNotNull(response);
        assertEquals("chat_123456789", response.getId());
        assertEquals("GLM-4.6", response.getModel());
        assertEquals("success", response.getStatus());
        assertNotNull(response.getMessages());
        assertEquals(1, response.getMessages().size());
        assertEquals("assistant", response.getMessages().get(0).getRole());
        assertTrue(response.getMessages().get(0).getContent().contains("人工智能"));
        assertNotNull(response.getUsage());
        assertEquals(50, response.getUsage().getPromptTokens());
        assertEquals(100, response.getUsage().getCompletionTokens());
        assertEquals(150, response.getUsage().getTotalTokens());
    }


    @Test
    void testHandleChat_EmptyMessages() {
        // 准备测试数据
        ChatRequest request = ChatRequest.builder()
            .platform(PlatFormType.IFLOW)
            .model("GLM-4.6")
            .messages(Arrays.asList())
            .build();

        // 模拟响应
        IflowResponse mockResponse = new IflowResponse();
        mockResponse.setId("chat_123");
        mockResponse.setModel("GLM-4.6");
        mockResponse.setCreated(System.currentTimeMillis());
        mockResponse.setChoices(Arrays.asList());

        when(iflowClient.chatCompletions(any(IflowRequest.class))).thenReturn(mockResponse);

        // 执行测试
        ChatResponse response = strategy.handleChat(request);

        // 验证结果
        assertNotNull(response);
        assertEquals("chat_123", response.getId());
        assertNotNull(response.getMessages());
        assertTrue(response.getMessages().isEmpty());
    }
}