# Contract AI

统一的 AI 模型编排服务，提供一致的调用接口支持多个 AI 服务商。

## 开源协议

本项目采用 [Apache License 2.0](LICENSE) 开源协议。

## 技术介绍

Contract AI 基于 **Java 17** 和 **Spring Boot 3.5.0** 构建，采用策略模式实现模型路由，通过模型名称自动选择对应的 AI 提供商。项目使用 Nacos 进行服务发现，Feign Client 进行服务间通信，默认配置 60 秒超时和 3 次指数退避重试，确保调用的稳定性。

## 快速开始

### 环境要求

- Java 17+
- Maven 3.8+
- Nacos 2.0+

### 配置环境变量

```bash
# Nacos服务发现配置
export NACOS_SERVER_ADDR=localhost:8848
export NACOS_USERNAME=nacos
export NACOS_PASSWORD=nacos

# GLM API配置（智谱AI）
export GLM_API_KEY=your-glm-api-key-here

# iFlow API配置（心流平台）
export IFLOW_API_KEY=your-iflow-api-key-here

# DeepSeek API配置
export DEEPSEEK_API_KEY=your-deepseek-api-key-here
```

> **重要提示**：所有API密钥必须配置为真实值才能正常使用。本项目配置文件中的默认值仅为占位符。

### 构建并运行

```bash
# 构建项目
mvn clean package

# 运行服务
java -jar ai-core/target/contract-ai-service-1.0.0.jar
```

服务启动后，通过 `POST /ai/chat` 接口调用 AI 模型。

## 贡献指南

欢迎提交 Issue 和 Pull Request！
