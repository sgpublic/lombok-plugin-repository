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
class ConfigRepository:
    git_url: str = attr.ib(default=_repo_git_url_default)
    branch: str = attr.ib(default="repository")


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


class ConfigObject:
    config: Config = None


def _yaml_filter(attribute, _) -> bool:
    return attribute not in ["_repo_git_url_default"]


def load_setting(config_path: str) -> Config:
    config_path: str = os.path.abspath(config_path)
    logger.info(f"使用配置文件：{config_path}")
    with open(config_path, "r+") as config_file:
        config_content: str = config_file.read()
        logger.debug(f"配置文件内容：\n{config_content}")
    obj: dict = yaml.safe_load(config_content)
    try:
        config: Config = Config(**obj)
    except Exception:
        if config_content != "":
            logger.warning("配置文件不合法，将备份已有配置文件并重新创建")
            with open(f"{config_path}.bak", "w+") as backup_file:
                backup_file.write(config_content)
        else:
            logger.warning("配置文件不存在或为空，将自动创建")
        config: Config = Config()
    with open(config_path, "w+") as config_file:
        if len(config.repos) == 0:
            config.repos["example"] = ConfigRepository()
        yaml.dump(
            attr.asdict(config, filter=_yaml_filter),
            config_file,
            default_flow_style=False
        )
    config.repos = {
        key: value
        for key, value in config.repos.items() if value.git_url != _repo_git_url_default
    }
    return config
