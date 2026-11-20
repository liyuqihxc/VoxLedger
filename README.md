# 言账 VoxLedger

自然语言语音记账 Android 应用。说一句话，自动识别金额、用途、分类、日期，确认后存入本地数据库。也支持「出差报销」标记，方便后期核对差旅花销（只记录，不管发票）。

## 功能

- 🎙️ **语音记账**：按住麦克风说话，识别成文字。可选**系统语音识别**或**本地离线识别**（Sherpa-ONNX + SenseVoice Small int8，语音不出本机）。
- 🧠 **AI 结构化解析**：调用 DeepSeek，把自然语言拆成一条或多条交易草稿（金额/类型/分类/日期/备注）。
- ✅ **先确认再入库**：解析结果可手动修改金额、分类、日期、备注，确认无误再保存。
- 📒 **本地存储**：Room 数据库，数据只存在本机。
- 💼 **出差报销**：给记录打「出差」标记并可选填写事由，单独一个页面汇总核对。
- 🔐 **API Key 加密**：DeepSeek API Key 用 Android Keystore + AES-GCM 加密保存，且不会出现在日志或备份里。

## 架构

```
app/src/main/java/com/liyuqihxc/voxledger/
├── di/              # 手动依赖注入 (AppContainer)
├── model/           # 领域模型：Transaction / TransactionDraft / Category / ParseContext
├── logic/           # 纯逻辑（可单测）：Money / ChineseNumber / RelativeDateParser
├── data/
│   ├── local/       # Room：Entity / DAO / Database
│   ├── remote/      # DeepSeek：Client / Parser / 响应映射（纯函数）
│   ├── security/    # ApiKeyStore（Keystore + AES-GCM）
│   └── repository/  # TransactionRepository / SettingsRepository
├── speech/          # 语音识别抽象 + 系统识别实现 + 本地离线识别（Sherpa-ONNX）
└── ui/              # Compose：theme / home / history / business / settings / record
```

数据流：**录音 → 识别 → DeepSeek 解析 → 草稿确认 → Room 入库**。

## 构建

环境要求：

- JDK 17
- Android SDK（已配置 `compileSdk 36`，`buildToolsVersion 36.0.0`）
- Gradle 8.14.8（项目自带 wrapper）

### 1. 配置 SDK 路径

`local.properties`（不要提交到 git）：

```properties
sdk.dir=F:/AndroidData/Sdk
```

### 2. 代理（仅无直连时）

`gradle.properties` 里已配置本机 Clash 代理（127.0.0.1:7890），用于访问 Maven Central / Google Maven：

```properties
systemProp.http.proxyHost=127.0.0.1
systemProp.http.proxyPort=7890
systemProp.https.proxyHost=127.0.0.1
systemProp.https.proxyPort=7890
```

如果你有直连网络，删掉这四行即可（或在 Android Studio 的 Settings → HTTP Proxy 中配置）。

### 3. 构建

命令行：

```bash
./gradlew assembleDebug
```

或在 Android Studio 中直接打开项目、点 Run。

### 4. 运行单元测试

```bash
./gradlew testDebugUnitTest
```

测试覆盖纯逻辑：`ChineseNumberTest`、`MoneyTest`、`RelativeDateParserTest`、`DeepSeekResponseMapperTest`。

## 使用

1. 打开「设置」，填入你的 DeepSeek API Key（模型默认 `deepseek-chat`）。
2. 回到首页，点右下角 **+**。
3. 按住麦克风说一句，或直接输入文字，例如：
   - 「今天中午吃饭花了35块，晚上买水果20」
   - 「昨天和朋友吃饭我付了128，后来打车26块5」
   - 「去北京出差，高铁票553，酒店两晚860」
4. 点「解析」，检查自动生成的多笔草稿，修改金额/分类/日期，勾选「出差报销」。
5. 点「保存」。

## 说明

- **金额精度**：内部一律用整数「分」存储，避免浮点误差。
- **分类**：由 App 内置固定分类，DeepSeek 只负责从列表里选，避免分类名不一致。
- **隐私**：记账文本会发送到 DeepSeek 云端处理；API Key 与交易数据只保存在本机。
- **离线语音识别**：内置 SenseVoice Small int8 模型（约 228MB，位于 `app/src/main/assets/sherpa-onnx/`）。在「设置 → 语音识别」切换到「本地离线」即可完全离线识别、语音不出本机；首次进入记账页需几秒加载模型。切回「系统语音」则使用 Android 系统识别。
- **依赖与体积**：离线识别依赖 `app/libs/sherpa-onnx.aar`（Sherpa-ONNX 2.0.2，含 onnxruntime 原生库）。因内置模型，debug APK 约 290MB；若不需要离线识别，可删除 `assets/sherpa-onnx/`、`app/libs/sherpa-onnx.aar` 及 `build.gradle.kts` 里对应的 `files("libs/sherpa-onnx.aar")` 来减小体积。
