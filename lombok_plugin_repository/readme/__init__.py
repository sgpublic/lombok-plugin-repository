readme_cn = '''
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

1. 进入此存储库的 [repository branch](%BRANCH_URL%)，从 README.md 中找到您正在使用的 Android Studio 版本，然后下载 `lombok-xxxx.x.x.tar.gz`。
2. 进入 `File` -> `Settings` -> `Plugins`，点击右侧 `Install` 旁边的设置齿轮图标。
3. 选择 `Install Plugin from Disk...`。
4. 选择下载的 `tar.gz` 文件，然后点击 `OK`。
5. Lombok 安装成功，enjoy！

## 版本合集

此表仅列举 Release 版本。

| Lombok 插件版本 | 源 IDEA Ultimate 版本 | 适用于 Android Studio 版本 |
| --------------- | --------------------- | -------------------------- |
%VERSIONS%
'''

readme_en = '''
# lombok-plugin-repository

[简体中文](/README.md) | English

This is a repository for Lombok plugin incompatibility issues with Android Studio.

## How to use

### Plugin repository (Recommend)

1. Go to `File` -> `Settings` -> `Plugins`, click the settings gear icon to the right of `Install`.
2. Select `Manage Plugin repositories...`.
3. Please add the repository link to the list based on the version you are using:
   + Release: `%RELEASE%`
   + Beta or Canary (includes Release): `%FULL%`
4. Search `Lombok` in `Marketplace` and install, enjoy!

### Manual installation

1. Go to the [repository branch](%BRANCH%) of this repository, find the Android Studio version you are using from README.md, and download `lombok-xxxx.x.x.tar.gz`.
2. Go to `File` -> `Settings` -> `Plugins`, click the settings gear icon to the right of `Install`.
3. Select `Install Plugin from Disk...`.
4. Select the downloaded `zip` file and click `OK`.
5. Lombok is successfully installed, enjoy!

## Versions

This table only lists release versions.

| Lombok Plugin Version | From IDEA Ultimate Version | For Android Studio Version |
| -------------------- | ---------------------------- | ------------------------------------- |
%VERSIONS%
'''

full_version = '''
<details>
<summary>%AS_VERSION%</summary>
%VERSION_ITEMS%
</details>

'''

full_version_item_cn = '''
### %LOMBOK_VERSION%

源 IDEA Ultimate 版本：%IDEA_VERSION%

下载：[%LOMBOK_VERSION%](%LOMBOK_LINK%)

适用于以下版本：

%VERSION_LIST%
'''


full_version_item_en = '''
### %LOMBOK_VERSION%

From IDEA Ultimate Version：%IDEA_VERSION%

Download：[%LOMBOK_VERSION%](%LOMBOK_LINK%)

Applies to the following versions:

%VERSION_LIST%
'''