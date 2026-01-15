package com.contract.ai.core.service.impl;

import com.contract.ai.core.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件存储服务实现类
 * 将文件存储到本地并返回访问URL
 */
@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${app.file.base-url:http://localhost:18080/api/files}")
    private String baseUrl;

    private static final long MAX_FILE_SIZE = 200 * 1024 * 1024; // 200MB

    @Override
    public String storeFile(MultipartFile file) {
        try {
            // 创建上传目录
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 验证文件
            if (!isSupportedFileType(file)) {
                throw new IllegalArgumentException("不支持的文件类型: " + file.getContentType());
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("文件大小超过限制: " + file.getSize() + " bytes");
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // 存储文件
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 返回访问URL
            return baseUrl + "/" + uniqueFilename;

        } catch (IOException e) {
            log.error("文件存储失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件存储失败", e);
        }
    }

    @Override
    public Map<String, String> storeFiles(MultipartFile[] files) {
        Map<String, String> fileUrlMap = new HashMap<>();

        if (files != null) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    String fileName = file.getOriginalFilename();
                    String fileUrl = storeFile(file);
                    fileUrlMap.put(fileName, fileUrl);
                }
            }
        }

        return fileUrlMap;
    }

    @Override
    public boolean isSupportedFileType(MultipartFile file) {
        if (file == null || file.getContentType() == null) {
            return false;
        }

        String contentType = file.getContentType().toLowerCase();
        String originalFilename = file.getOriginalFilename();

        // 支持的图片类型
        if (contentType.startsWith("image/") &&
            (contentType.contains("jpeg") || contentType.contains("jpg") || contentType.contains("png"))) {
            return true;
        }

        // 支持的视频类型
        if (contentType.startsWith("video/") && contentType.contains("mp4")) {
            return true;
        }

        // 支持的文档类型
        if (contentType.equals("application/pdf") ||
            contentType.equals("application/msword") ||
            contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            return true;
        }

        // 根据文件扩展名检查
        if (originalFilename != null) {
            String lowerFilename = originalFilename.toLowerCase();
            return lowerFilename.endsWith(".jpg") ||
                   lowerFilename.endsWith(".jpeg") ||
                   lowerFilename.endsWith(".png") ||
                   lowerFilename.endsWith(".mp4") ||
                   lowerFilename.endsWith(".pdf") ||
                   lowerFilename.endsWith(".doc") ||
                   lowerFilename.endsWith(".docx");
        }

        return false;
    }
}