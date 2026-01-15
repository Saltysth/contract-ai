package com.contract.ai.core.exception;

import com.contractreview.exception.core.BaseException;
import com.contractreview.exception.enums.ErrorCode;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * AI服务异常类
 * 继承统一的BaseException，集成到全局异常处理体系
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class AiException extends BaseException {

    /**
     * 使用AI错误码构造异常
     */
    public AiException(AiErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * 使用AI错误码和原因构造异常
     */
    public AiException(AiErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    /**
     * 使用AI错误码和参数构造异常
     */
    public AiException(AiErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    /**
     * 使用AI错误码、原因和参数构造异常
     */
    public AiException(AiErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }

    /**
     * 使用通用错误码构造异常
     */
    public AiException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * 使用通用错误码和原因构造异常
     */
    public AiException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    /**
     * 使用通用错误码和参数构造异常
     */
    public AiException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    /**
     * 使用通用错误码、原因和参数构造异常
     */
    public AiException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }

    /**
     * 创建AI服务错误异常
     */
    public static AiException aiServiceError(String message) {
        return new AiException(AiErrorCode.AI_SERVICE_ERROR);
    }

    /**
     * 创建AI服务不可用异常
     */
    public static AiException aiServiceUnavailable() {
        return new AiException(AiErrorCode.AI_SERVICE_UNAVAILABLE);
    }

    /**
     * 创建AI请求超时异常
     */
    public static AiException aiRequestTimeout() {
        return new AiException(AiErrorCode.AI_REQUEST_TIMEOUT);
    }

    /**
     * 创建AI请求频率超限异常
     */
    public static AiException aiRequestLimitExceeded() {
        return new AiException(AiErrorCode.AI_REQUEST_LIMIT_EXCEEDED);
    }

    /**
     * 创建AI模型不支持异常
     */
    public static AiException aiModelNotSupported(String model) {
        return new AiException(AiErrorCode.AI_MODEL_NOT_SUPPORTED, model);
    }

    /**
     * 创建AI响应格式错误异常
     */
    public static AiException aiResponseFormatError() {
        return new AiException(AiErrorCode.AI_RESPONSE_FORMAT_ERROR);
    }

    /**
     * 创建AI认证失败异常
     */
    public static AiException aiAuthenticationFailed() {
        return new AiException(AiErrorCode.AI_AUTHENTICATION_FAILED);
    }

    /**
     * 创建AI配额不足异常
     */
    public static AiException aiQuotaExceeded() {
        return new AiException(AiErrorCode.AI_QUOTA_EXCEEDED);
    }

    /**
     * 创建AI内容被过滤异常
     */
    public static AiException aiContentFiltered() {
        return new AiException(AiErrorCode.AI_CONTENT_FILTERED);
    }

    /**
     * 创建AI上下文过长异常
     */
    public static AiException aiContextTooLong() {
        return new AiException(AiErrorCode.AI_CONTEXT_TOO_LONG);
    }

    /**
     * 创建无效模型参数异常
     */
    public static AiException invalidModelParameter(String parameter) {
        return new AiException(AiErrorCode.AI_INVALID_MODEL_PARAMETER, parameter);
    }

    /**
     * 创建缺少必需参数异常
     */
    public static AiException missingRequiredParameter(String parameter) {
        return new AiException(AiErrorCode.AI_MISSING_REQUIRED_PARAMETER, parameter);
    }

    /**
     * 创建AI连接失败异常
     */
    public static AiException aiConnectionFailed() {
        return new AiException(AiErrorCode.AI_CONNECTION_FAILED);
    }

    /**
     * 创建AI连接超时异常
     */
    public static AiException aiConnectionTimeout() {
        return new AiException(AiErrorCode.AI_CONNECTION_TIMEOUT);
    }

    /**
     * 创建AI读取超时异常
     */
    public static AiException aiReadTimeout() {
        return new AiException(AiErrorCode.AI_READ_TIMEOUT);
    }
}