# lombok-plugin

这是一个关于 Lombok 插件与 Android Studio 不兼容问题的存储库。

## 如何使用

### Plugin repositories（推荐）

1. 进入 `File` -> `Settings` -> `Plugins`，点击右侧 `Install` 旁边的设置齿轮图标。
2. 选择 `Manage Plugin repositories...`。
3. 如果您使用的是 Beta 版本，请将 `https://raw.githubusercontent.com/sgpublic/lombok-plugin-repository/repository/release` 添加到列表中；如果您使用的是 Beta 或 Canary 版本，请将 `https://raw.githubusercontent.com/sgpublic/lombok-plugin-repository/repository/pre-release` 添加到列表中。
4. 在 `Marketplace` 中搜索 `Lombok` 并安装，enjoy！

### 手动安装

1. 进入此存储库的 [repository branch](https://github.com/sgpublic/lombok-plugin-repository/tree/repository)，从 README.md 中找到您正在使用的 Android Studio 版本，然后下载 `lombok-xxxx.x.x.tar.gz`。
2. 进入 `File` -> `Settings` -> `Plugins`，点击右侧 `Install` 旁边的设置齿轮图标。
3. 选择 `Install Plugin from Disk...`。
4. 选择下载的 `tar.gz` 文件，然后点击 `OK`。
5. Lombok 安装成功，enjoy！