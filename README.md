# 校园失物招领 App (Android 客户端)

## 📖 项目简介

本项目是一个专为高校师生打造的**校园失物招领与综合服务 Android 客户端**。通过该应用，用户可以快速发布寻物启事与招领信息，浏览校园动态，并通过内置的即时通讯（IM）功能直接与相关同学取得联系，极大提高校园内物品找回的效率。该客户端与后端的 Spring Boot 服务配合，提供了稳定、流畅的移动端体验。
修改 \campus_app_android\app\src\main\assets 目录下面的url.properties文件 ip地址为服务器的地址

## ✨ 核心功能

* **失物与招领大厅 (Lost & Found)**：双信息流展示，支持图文列表、详情查看，信息一目了然。
* **信息发布**：提供便捷的表单，支持上传图片、选择物品分类、填写时间地点等关键信息。
* **即时通讯 (IM) 互动**：内置聊天会话模块，支持用户间私信沟通、评论留言与消息互动，方便归还物品时取得联系。
* **高效搜索**：支持关键字检索，快速定位所需找回的物品信息。
* **个人中心与用户管理**：涵盖注册、登录、密码重置、个人资料管理、我的发布及评论记录查看。
* **个性化设置**：支持多语言切换（Multi-Language）、全局字体大小调节，以及应用版本自动更新。

## 🛠️ 技术栈与开源库

本项目采用 Android 主流开发架构，注重组件化与代码规范，并深度集成了以下优秀的开源组件：

* **UI 框架**：基于 [XUI](https://github.com/xuexiangjys/XUI) 构建了统一、美观的界面体系。
* **页面路由**：使用 [XPage](https://github.com/xuexiangjys/XPage) 实现了灵活的 Fragment 页面路由与跳转。
* **网络请求**：Retrofit2 + RxJava，结合自定义的全局 `ApiService` 和加载状态订阅（ProgressLoader）。
* **Web 容器**：采用 AgentWeb 提供流畅的内置网页浏览体验（服务协议、隐私政策等）。
* **版本更新**：接入 XUpdate 实现了完善的 App 内更新检测与安装机制。
* **数据缓存**：基于 MMKV 提供高性能的本地键值缓存。
* **性能监控**：集成了友盟统计 (UMeng) 和 ANRWatchDog，用于线上异常捕获与体验优化。

## 📁 核心目录结构

```text
app/src/main/java/com/hx/campus/
├── activity/      # 全局核心 Activity（如 MainActivity, LoginActivity, SplashActivity）
├── fragment/      # 业务页面 Fragment
│   ├── look/      # 失物招领大厅（包含寻物、拾物列表及详情）
│   ├── message/   # 消息互动、聊天列表及我的评论
│   ├── navigation/# 底部导航主页面及物品发布模块
│   ├── personal/  # 个人中心（账号管理、相册照片、反馈建议）
│   ├── settings/  # 设置模块（通用设置、多语言配置等）
│   └── other/     # 搜索、登录注册、协议展示等通用页面
├── adapter/       # RecyclerView 适配器集合（采用委托代理模式，支持复杂多类型列表）
├── core/          # 核心基础类封装（BaseActivity/Fragment, 统一网络请求回调, 进度条加载器, Web配置）
└── utils/         # 通用工具类与 SDK 初始化配置
    ├── api/       # 网络接口封装与 RetrofitClient
    ├── sdkinit/   # 第三方 SDK 统一初始化入口（XUI, XUpdate, UMeng等）
    └── common/    # 缓存清理、语言工具、Token管理等常规工具