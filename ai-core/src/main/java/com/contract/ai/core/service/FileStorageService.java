package com.contract.ai.core.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务接口
 * 负责处理上传文件的存储和URL生成
 */
public interface FileStorageService {

    /**
     * 存储文件并返回可访问的URL
     *
     * @param file 上传的文件
     * @return 文件的可访问URL
     */
    String storeFile(MultipartFile file);

    /**
     * 批量存储文件并返回文件名到URL的映射
     *
     * @param files 上传的文件数组
     * @return 文件名到URL的映射
     */
    java.util.Map<String, String> storeFiles(MultipartFile[] files);

    /**
     * 判断是否为支持的文件类型
     *
     * @param file 文件
     * @return 是否支持
     */
    boolean isSupportedFileType(MultipartFile file);
}