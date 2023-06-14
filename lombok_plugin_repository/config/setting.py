import os.path

import attr
import yaml
from loguru import logger

from lombok_plugin_repository.config import Config, ConfigRepository, _git_url_default


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
        for key, value in config.repos.items() if value.git_url != _git_url_default
    }
    return config
