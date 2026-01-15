package com.contract.ai.core.exception;

import com.contractreview.exception.enums.ErrorCode;
import lombok.Getter;

/**
 * AI服务错误码
 * 继承统一的ErrorCode接口，用于AI相关业务的异常处理
 */
@Getter
public enum AiErrorCode implements ErrorCode {

    // AI服务错误 (AI001-AI999)
    AI_SERVICE_ERROR("AI001", "AI服务内部错误", "AI_SERVICE"),
    AI_SERVICE_UNAVAILABLE("AI002", "AI服务不可用", "AI_SERVICE"),
    AI_REQUEST_TIMEOUT("AI003", "AI请求超时", "AI_SERVICE"),
    AI_REQUEST_LIMIT_EXCEEDED("AI004", "AI请求频率超限", "AI_SERVICE"),
    AI_MODEL_NOT_SUPPORTED("AI005", "不支持的AI模型", "AI_SERVICE"),
    AI_RESPONSE_FORMAT_ERROR("AI006", "AI响应格式错误", "AI_SERVICE"),
    AI_AUTHENTICATION_FAILED("AI007", "AI服务认证失败", "AI_SERVICE"),
    AI_QUOTA_EXCEEDED("AI008", "AI服务配额不足", "AI_SERVICE"),
    AI_CONTENT_FILTERED("AI009", "AI内容被过滤", "AI_SERVICE"),
    AI_CONTEXT_TOO_LONG("AI010", "AI上下文过长", "AI_SERVICE"),

    // AI模型参数错误 (AIP001-AIP999)
    AI_INVALID_MODEL_PARAMETER("AIP001", "无效的模型参数", "AI_PARAMETER"),
    AI_MISSING_REQUIRED_PARAMETER("AIP002", "缺少必需参数", "AI_PARAMETER"),
    AI_INVALID_TEMPERATURE("AIP003", "无效的温度参数", "AI_PARAMETER"),
    AI_INVALID_MAX_TOKENS("AIP004", "无效的最大Token数", "AI_PARAMETER"),
    AI_INVALID_TOP_P("AIP005", "无效的Top-P参数", "AI_PARAMETER"),

    // AI网络通信错误 (AIN001-AIN999)
    AI_CONNECTION_FAILED("AIN001", "AI服务连接失败", "AI_NETWORK"),
    AI_CONNECTION_TIMEOUT("AIN002", "AI服务连接超时", "AI_NETWORK"),
    AI_READ_TIMEOUT("AIN003", "AI服务读取超时", "AI_NETWORK"),
    AI_NETWORK_UNREACHABLE("AIN004", "AI服务网络不可达", "AI_NETWORK");

    private final String code;
    private final String message;
    private final String category;

    AiErrorCode(String code, String message, String category) {
        this.code = code;
        this.message = message;
        this.category = category;
    }
}