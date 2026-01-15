package com.contract.ai.feign.util;

import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Base64图片格式验证工具类
 * 用于验证和处理GLM视觉模型所需的base64图片格式
 */
public class ImageBase64Validator {

    private static final Pattern BASE64_IMAGE_PATTERN = Pattern.compile(
        "^data:image/(jpeg|jpg|png|webp|gif|bmp|tiff);base64,[A-Za-z0-9+/]+={0,2}$"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_PIXELS = 6000 * 6000; // 36M pixels

    /**
     * 验证base64图片格式是否正确
     *
     * @param base64Image base64图片字符串
     * @return 验证结果
     */
    public static ValidationResult validateBase64Image(String base64Image) {
        if (base64Image == null || base64Image.trim().isEmpty()) {
            return ValidationResult.error("图片内容不能为空");
        }

        // 检查格式前缀
        if (!base64Image.startsWith("data:image/")) {
            return ValidationResult.error("图片必须为base64格式，格式如：data:image/jpeg;base64,...");
        }

        // 正则验证格式
        if (!BASE64_IMAGE_PATTERN.matcher(base64Image).matches()) {
            return ValidationResult.error("base64图片格式不正确，应为：data:image/[format];base64,[data]");
        }

        try {
            // 提取base64数据部分
            String[] parts = base64Image.split(",");
            if (parts.length != 2) {
                return ValidationResult.error("base64图片格式不正确，缺少逗号分隔符");
            }

            String base64Data = parts[1];
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            // 检查文件大小
            if (imageBytes.length > MAX_FILE_SIZE) {
                return ValidationResult.error("图片大小超过限制（5MB）");
            }

            // 检查图片格式（通过文件头判断）
            String format = extractImageFormat(base64Image);
            if (!isValidImageFormat(imageBytes, format)) {
                return ValidationResult.error("不支持的图片格式或图片数据损坏");
            }

            return ValidationResult.success();

        } catch (IllegalArgumentException e) {
            return ValidationResult.error("base64编码格式不正确：" + e.getMessage());
        } catch (Exception e) {
            return ValidationResult.error("图片验证失败：" + e.getMessage());
        }
    }

    /**
     * 从base64字符串中提取图片格式
     */
    private static String extractImageFormat(String base64Image) {
        String prefix = base64Image.split(";")[0];
        return prefix.substring("data:image/".length());
    }

    /**
     * 标准化base64图片格式，确保符合GLM API要求
     *
     * @param base64Image 原始base64图片字符串
     * @return 标准化后的base64图片字符串
     */
    public static String normalizeBase64Image(String base64Image) {
        if (base64Image == null || base64Image.trim().isEmpty()) {
            return null;
        }

        base64Image = base64Image.trim();

        // 如果已经是标准格式，直接返回
        if (BASE64_IMAGE_PATTERN.matcher(base64Image).matches()) {
            return base64Image;
        }

        // 尝试自动修复格式
        try {
            String[] parts = base64Image.split(",");

            // 处理缺少data:image/前缀的情况
            if (parts.length == 1) {
                // 纯base64数据，需要检测格式并添加前缀
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                String format = detectImageFormat(imageBytes);
                if (format != null) {
                    return String.format("data:image/%s;base64,%s", format, base64Image);
                }
            } else if (parts.length == 2) {
                // 有前缀但可能格式不标准
                String prefix = parts[0];
                String data = parts[1];

                // 提取格式并标准化
                if (prefix.startsWith("data:image/")) {
                    String format = extractImageFormat(prefix);
                    // 确保格式小写
                    format = format.toLowerCase();
                    // 标准化格式名称
                    if (format.equals("jpg")) {
                        format = "jpeg";
                    }
                    return String.format("data:image/%s;base64,%s", format, data);
                }
            }
        } catch (Exception e) {
            // 格式修复失败，返回原值
        }

        return base64Image;
    }

    /**
     * 通过文件头检测图片格式
     *
     * @param imageBytes 图片字节数据
     * @return 图片格式字符串，如果无法识别则返回null
     */
    private static String detectImageFormat(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length < 4) {
            return null;
        }

        // JPEG文件头: FF D8 FF
        if (imageBytes.length >= 3 &&
            imageBytes[0] == (byte) 0xFF &&
            imageBytes[1] == (byte) 0xD8 &&
            imageBytes[2] == (byte) 0xFF) {
            return "jpeg";
        }

        // PNG文件头: 89 50 4E 47
        if (imageBytes.length >= 8 &&
            imageBytes[0] == (byte) 0x89 &&
            imageBytes[1] == 0x50 &&
            imageBytes[2] == 0x4E &&
            imageBytes[3] == 0x47) {
            return "png";
        }

        // WebP文件头: 52 49 46 46 ... 57 45 42 50
        if (imageBytes.length >= 12 &&
            imageBytes[0] == 0x52 && // R
            imageBytes[1] == 0x49 && // I
            imageBytes[2] == 0x46 && // F
            imageBytes[3] == 0x46 && // F
            imageBytes[8] == 0x57 && // W
            imageBytes[9] == 0x45 && // E
            imageBytes[10] == 0x42 && // B
            imageBytes[11] == 0x50) { // P
            return "webp";
        }

        // GIF文件头: 47 49 46 38
        if (imageBytes.length >= 6 &&
            imageBytes[0] == 0x47 && // G
            imageBytes[1] == 0x49 && // I
            imageBytes[2] == 0x46 && // F
            imageBytes[3] == 0x38 && // 8
            (imageBytes[4] == 0x37 || imageBytes[4] == 0x39) && // 7 or 9
            imageBytes[5] == 0x61) { // a
            return "gif";
        }

        // BMP文件头: 42 4D
        if (imageBytes.length >= 2 &&
            imageBytes[0] == 0x42 && // B
            imageBytes[1] == 0x4D) { // M
            return "bmp";
        }

        // TIFF文件头
        if (imageBytes.length >= 4) {
            if ((imageBytes[0] == 0x49 && // I
                 imageBytes[1] == 0x49 && // I
                 imageBytes[2] == 0x2A && // *
                 imageBytes[3] == 0x00) || // .
                (imageBytes[0] == 0x4D && // M
                 imageBytes[1] == 0x4D && // M
                 imageBytes[2] == 0x00 && // .
                 imageBytes[3] == 0x2A)) { // *
                return "tiff";
            }
        }

        return null;
    }

    /**
     * 验证图片格式是否有效
     */
    private static boolean isValidImageFormat(byte[] imageBytes, String format) {
        if (imageBytes == null || imageBytes.length < 4) {
            return false;
        }

        // 通过文件头验证图片格式
        if ("jpeg".equalsIgnoreCase(format) || "jpg".equalsIgnoreCase(format)) {
            // JPEG文件头: FF D8 FF
            return imageBytes.length >= 3 &&
                   imageBytes[0] == (byte) 0xFF &&
                   imageBytes[1] == (byte) 0xD8 &&
                   imageBytes[2] == (byte) 0xFF;
        } else if ("png".equalsIgnoreCase(format)) {
            // PNG文件头: 89 50 4E 47
            return imageBytes.length >= 8 &&
                   imageBytes[0] == (byte) 0x89 &&
                   imageBytes[1] == 0x50 &&
                   imageBytes[2] == 0x4E &&
                   imageBytes[3] == 0x47;
        } else if ("webp".equalsIgnoreCase(format)) {
            // WebP文件头: 52 49 46 46 ... 57 45 42 50
            return imageBytes.length >= 12 &&
                   imageBytes[0] == 0x52 && // R
                   imageBytes[1] == 0x49 && // I
                   imageBytes[2] == 0x46 && // F
                   imageBytes[3] == 0x46 && // F
                   imageBytes[8] == 0x57 && // W
                   imageBytes[9] == 0x45 && // E
                   imageBytes[10] == 0x42 && // B
                   imageBytes[11] == 0x50;  // P
        } else if ("gif".equalsIgnoreCase(format)) {
            // GIF文件头: 47 49 46 38
            return imageBytes.length >= 6 &&
                   ((imageBytes[0] == 0x47 && // G
                     imageBytes[1] == 0x49 && // I
                     imageBytes[2] == 0x46 && // F
                     imageBytes[3] == 0x38 && // 8
                     (imageBytes[4] == 0x37 || imageBytes[4] == 0x39) && // 7 or 9
                     imageBytes[5] == 0x61)); // a
        } else if ("bmp".equalsIgnoreCase(format)) {
            // BMP文件头: 42 4D
            return imageBytes.length >= 2 &&
                   imageBytes[0] == 0x42 && // B
                   imageBytes[1] == 0x4D;   // M
        } else if ("tiff".equalsIgnoreCase(format)) {
            // TIFF文件头: 49 49 2A 00 或 4D 4D 00 2A
            return imageBytes.length >= 4 &&
                   ((imageBytes[0] == 0x49 && // I
                     imageBytes[1] == 0x49 && // I
                     imageBytes[2] == 0x2A && // *
                     imageBytes[3] == 0x00) || // .
                    (imageBytes[0] == 0x4D && // M
                     imageBytes[1] == 0x4D && // M
                     imageBytes[2] == 0x00 && // .
                     imageBytes[3] == 0x2A)); // *
        }

        return false;
    }

    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        @Override
        public String toString() {
            return valid ? "Valid" : "Invalid: " + errorMessage;
        }
    }
}