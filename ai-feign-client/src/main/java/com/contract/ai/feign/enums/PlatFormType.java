package com.contract.ai.feign.enums;

/**
 * AI平台类型枚举
 * 用于选择不同的AI策略实现
 */
public enum PlatFormType {

    /**
     * IFLOW平台
     */
    IFLOW("IFLOW", "IFLOW平台"),

    /**
     * DEEPSEEK平台
     */
    DEEPSEEK("DEEPSEEK", "DEEPSEEK平台"),

    /**
     * GLM平台
     */
    GLM("GLM", "GLM平台");

    private final String code;
    private final String description;

    PlatFormType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据code查找枚举值
     */
    public static PlatFormType fromCode(String code) {
        if (code == null) {
            return null;
        }

        for (PlatFormType platform : values()) {
            if (platform.getCode().equals(code)) {
                return platform;
            }
        }

        throw new IllegalArgumentException("Unknown platform code: " + code);
    }
}