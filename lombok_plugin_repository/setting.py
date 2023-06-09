import os.path

import attr
import yaml
from loguru import logger


@attr.s
class _ConfigLogging:
    path: str = attr.ib(default="/var/log/lombok")
    level: str = attr.ib(default="INFO")
    aging: int = attr.ib(default=604800)


_repo_git_url_default: str = "https://username@auth_token:github.com/user/example.git"


@attr.s
class _ConfigRepository:
    git_urls: list[str] = attr.ib(default=[_repo_git_url_default])
    branch: str = attr.ib(default="repository")


@attr.s
class Config:
    temp_dir: str = attr.ib(default="/tmp/lombok-plugin")
    cron: str = attr.ib(default="0 0 2 * * *")
    logging: _ConfigLogging = attr.ib(factory=_ConfigLogging)
    repo: _ConfigRepository = attr.ib(factory=_ConfigRepository)

    # noinspection PyArgumentList
    def __attrs_post_init__(self):
        if self.logging is None:
            self.logging = _ConfigLogging()
        elif not isinstance(self.logging, _ConfigLogging):
            self.logging = _ConfigLogging(**self.logging)
        if self.repo is None:
            self.repo = _ConfigRepository()
        elif not isinstance(self.repo, _ConfigRepository):
            self.repo = _ConfigRepository(**self.repo)
        self.repo.git_urls.remove(_repo_git_url_default)


def _yaml_filter(attribute, _) -> bool:
    return attribute not in ["_repo_git_url_default"]


def load_setting(config_path: str) -> Config:
    config_path = os.path.abspath(config_path)
    logger.info(f"使用配置文件：{config_path}")
    with open(config_path, "r+") as config_file:
        config_content = config_file.read()
        logger.debug(f"配置文件内容：\n{config_content}")
    obj = yaml.safe_load(config_content)
    if obj is None:
        logger.warning("配置文件不存在，自动创建")
        config = Config()
    else:
        config = Config(**obj)
    with open(config_path, "w+") as config_file:
        if len(config.repo.git_urls) == 0:
            config.repo.git_urls.append(_repo_git_url_default)
        yaml.dump(
            attr.asdict(config, filter=_yaml_filter),
            config_file,
            default_flow_style=False
        )
    return config
