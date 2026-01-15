package com.contract.ai.feign.enums;

/**
 * AI模型类型枚举
 * 定义各个AI平台支持的合法模型参数
 */
public enum ModelType {

    // ==================== DeepSeek 平台模型 ====================

    /**
     * DeepSeek 聊天模型
     * 平台：DEEPSEEK
     * 支持通用对话任务
     */
    DEEPSEEK_CHAT("deepseek-chat", "DeepSeek 聊天模型", PlatFormType.DEEPSEEK),

    /**
     * DeepSeek 推理模型
     * 平台：DEEPSEEK
     * 支持复杂推理任务
     */
    DEEPSEEK_REASONER("deepseek-reasoner", "DeepSeek 推理模型", PlatFormType.DEEPSEEK),

    // ==================== GLM 平台模型 ====================

    /**
     * GLM-4.1V 视觉思考模型
     * 平台：GLM
     * 支持多模态视觉理解与推理
     */
    GLM_4_1V_THINKING_FLASH("glm-4.1v-thinking-flash", "GLM-4.1V 视觉思考模型", PlatFormType.GLM),

    /**
     * GLM-4V-PLUS-0111 模型
     * 平台：GLM
     * 支持多模态视觉理解与推理
     */
    GLM_4V_PLUS_0111("glm-4v-plus-0111", "GLM-4V-PLUS-0111 视觉思考模型", PlatFormType.GLM),

    /**
     * GLM-4.5V 模型
     * 平台：GLM
     * 支持文件上传和多模态理解
     * 注意：GLM平台中唯一支持文件上传的模型
     */
    GLM_4_5V("glm-4.5v", "GLM-4.5V 多模态模型（支持文件上传）", PlatFormType.GLM),

    // ==================== IFLOW 平台模型 ====================

    /**
     * GLM-4.6 模型（通过心流平台）
     * 平台：IFLOW
     * 支持通用对话任务
     */
    IFlow_GLM_4_6("GLM-4.6", "GLM-4.6 模型（心流平台）", PlatFormType.IFLOW),

    /**
     * TBStars2-200B-A13B 模型
     * 平台：IFLOW
     * 支持高性能对话任务
     */
    IFlow_TBSTARS2_200B_A13B("TBStars2-200B-A13B", "TBStars2-200B-A13B 模型", PlatFormType.IFLOW);

    private final String modelCode;
    private final String description;
    private final PlatFormType platform;

    ModelType(String modelCode, String description, PlatFormType platform) {
        this.modelCode = modelCode;
        this.description = description;
        this.platform = platform;
    }

    public String getModelCode() {
        return modelCode;
    }

    public String getDescription() {
        return description;
    }

    public PlatFormType getPlatform() {
        return platform;
    }

    /**
     * 根据模型代码查找枚举值
     *
     * @param modelCode 模型代码
     * @return 对应的ModelType枚举值，如果未找到返回null
     */
    public static ModelType fromModelCode(String modelCode) {
        if (modelCode == null) {
            return null;
        }

        for (ModelType modelType : values()) {
            if (modelType.getModelCode().equals(modelCode)) {
                return modelType;
            }
        }

        return null;
    }

    /**
     * 根据平台类型查找该平台支持的所有模型
     *
     * @param platform 平台类型
     * @return 该平台支持的所有模型列表
     */
    public static ModelType[] getModelsByPlatform(PlatFormType platform) {
        return java.util.Arrays.stream(values())
                .filter(modelType -> modelType.getPlatform() == platform)
                .toArray(ModelType[]::new);
    }

    /**
     * 检查指定模型代码是否合法
     *
     * @param modelCode 模型代码
     * @return true表示合法，false表示不合法
     */
    public static boolean isValidModel(String modelCode) {
        return fromModelCode(modelCode) != null;
    }

    /**
     * 获取所有支持的模型代码列表
     *
     * @return 所有模型代码的字符串数组
     */
    public static String[] getAllModelCodes() {
        return java.util.Arrays.stream(values())
                .map(ModelType::getModelCode)
                .toArray(String[]::new);
    }

    /**
     * 获取指定平台支持的所有模型代码列表
     *
     * @param platform 平台类型
     * @return 该平台支持的模型代码数组
     */
    public static String[] getModelCodesByPlatform(PlatFormType platform) {
        return java.util.Arrays.stream(getModelsByPlatform(platform))
                .map(ModelType::getModelCode)
                .toArray(String[]::new);
    }
}