package com.contract.ai.core.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * AI服务全局异常处理器
 * 处理AI服务特定的异常情况
 */
@Slf4j
@ControllerAdvice
public class AiGlobalExceptionHandler {

    /**
     * 处理异步请求不可用异常
     * 当客户端断开连接但服务器仍在尝试写入响应时触发
     *
     * @param e 异常对象
     * @return 空响应，不记录错误日志（这是正常的客户端断开场景）
     */
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public ResponseEntity<Void> handleAsyncRequestNotUsableException(AsyncRequestNotUsableException e) {
        // 记录DEBUG级别日志，不记录为错误
        if (log.isDebugEnabled()) {
            log.debug("客户端断开连接，停止写入响应: {}", e.getMessage());
        }
        // 返回204 No Content，表示请求已完成但无内容返回
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 处理客户端断开连接的IO异常
     *
     * @param e 异常对象
     * @return 空响应
     */
    @ExceptionHandler({java.io.IOException.class})
    public ResponseEntity<Void> handleIOException(java.io.IOException e) {
        // 检查是否是连接断开相关的问题
        String message = e.getMessage();
        if (message != null && (
            message.contains("Broken pipe") ||
            message.contains("Connection reset") ||
            message.contains("Client disconnected"))) {

            if (log.isDebugEnabled()) {
                log.debug("客户端连接断开: {}", e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        // 其他IO异常仍然记录为错误
        log.error("IO异常: {}", e.getMessage(), e);
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "IO_ERROR");
        errorResponse.put("message", "内部IO错误");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}