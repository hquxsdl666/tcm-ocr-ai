# 中药智能识别与AI方剂系统

[![Build APK](https://github.com/hquxsdl666/tcm-ocr-ai/actions/workflows/build.yml/badge.svg)](https://github.com/hquxsdl666/tcm-ocr-ai/actions/workflows/build.yml)

基于Android的中药药方OCR识别与AI方剂管理应用。

## 📱 功能特性

- 📷 **OCR智能识别**：拍照识别手写/打印中药药方
- 📚 **方剂管理**：本地数据库存储，支持CRUD操作
- 🔍 **智能搜索**：多维度搜索（方剂名、药材、症状）
- 🤖 **AI助手**：集成DeepSeek API，中医知识问答
- 💊 **AI开方**：基于个人方剂库的智能推荐

## 🚀 快速开始

### 方式一：直接下载APK（推荐）

1. 点击本页面右侧的 **Releases** 
2. 下载最新版本的 `app-debug.apk` 或 `app-release-unsigned.apk`
3. 安装到Android手机（需要Android 7.0+）

或者点击上方徽章 [![Build APK](https://github.com/hquxsdl666/tcm-ocr-ai/actions/workflows/build.yml/badge.svg)]，进入Actions页面下载最新构建的APK。

### 方式二：自行构建

```bash
# 克隆仓库
git clone https://github.com/hquxsdl666/tcm-ocr-ai.git

# 打开 Android Studio
# File -> Open -> 选择项目文件夹

# 点击 Run 按钮构建并安装
```

## 🔑 首次使用配置

1. **获取DeepSeek API Key**
   - 访问 https://platform.deepseek.com/
   - 注册账号并创建API Key
   - 新用户有10元免费额度

2. **设置API Key**
   - 打开APP，进入"AI助手"页面
   - 点击右上角 🔑 钥匙图标
   - 粘贴API Key并保存

3. **开始使用**
   - 拍照识别药方
   - 搜索方剂
   - AI开方咨询

## 🏗️ 技术架构

- **语言**：Kotlin
- **UI框架**：Jetpack Compose + Material 3
- **架构**：MVVM + Repository模式
- **数据库**：Room (SQLite)
- **网络**：Retrofit + OkHttp
- **AI服务**：DeepSeek API

## 📋 项目结构

```
app/src/main/java/com/tcm/app/
├── data/
│   ├── local/          # Room数据库
│   ├── remote/         # API接口
│   └── repository/     # 数据仓库
├── ui/
│   ├── screens/        # 页面
│   ├── components/     # 组件
│   ├── theme/          # 主题
│   └── viewmodel/      # 视图模型
└── utils/              # 工具类
```

## 📝 版本历史

### v1.0.0 (2024-02-18)
- ✨ 初始版本发布
- ✨ OCR药方识别功能
- ✨ 方剂数据库管理
- ✨ AI助手对话
- ✨ AI智能开方

## 📄 许可证

MIT License

## ⚠️ 免责声明

本应用仅供参考学习使用，AI建议不构成医疗诊断或治疗建议。具体用药请咨询专业中医师。

---

**维护者**：YOUR_NAME  
**联系方式**：your.email@example.com
