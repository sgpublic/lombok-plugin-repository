from __future__ import annotations

import os.path
import shutil

import git
from git import Repo
from loguru import logger

from lombok_plugin_repository.setting import ConfigRepository, ConfigObject


class GitRepo:
    @staticmethod
    def checkout_git_repo(name: str, conf: ConfigRepository) -> Repo | None:
        path: str = os.path.join(ConfigObject.config.temp_dir, "git", name)
        try:
            repo: Repo = git.Repo(path)
            if repo.remote("origin").repo != conf.git_url:
                raise Exception("目标仓库不匹配")
            repo.remote("origin").pull()
        except Exception:
            try:
                shutil.rmtree(path)
                repo: Repo = git.Repo.clone_from(conf.git_url, path)
            except Exception:
                logger.warning(f"仓库克隆失败：{name}（{conf.git_url}）")
                return None

        return repo
