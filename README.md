# 🐵 猴哥星球 — Android

> 考研/考证一站式学习平台：题库刷题 + 资料分享

## 功能

### 📚 题库刷题（核心功能）
- 多题库支持，按分类树筛选
- **答题模式** 与 **背题模式** 自由切换
- 题型：单选题、多选题、判断题、填空题、问答题
- 作答即反馈：对错标色 + 正确答案 + 题目解析
- **收藏功能**：收藏优质题目，集中练习
- **错题本**：自动收录错题，专项巩固
- **答题卡**：实时统计已答/正确/错误数量
- **学习记录**：查看历史练习的正确率与用时

### 📄 资料分享
- 按科目 + 分类双级筛选
- 分页资源列表，滚动自动加载
- 资源详情：百度网盘/夸克网盘链接与提取码

### 🏠 首页
- Banner 轮播图，点击跳转详情
- 公告栏上下滚动
- 文章推荐列表
- 网盘资源展示
- 搜索入口

### 👤 个人中心
- 手机号登录/注册
- 编辑资料（头像、昵称、性别）
- 学习记录统计
- 修改密码、设置、关于我们

## 技术栈

| 层级 | 技术 | 用途 |
|------|------|------|
| 语言 | **Kotlin** 100% | — |
| UI | **XML + ViewBinding** | 布局与视图绑定 |
| 网络 | **Retrofit 2.11** + **OkHttp 4.12** | REST API 请求 |
| 序列化 | **Gson 2.11** | JSON 解析 |
| 图片 | **Coil 2.7** | 图片加载（50MB 磁盘缓存） |
| 安全 | **BouncyCastle** | AES/CBC/PKCS7Padding 加解密 |
| 存储 | **DataStore Preferences** | Token/用户信息持久化 |
| 组件 | **ViewPager2, Banner (youth5201314), BRVAH** | 页面切换、轮播、列表适配 |

### 网络架构

三个独立 Retrofit 实例，按 API 路径拆分：

| 实例 | 拦截器链 |
|------|----------|
| `mp/` 接口 | 签名 → Token → 加密 → 日志 |
| `api/article/` 接口 | Token → AES 解密 → 版本头 → 日志 |
| `api/material/` 接口 | Token → AES 解密 → 版本头 → 日志 |

## 项目结构

```
app/src/main/java/top/monkeysxu/planet/
├── PlanetApp.kt              # Application
├── MainActivity.kt           # 入口（4 Tab 容器）
│
├── core/                     # 核心基础设施
│   ├── base/                 # BaseActivity, BaseFragment, Refreshable
│   ├── model/                # ApiResponse, Resource 封装
│   ├── network/              # RetrofitClient, 拦截器链
│   ├── storage/              # TokenManager 等持久化
│   └── config/               # 公共配置
│
├── feature_home/             # 首页
├── feature_material/         # 资料
├── feature_exam/             # 题库（核心）
├── feature_profile/          # 个人中心
│
├── feature_common/           # 公共页面
│   ├── article/              # 文章列表/详情
│   ├── material/             # 资料列表/详情
│   ├── search/               # 搜索
│   └── webview/              # 通用 WebView
│
└── widget/                   # 自定义控件
```

每个 Feature 模块内按职责分层：`api/` → `model/` → `adapter/` → Fragment/ViewModel/Repository。

## 快速开始

### 前置要求

- Android Studio Hedgehog 或更高版本
- JDK 11+
- Android SDK 36+
- Gradle 8.13+

### 1️⃣ 克隆项目

```bash
git clone <仓库地址>
cd MonkeysPlanetAndroid
```

### 2️⃣ 配置密钥

项目已将敏感配置从源代码中剥离，需自行配置：

```bash
cp secrets.properties.example secrets.properties
```

编辑 `secrets.properties` 填入真实值：

```properties
# AES 加密密钥（16位字符串）
AES_KEY=YOUR_AES_KEY_HERE
# AES 初始化向量（16位字符串）
AES_IV=YOUR_AES_IV_HERE
# 接口签名密钥
SIGN_KEY=YOUR_SIGN_KEY_HERE
# API 基础地址
BASE_URL=https://your-api-domain.com
# 接口版本号（按需修改）
ARTICLE_API_VERSION=10
MATERIAL_API_VERSION=8
```

> `secrets.properties` 已在 `.gitignore` 中排除，不会提交到仓库。

### 3️⃣ 编译运行

使用 Android Studio 打开项目根目录，Sync Gradle 后直接运行；或命令行：

```bash
./gradlew assembleDebug
```

## 隐私与安全

- 接口响应体使用 **AES/CBC/PKCS7Padding** 加密传输
- 请求带 **MD5 签名**（密钥 + 时间戳 + 路径）
- **Bearer Token** 认证
- 加密密钥、签名密钥全部通过 `secrets.properties` 外部配置，**不硬编码在源码中**

## License

```
Copyright (c) 2026 MonkeyXu

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
