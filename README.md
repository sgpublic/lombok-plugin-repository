# lombok-plugin-repository

示例配置文件：

```yaml
debug: false
cron: "0 0 2 * * ?"
temp-dir: "/tmp/lombok-plugin-repository"
log-dir: "/var/log/lombok-plugin-repository"
version-rss:
  android-studio: "https://jb.gg/android-studio-releases-list.json"
  idea-ultimate: "https://data.services.jetbrains.com/products?code=IU&fields=releases"
  lombok-official: "https://plugins.jetbrains.com/api/plugins/6317/updates"
download-retry: 3
repos:
  github:
    release-repository: "https://raw.githubusercontent.com/sgpublic/lombok-plugin-repository/%BRANCH%/release"
    full-repository: "https://raw.githubusercontent.com/sgpublic/lombok-plugin-repository/%BRANCH%/full"
    item-download-url: "https://raw.githubusercontent.com/sgpublic/lombok-plugin-repository/%BRANCH%/plugins/%ANDROID_STUDIO_BUILD%/%FILE_NAME%"
    repo-url: "https://github.com/sgpublic/lombok-plugin-repository"
    wiki-url: "https://github.com/sgpublic/lombok-plugin-repository/wiki"
    plugin-repo:
      branch: "repository"
      git-url: "https://github.com/sgpublic/lombok-plugin-repository.git"
      auth:
        username: "xxx"
        token: "xxx"
    wiki-repo:
      git-url: "https://github.com/sgpublic/lombok-plugin-repository.wiki.git"
      auth:
        username: "xxx"
        token: "xxx"
```