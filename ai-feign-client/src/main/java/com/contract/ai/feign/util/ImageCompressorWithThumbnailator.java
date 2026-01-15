package com.contract.ai.feign.util;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 使用Thumbnailator库的图片压缩工具类
 * 支持多种图片格式，压缩效果优秀
 * 
 * Maven依赖：
 * <dependency>
 *     <groupId>net.coobird</groupId>
 *     <artifactId>thumbnailator</artifactId>
 *     <version>0.4.19</version>
 * </dependency>
 */
public class ImageCompressorWithThumbnailator {

    /**
     * 压缩图片到指定大小
     * @param inputPath 输入图片路径
     * @param outputPath 输出图片路径
     * @param targetSizeKB 目标大小（KB）
     * @return 是否压缩成功
     */
    public static boolean compressImage(String inputPath, String outputPath, int targetSizeKB) {
        try {
            if (!Files.exists(Paths.get(inputPath))) {
                System.err.println("输入文件不存在: " + inputPath);
                return false;
            }

            long targetSizeBytes = targetSizeKB * 1024L;
            long originalSize = Files.size(Paths.get(inputPath));
            
            if (originalSize <= targetSizeBytes) {
                Files.copy(Paths.get(inputPath), Paths.get(outputPath));
                System.out.println("原文件已满足大小要求，直接复制");
                return true;
            }

            // 首先尝试质量压缩
            if (compressWithQuality(inputPath, outputPath, targetSizeBytes)) {
                return true;
            }

            // 如果质量压缩无效，尝试尺寸压缩
            return compressWithResize(inputPath, outputPath, targetSizeBytes);

        } catch (Exception e) {
            System.err.println("压缩图片时发生错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 通过调整质量进行压缩
     */
    private static boolean compressWithQuality(String inputPath, String outputPath, long targetSizeBytes) {
        try {
            float[] qualities = {0.9f, 0.8f, 0.7f, 0.6f, 0.5f, 0.4f, 0.3f, 0.2f, 0.1f};
            
            for (float quality : qualities) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                
                Thumbnails.of(inputPath)
                        .scale(1.0)
                        .outputQuality(quality)
                        .toOutputStream(baos);
                
                if (baos.size() <= targetSizeBytes) {
                    Files.write(Paths.get(outputPath), baos.toByteArray());
                    System.out.println("质量压缩成功，质量: " + quality + ", 大小: " + baos.size() + " 字节");
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("质量压缩失败: " + e.getMessage());
        }
        return false;
    }

    /**
     * 通过调整尺寸进行压缩
     */
    private static boolean compressWithResize(String inputPath, String outputPath, long targetSizeBytes) {
        try {
            double[] scales = {0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1};
            
            for (double scale : scales) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                
                Thumbnails.of(inputPath)
                        .scale(scale)
                        .outputQuality(0.8)
                        .toOutputStream(baos);
                
                if (baos.size() <= targetSizeBytes) {
                    Files.write(Paths.get(outputPath), baos.toByteArray());
                    System.out.println("尺寸压缩成功，缩放: " + scale + ", 大小: " + baos.size() + " 字节");
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("尺寸压缩失败: " + e.getMessage());
        }
        return false;
    }

    /**
     * 按指定宽高压缩图片
     */
    public static boolean compressImageBySize(String inputPath, String outputPath, 
                                            int width, int height, float quality) {
        try {
            Thumbnails.of(inputPath)
                    .size(width, height)
                    .outputQuality(quality)
                    .toFile(outputPath);
            
            System.out.println("按尺寸压缩成功: " + width + "x" + height);
            return true;
        } catch (Exception e) {
            System.err.println("按尺寸压缩失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 压缩图片，同时限制分辨率和文件大小
     * @param inputPath 输入图片路径
     * @param outputPath 输出图片路径
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @param targetSizeKB 目标大小（KB）
     * @return 是否压缩成功
     */
    public static boolean compressImageWithResolution(String inputPath, String outputPath, 
                                                    int maxWidth, int maxHeight, int targetSizeKB) {
        try {
            if (!Files.exists(Paths.get(inputPath))) {
                System.err.println("输入文件不存在: " + inputPath);
                return false;
            }

            long targetSizeBytes = targetSizeKB * 1024L;
            
            // 首先限制分辨率，然后压缩文件大小
            return compressWithResolutionAndSize(inputPath, outputPath, maxWidth, maxHeight, targetSizeBytes);

        } catch (Exception e) {
            System.err.println("压缩图片时发生错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 同时进行分辨率限制和大小压缩
     */
    private static boolean compressWithResolutionAndSize(String inputPath, String outputPath, 
                                                       int maxWidth, int maxHeight, long targetSizeBytes) {
        try {
            // 质量级别数组
            float[] qualities = {0.9f, 0.8f, 0.7f, 0.6f, 0.5f, 0.4f, 0.3f, 0.2f, 0.1f};
            
            // 首先尝试限制分辨率 + 不同质量
            for (float quality : qualities) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                
                Thumbnails.of(inputPath)
                        .size(maxWidth, maxHeight)
                        .outputQuality(quality)
                        .toOutputStream(baos);
                
                if (baos.size() <= targetSizeBytes) {
                    Files.write(Paths.get(outputPath), baos.toByteArray());
                    System.out.println("分辨率+质量压缩成功，分辨率: " + maxWidth + "x" + maxHeight + 
                                     ", 质量: " + quality + ", 大小: " + baos.size() + " 字节");
                    return true;
                }
            }
            
            // 如果还是太大，进一步缩小分辨率
            double[] scales = {0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1};
            
            for (double scale : scales) {
                int scaledWidth = (int) (maxWidth * scale);
                int scaledHeight = (int) (maxHeight * scale);
                
                for (float quality : qualities) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    
                    Thumbnails.of(inputPath)
                            .size(scaledWidth, scaledHeight)
                            .outputQuality(quality)
                            .toOutputStream(baos);
                    
                    if (baos.size() <= targetSizeBytes) {
                        Files.write(Paths.get(outputPath), baos.toByteArray());
                        System.out.println("分辨率缩放+质量压缩成功，分辨率: " + scaledWidth + "x" + scaledHeight + 
                                         ", 质量: " + quality + ", 大小: " + baos.size() + " 字节");
                        return true;
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("分辨率+大小压缩失败: " + e.getMessage());
        }
        return false;
    }

    /**
     * 压缩 MultipartFile 到指定分辨率和文件大小
     * @param file 上传的文件
     * @param maxWidth 最大宽度（像素）
     * @param maxHeight 最大高度（像素）
     * @param targetSizeKB 目标大小（KB）
     * @return 压缩后的字节数组
     * @throws IOException 压缩失败时抛出异常
     */
    public static byte[] compressMultipartFile(MultipartFile file, int maxWidth, int maxHeight, int targetSizeKB)
            throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        long targetSizeBytes = targetSizeKB * 1024L;
        byte[] originalBytes = file.getBytes();

        // 如果原文件已满足要求，直接返回
        if (originalBytes.length <= targetSizeBytes) {
            return originalBytes;
        }

        // 质量级别数组
        float[] qualities = {0.9f, 0.8f, 0.7f, 0.6f, 0.5f, 0.4f, 0.3f, 0.2f, 0.1f};

        try (ByteArrayInputStream bais = new ByteArrayInputStream(originalBytes)) {
            // 首先尝试限制分辨率 + 不同质量
            for (float quality : qualities) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                Thumbnails.of(bais)
                        .size(maxWidth, maxHeight)
                        .outputQuality(quality)
                        .toOutputStream(baos);

                if (baos.size() <= targetSizeBytes) {
                    System.out.println("分辨率+质量压缩成功，分辨率: " + maxWidth + "x" + maxHeight +
                                     ", 质量: " + quality + ", 大小: " + baos.size() + " 字节");
                    return baos.toByteArray();
                }

                // 重置输入流
                bais.reset();
            }

            // 如果还是太大，进一步缩小分辨率
            double[] scales = {0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1};

            for (double scale : scales) {
                int scaledWidth = (int) (maxWidth * scale);
                int scaledHeight = (int) (maxHeight * scale);

                for (float quality : qualities) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    Thumbnails.of(bais)
                            .size(scaledWidth, scaledHeight)
                            .outputQuality(quality)
                            .toOutputStream(baos);

                    if (baos.size() <= targetSizeBytes) {
                        System.out.println("分辨率缩放+质量压缩成功，分辨率: " + scaledWidth + "x" + scaledHeight +
                                         ", 质量: " + quality + ", 大小: " + baos.size() + " 字节");
                        return baos.toByteArray();
                    }

                    // 重置输入流
                    bais.reset();
                }
            }
        }

        // 如果所有压缩方法都失败，返回最小的压缩结果
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ByteArrayInputStream bais = new ByteArrayInputStream(originalBytes)) {
            Thumbnails.of(bais)
                    .size(100, 100)
                    .outputQuality(0.1)
                    .toOutputStream(baos);
        }

        System.out.println("使用最小压缩设置，大小: " + baos.size() + " 字节");
        return baos.toByteArray();
    }

    /**
     * 获取文件大小（KB）
     */
    public static long getFileSizeKB(String filePath) {
        try {
            return Files.size(Paths.get(filePath)) / 1024;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取 MultipartFile 大小（KB）
     */
    public static long getFileSizeKB(MultipartFile file) {
        return file.getSize() / 1024;
    }

}