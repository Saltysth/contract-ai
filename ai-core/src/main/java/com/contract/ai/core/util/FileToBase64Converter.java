package com.contract.ai.core.util;

import com.contract.ai.feign.util.ImageBase64Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件转Base64编码工具类
 * 用于将上传的文件转换为base64格式，直接用于GLM视觉模型调用
 */
@Slf4j
public class FileToBase64Converter {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB，与GLM限制一致

    /**
     * 将多个上传文件转换为base64格式映射
     *
     * @param files 上传的文件数组
     * @return 文件名到base64字符串的映射，如果转换失败则返回空映射
     */
    public static Map<String, String> convertFilesToBase64(MultipartFile[] files) {
        Map<String, String> base64Map = new HashMap<>();

        if (files == null || files.length == 0) {
            log.warn("没有提供上传文件");
            return base64Map;
        }

        for (MultipartFile file : files) {
            try {
                String base64Image = convertSingleFileToBase64(file);
                if (base64Image != null) {
                    String fileName = generateUniqueFileName(file);
                    base64Map.put(fileName, base64Image);
                    log.info("成功转换文件为base64格式: {}", fileName);
                }
            } catch (Exception e) {
                log.error("转换文件为base64失败: {}", file.getOriginalFilename(), e);
                // 继续处理其他文件，不中断整个流程
            }
        }

        log.info("共{}个文件，成功转换{}个为base64格式", files.length, base64Map.size());
        return base64Map;
    }

    /**
     * 将单个文件转换为base64格式
     *
     * @param file 上传的文件
     * @return base64格式的图片字符串，转换失败返回null
     */
    public static String convertSingleFileToBase64(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("文件为空，跳过转换");
            return null;
        }

        try {
            // 检查文件大小
            if (file.getSize() > MAX_FILE_SIZE) {
                log.warn("文件大小超过限制: {} bytes, 文件名: {}", file.getSize(), file.getOriginalFilename());
                return null;
            }

            // 读取文件内容
            byte[] fileBytes = file.getBytes();

            // 转换为base64
            String base64Data = Base64.getEncoder().encodeToString(fileBytes);

            // 检测图片格式
            String format = detectImageFormat(fileBytes, file.getOriginalFilename());
            if (format == null) {
                log.warn("无法识别文件格式: {}", file.getOriginalFilename());
                return null;
            }

            // 构建完整的base64 URL格式
            String base64Image = String.format("data:image/%s;base64,%s", format, base64Data);

            // 验证生成的base64格式
            ImageBase64Validator.ValidationResult validation = ImageBase64Validator.validateBase64Image(base64Image);
            if (!validation.isValid()) {
                log.warn("生成的base64格式验证失败: {}, 文件: {}", validation.getErrorMessage(), file.getOriginalFilename());
                return null;
            }

            // 标准化格式
            return ImageBase64Validator.normalizeBase64Image(base64Image);

        } catch (IOException e) {
            log.error("读取文件内容失败: {}", file.getOriginalFilename(), e);
            return null;
        } catch (Exception e) {
            log.error("转换文件为base64时发生未知错误: {}", file.getOriginalFilename(), e);
            return null;
        }
    }

    /**
     * 通过文件内容和文件名检测图片格式
     *
     * @param fileBytes 文件字节数据
     * @param originalFilename 原始文件名
     * @return 图片格式字符串，无法识别时返回null
     */
    private static String detectImageFormat(byte[] fileBytes, String originalFilename) {
        if (fileBytes == null || fileBytes.length < 4) {
            return null;
        }

        // 首先通过文件头检测格式
        String format = detectFormatByFileHeader(fileBytes);
        if (format != null) {
            return format;
        }

        // 如果文件头检测失败，尝试通过文件名扩展名检测
        if (originalFilename != null && originalFilename.contains(".")) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

            // 标准化扩展名
            switch (extension) {
                case "jpg":
                case "jpeg":
                    return "jpeg";
                case "png":
                    return "png";
                case "webp":
                    return "webp";
                case "gif":
                    return "gif";
                case "bmp":
                    return "bmp";
                case "tiff":
                case "tif":
                    return "tiff";
                default:
                    log.warn("不支持的文件扩展名: {}", extension);
                    return null;
            }
        }

        return null;
    }

    /**
     * 通过文件头检测图片格式
     *
     * @param fileBytes 文件字节数据
     * @return 图片格式字符串，无法识别时返回null
     */
    private static String detectFormatByFileHeader(byte[] fileBytes) {
        // JPEG文件头: FF D8 FF
        if (fileBytes.length >= 3 &&
            fileBytes[0] == (byte) 0xFF &&
            fileBytes[1] == (byte) 0xD8 &&
            fileBytes[2] == (byte) 0xFF) {
            return "jpeg";
        }

        // PNG文件头: 89 50 4E 47
        if (fileBytes.length >= 8 &&
            fileBytes[0] == (byte) 0x89 &&
            fileBytes[1] == 0x50 &&
            fileBytes[2] == 0x4E &&
            fileBytes[3] == 0x47) {
            return "png";
        }

        // WebP文件头: 52 49 46 46 ... 57 45 42 50
        if (fileBytes.length >= 12 &&
            fileBytes[0] == 0x52 && // R
            fileBytes[1] == 0x49 && // I
            fileBytes[2] == 0x46 && // F
            fileBytes[3] == 0x46 && // F
            fileBytes[8] == 0x57 && // W
            fileBytes[9] == 0x45 && // E
            fileBytes[10] == 0x42 && // B
            fileBytes[11] == 0x50) { // P
            return "webp";
        }

        // GIF文件头: 47 49 46 38
        if (fileBytes.length >= 6 &&
            fileBytes[0] == 0x47 && // G
            fileBytes[1] == 0x49 && // I
            fileBytes[2] == 0x46 && // F
            fileBytes[3] == 0x38 && // 8
            (fileBytes[4] == 0x37 || fileBytes[4] == 0x39) && // 7 or 9
            fileBytes[5] == 0x61) { // a
            return "gif";
        }

        // BMP文件头: 42 4D
        if (fileBytes.length >= 2 &&
            fileBytes[0] == 0x42 && // B
            fileBytes[1] == 0x4D) { // M
            return "bmp";
        }

        // TIFF文件头
        if (fileBytes.length >= 4) {
            if ((fileBytes[0] == 0x49 && // I
                 fileBytes[1] == 0x49 && // I
                 fileBytes[2] == 0x2A && // *
                 fileBytes[3] == 0x00) || // .
                (fileBytes[0] == 0x4D && // M
                 fileBytes[1] == 0x4D && // M
                 fileBytes[2] == 0x00 && // .
                 fileBytes[3] == 0x2A)) { // *
                return "tiff";
            }
        }

        return null;
    }

    /**
     * 生成唯一的文件名，用于base64映射的键
     *
     * @param file 上传的文件
     * @return 唯一的文件名
     */
    private static String generateUniqueFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String baseName = originalFilename != null ?
            originalFilename.substring(0, originalFilename.lastIndexOf('.')) : "image";

        // 清理文件名中的特殊字符
        baseName = baseName.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_");

        // 生成UUID确保唯一性
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return String.format("%s_%s", baseName, uuid);
    }

    /**
     * 验证文件是否为支持的图片格式
     *
     * @param file 上传的文件
     * @return 如果是支持的图片格式返回true，否则返回false
     */
    public static boolean isSupportedImageFormat(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            return false;
        }

        try {
            byte[] fileBytes = file.getBytes();
            String format = detectImageFormat(fileBytes, file.getOriginalFilename());
            return format != null;
        } catch (IOException e) {
            log.error("读取文件内容失败: {}", file.getOriginalFilename(), e);
            return false;
        }
    }

    /**
     * 获取支持的图片格式列表
     *
     * @return 支持的图片格式字符串数组
     */
    public static String[] getSupportedFormats() {
        return new String[]{"jpeg", "jpg", "png", "webp", "gif", "bmp", "tiff"};
    }
}