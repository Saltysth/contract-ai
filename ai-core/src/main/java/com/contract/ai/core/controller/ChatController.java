package com.contract.ai.core.controller;

import com.contract.ai.core.service.ChatService;
import com.contract.ai.core.strategy.impl.glm.GlmVisionAiStrategy;
import com.contract.ai.feign.dto.ChatRequest;
import com.contract.ai.feign.dto.ChatResponse;
import com.contract.ai.feign.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.feign.annotation.RemotePreAuthorize;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * 聊天控制器
 * 提供REST入口，映射到Service与统一错误响应
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ObjectMapper objectMapper;
    private final GlmVisionAiStrategy glmVisionAiStrategy;

    /**
     * 聊天接口
     * 固定签名chat(ChatRequest)对外契约
     * 通用接口，支持纯文本、图片URL、文件URL等多种内容类型
     * 根据请求内容自动路由到对应的处理策略
     *
     * @param request 聊天请求，支持文本、图片URL、文件URL等
     * @return 聊天响应
     */
    @RemotePreAuthorize("@ss.hasAnyRoles('admin,common')")
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@Valid @RequestBody ChatRequest request) {
        String requestId = UUID.randomUUID().toString().replace("-", "");

        // 计算请求中的内容类型
        int imageCount = request.getMessages().stream()
            .filter(msg -> msg.isMultimodal())
            .flatMap(msg -> msg.getMultimodalContent().stream())
            .mapToInt(item -> "image_url".equals(item.getType()) ? 1 : 0)
            .sum();

        int fileCount = request.getMessages().stream()
            .filter(msg -> msg.isMultimodal())
            .flatMap(msg -> msg.getMultimodalContent().stream())
            .mapToInt(item -> "file_url".equals(item.getType()) ? 1 : 0)
            .sum();

        log.info("Received universal chat request [{}] for model: [{}] with {} images and {} files",
                requestId, request.getModel(), imageCount, fileCount);

        try {
            ChatResponse response = chatService.chat(request);
            log.info("Successfully processed universal chat request [{}] for model: [{}]",
                    requestId, request.getModel());
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            log.error("Error processing universal chat request [{}]: {}", requestId, e.getMessage(), e);
            throw e; // 重新抛出异常，让全局异常处理器处理
        }
    }

    /**
     * 支持图片URL的视觉聊天接口
     * 接收图片URL列表进行处理
     *
     * @param request 聊天请求，messages中包含image_url类型的content
     * @return 聊天响应
     */
    @RemotePreAuthorize("@ss.hasAnyRoles('admin,common')")
    @PostMapping(value = "/chat/vision/urls", consumes = "application/json")
    public ResponseEntity<ApiResponse<ChatResponse>> chatWithUrls(
            @Valid @RequestBody ChatRequest request) {

        String requestId = UUID.randomUUID().toString().replace("-", "");

        // 计算请求中的图片数量
        int imageCount = request.getMessages().stream()
            .filter(msg -> msg.isMultimodal())
            .flatMap(msg -> msg.getMultimodalContent().stream())
            .mapToInt(item -> "image_url".equals(item.getType()) ? 1 : 0)
            .sum();

        log.info("Received vision URLs chat request [{}] for model: [{}] with {} image URLs",
                requestId, request.getModel(), imageCount);

        try {
            ChatResponse response = chatService.chat(request);
            log.info("Successfully processed vision URLs chat request [{}] for model: [{}]", requestId, request.getModel());
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            log.error("Error processing vision URLs chat request [{}]: {}", requestId, e.getMessage(), e);
            throw e; // 重新抛出异常，让全局异常处理器处理
        }
    }

    /**
     * 支持文件上传的视觉聊天接口（使用base64编码）
     * 直接将上传文件转换为base64格式，无需通过文件存储服务
     *
     * @param requestJson JSON格式的聊天请求
     * @param files 上传的文件列表
     * @return 聊天响应
     */
    @RemotePreAuthorize("@ss.hasAnyRoles('admin,common')")
    @PostMapping(value = "/chat/vision/base64", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ChatResponse>> chatWithVisionBase64(
            @RequestPart("request") String requestJson,
            @RequestPart(value = "files", required = false) MultipartFile[] files) {

        String requestId = UUID.randomUUID().toString().replace("-", "");

        try {
            // 解析JSON请求
            ChatRequest request = objectMapper.readValue(requestJson, ChatRequest.class);

            log.info("Received vision chat request with base64 conversion [{}] for model: [{}] with {} files",
                    requestId, request.getModel(), files != null ? files.length : 0);

            ChatResponse response = glmVisionAiStrategy.handleChatWithVisionBase64(request, files);
            log.info("Successfully processed vision chat request with base64 conversion [{}] for model: [{}]",
                    requestId, request.getModel());
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Invalid JSON in vision chat request [{}]: {}", requestId, e.getMessage(), e);
            throw new IllegalArgumentException("请求JSON格式错误: " + e.getMessage(), e);

        } catch (Exception e) {
            log.error("Error processing vision chat request with base64 conversion [{}]: {}", requestId, e.getMessage(), e);
            throw e; // 重新抛出异常，让全局异常处理器处理
        }
    }

    /**
     * 健康检查接口
     *
     * @return 健康状态
     */
    @Anonymous
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    /**
     * 获取服务信息
     *
     * @return 服务信息
     */
    @Anonymous
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Object>> info() {
        var info = java.util.Map.of(
            "service", "contract-ai-service",
            "version", "1.0.0",
            "description", "AI多策略核心与Feign客户端双模块设计"
        );
        return ResponseEntity.ok(ApiResponse.success(info));
    }

    
  }