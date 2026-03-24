# 校园失物招领 App (Android 客户端)

本项目是一款基于 Android 平台的校园失物招领应用程序，旨在为高校师生提供一个便捷、高效的失物寻回与信息发布平台。此项目为本科毕业设计系统中的 Android 客户端部分。

## 🌟 主要功能

- **失物/招领**：浏览最新发布的丢失物品和拾取物品信息，支持分类和位置标记。
- **信息发布**：快速发布寻物启事或招领启事，支持图文描述。
- **消息互动**：支持用户之间的互动留言与私信沟通，方便核实物品细节。
- **个人中心**：管理个人发布的信息、查看我的评论与互动记录。
- **积分商城**：内置积分系统，用户可通过发布有效信息获取积分，并在商城中兑换相关权益或物品。
- **推荐**：浏览最新的动态信息。

## 🛠️ 技术栈

本项目采用纯粹的 Android 原生开发方式，使用 Java 语言编写，集成了多项主流开源库以提升开发效率和用户体验：

- **开发语言**：Java
- **网络请求**：[Retrofit](https://github.com/square/retrofit) 结合自定义的 Http 组件进行 RESTful API 请求。
- **UI 框架**：使用了 [XUI](https://github.com/xuexiangjys/XUI) 打造统一且美观的用户界面，包含丰富的自定义控件。
- **本地存储**：集成 [MMKV](https://github.com/Tencent/MMKV) 替代传统的 SharedPreferences，实现极速的本地键值对存储。
- **Web 容器**：使用 [AgentWeb](https://github.com/Justson/AgentWeb) 轻量级集成 WebView，用于展示服务协议和富文本页面。
- **数据统计/崩溃分析**：集成了友盟 (UMeng) SDK 以及 ANRWatchDog 进行线上的性能监控与日志追踪。

## 📁 核心目录结构

```text
app/src/main/java/com/hx/campus/
├── activity/          # Activity 页面容器 (包含主页、登录注册、启动页等)
├── fragment/          # 业务 Fragment 模块
│   ├── dynamic/       # 动态模块
│   ├── look/          # 寻物/招领
│   ├── message/       # 消息与互动模块
│   ├── navigation/    # 信息发布与详情导航模块
│   ├── personal/      # 个人中心与账号管理
│   ├── settings/      # 系统设置
│   └── shop/          # 积分商城模块
├── adapter/           # 各种 RecyclerView 的数据适配器与实体类 (Entity)
├── core/              # 项目基类 (BaseActivity, BaseFragment, WebView 封装等)
├── utils/             # 工具类集合
│   ├── api/           # Retrofit 接口定义与网络服务封装
│   ├── common/        # 通用工具 (弹窗、缓存、MMKV 管理等)
│   ├── sdkinit/       # 第三方 SDK 的统一初始化管理
│   └── update/        # 自动更新模块相关实现
└── widget/            # 自定义 UI 控件 (如字体滑动条、上拉加载尾部等)