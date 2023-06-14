import attr


@attr.s
class _ConfigLogging:
    path: str = attr.ib(default="/var/log/lombok")
    level: str = attr.ib(default="INFO")
    aging: int = attr.ib(default=604800)


_git_url_default: str = "https://username@auth_token:github.com/user/example.git"


@attr.s
class ConfigRepository:
    repo_url: str = attr.ib(default="https://github.com/user/example")
    git_url: str = attr.ib(default=_git_url_default)
    branch: str = attr.ib(default="repository")

    RELEASE_REPOSITORY: str = attr.ib(default="https://raw.githubusercontent.com/user/example/%BRANCH%/release")
    FULL_REPOSITORY: str = attr.ib(default="https://raw.githubusercontent.com/user/example/%BRANCH%/full")
    ITEM_DOWNLOAD_URL: str = attr.ib(default="https://raw.githubusercontent.com/user/example/%BRANCH%/plugins/%LOMBOK_VERSION%/%FILE_NAME%")
    BRANCH_URL: str = attr.ib(default="https://github.com/user/example/tree/%BRANCH%")

    def release_repository(self):
        return self.RELEASE_REPOSITORY.replace("%BRANCH%", self.branch)

    def full_repository(self):
        return self.FULL_REPOSITORY.replace("%BRANCH%", self.branch)

    def item_download_url(self, lombok_ver: str):
        return self.ITEM_DOWNLOAD_URL.replace("%BRANCH%", self.branch)\
            .replace("%LOMBOK_VERSION%", lombok_ver)\
            .replace("%FILE_NAME%", f"lombok-{lombok_ver}.tar.gz")

    def branch_url(self):
        return self.BRANCH_URL.replace("%BRANCH%", self.branch)


@attr.s
class Config:
    temp_dir: str = attr.ib(default="/tmp/lombok-plugin")
    cron: str = attr.ib(default="0 0 2 * * *")
    logging: _ConfigLogging = attr.ib(factory=_ConfigLogging)
    repos: dict[str, ConfigRepository] = attr.ib(factory=dict[str, ConfigRepository])

    # noinspection PyArgumentList
    def __attrs_post_init__(self):
        try:
            self.logging = _ConfigLogging(**self.logging)
        except Exception:
            self.logging = _ConfigLogging()
        try:
            self.repos = {
                key: ConfigRepository(**value)
                for key, value in self.repos.items()
            }
        except Exception:
            self.repos = {}


global_config: Config
