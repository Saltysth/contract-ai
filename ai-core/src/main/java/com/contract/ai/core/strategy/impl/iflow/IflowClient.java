package com.contract.ai.core.strategy.impl.iflow;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 心流平台Feign客户端
 * 配置为单线程并发以符合心流平台要求
 */
@FeignClient(
    name = "iflow-client",
    url = "${ai.strategy.iflow.base-url:https://apis.iflow.cn}",
    configuration = IflowConfiguration.class
)
public interface IflowClient {

    /**
     * 调用心流平台聊天完成API
     *
     * @param request 心流平台请求
     * @return 心流平台响应
     */
    @PostMapping(
        value = "/v1/chat/completions",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    IflowResponse chatCompletions(@RequestBody IflowRequest request);
}