# lombok-plugin

这是一个为解决 Lombok 插件和 Android Studio 不兼容问题的仓库。

**注意：如果你的 Android Studio 版本为 4.2.2（202.\*）及以下，则你可以直接使用插件商城中下载的版本，而无需使用本仓库中的内容。**

## 如何使用

### Plugin repository（推荐）

1. 前往 `File` -> `Settings` -> `Plugins`，点击 `Install` 右边的设置齿轮图标。
2. 选择 `Manage Plugin repositories...`.
3. 将 `https://github.com/sgpublic/lombok-plugin-action/releases/download/plugin-repository/plugin-repository` 添加到列表.
4. 在 `Marketplace` 中搜索 `Lombok` 并直接安装即可，enjoy!

### 手动安装

1. 前往本仓库的 [release 页](https://github.com/sgpublic/lombok-plugin-action/releases) 寻找适合你所使用的 Android Studio 的版本，下载资源中的 `lombok-xxx.xxxx.xx.zip`。
2. 前往 `File` -> `Settings` -> `Plugins`，点击 `Install` 右边的设置齿轮图标。
3. 选择 `Install Plugin from Disk...`.
4. 选择下载的 `zip` 文件，并点击 `OK`。
5. Lombok 安装成功，enjoy！