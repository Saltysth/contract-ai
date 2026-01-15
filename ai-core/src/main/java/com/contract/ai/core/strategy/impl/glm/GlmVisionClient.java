package com.contract.ai.core.strategy.impl.glm;

import com.contract.ai.feign.dto.glm.GlmVisionChatRequest;
import com.contract.ai.feign.dto.glm.GlmVisionChatResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * GLM视觉模型Feign客户端
 */
@FeignClient(
    name = "glm-vision-service",
    url = "https://open.bigmodel.cn/api/paas/v4",
    configuration = GlmVisionConfiguration.class
)
public interface GlmVisionClient {

    /**
     * GLM视觉模型对话接口
     *
     * @param authorization Bearer token
     * @param request GLM视觉聊天请求
     * @return GLM视觉聊天响应
     */
    @PostMapping(value = "/chat/completions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    GlmVisionChatResponse chatCompletions(
            @RequestHeader("Authorization") String authorization,
            @RequestBody GlmVisionChatRequest request
    );
}