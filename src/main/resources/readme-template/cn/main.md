# lombok-plugin-repository

简体中文 | [English](/README.EN.md)

这是一个关于 Lombok 插件与 Android Studio 不兼容问题的存储库。

## 如何使用

### Plugin repositories（推荐）

1. 进入 `File` -> `Settings` -> `Plugins`，点击右侧 `Install` 旁边的设置齿轮图标。
2. 选择 `Manage Plugin repositories...`。
3. 请根据您所使用的版本将仓库链接添加到列表中：
    + Release：`%RELEASE_REPOSITORY%`
    + Beta 或 Canary（包含 Release）：`%FULL_REPOSITORY%`
4. 在 `Marketplace` 中搜索 `Lombok` 并安装，enjoy！

### 手动安装

1. 从当前 README.md 尾部的表格中找到您正在使用的 Android Studio 版本，然后下载 `lombok-xxxx.x.x.zip`。
2. 进入 `File` -> `Settings` -> `Plugins`，点击右侧 `Install` 旁边的设置齿轮图标。
3. 选择 `Install Plugin from Disk...`。
4. 选择下载的 `zip` 文件，然后点击 `OK`。
5. Lombok 安装成功，enjoy！

## 版本合集

此表仅列举 Release 版本，完整列表请前往 [Wiki](%WIKI_URL%)。

| 适用于 Android Studio 版本 | 源 IDEA Ultimate 版本 | 下载 |
|-----------------------|--------------------|----|
%VERSIONS%