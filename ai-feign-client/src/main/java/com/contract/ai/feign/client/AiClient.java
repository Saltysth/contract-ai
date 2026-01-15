package com.contract.ai.feign.client;

import com.contract.ai.feign.dto.ApiResponse;
import com.contract.ai.feign.dto.ChatRequest;
import com.contract.ai.feign.dto.ChatResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI服务Feign客户端
 * 提供完整的AI服务接口，包括文本和视觉处理能力
 */
@FeignClient(
    name = "contract-ai-service",
    path = "/contract-ai/api/ai",
    configuration = AiClientConfiguration.class
)
public interface AiClient {

    /**
     * 聊天接口
     * 固定签名chat(ChatRequest)对外契约
     * 仅支持纯文本对话，不支持图片或文件上传
     * 如需图片处理，请使用 chatWithVisionUrls 或 chatWithVisionBase64 接口
     *
     * @param request 聊天请求
     * @return 聊天响应
     */
    @PostMapping("/chat")
    ApiResponse<ChatResponse> chat(@RequestBody ChatRequest request);

    @PostMapping("/chat")
    ApiResponse<ChatResponse> chat(@RequestBody ChatRequest request, @RequestHeader(name = "X-Internal-Auth-Secret", required = false) String authHeader);

    /**
     * 支持图片URL的视觉聊天接口
     * 接收图片URL列表进行处理
     *
     * @param requestJson JSON格式的聊天请求，messages中包含image_url类型的content
     * @return 聊天响应
     */
    @PostMapping(value = "/chat/vision/urls", consumes = "application/json")
    ApiResponse<ChatResponse> chatWithVisionUrls(@RequestBody String requestJson);

    /**
     * 支持文件上传的视觉聊天接口（使用base64编码）
     * 直接将上传文件转换为base64格式，无需通过文件存储服务
     *
     * @param requestJson JSON格式的聊天请求
     * @param files 上传的文件列表
     * @return 聊天响应
     */
    @PostMapping(value = "/chat/vision/base64", consumes = "multipart/form-data")
    ApiResponse<ChatResponse> chatWithVisionBase64(
            @RequestPart("request") String requestJson,
            @RequestPart(value = "files", required = false) MultipartFile[] files
    );

    /**
     * 健康检查接口
     *
     * @return 健康状态
     */
    @GetMapping("/health")
    ApiResponse<String> health();

    /**
     * 获取服务信息
     *
     * @return 服务信息
     */
    @GetMapping("/info")
    ApiResponse<Object> info();
}