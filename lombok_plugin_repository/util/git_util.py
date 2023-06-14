from __future__ import annotations

import os.path
import shutil

import git
from git import Repo
from loguru import logger

from lombok_plugin_repository import config
from lombok_plugin_repository.config.setting import ConfigRepository


class GitRepo:
    @staticmethod
    def checkout_git_repo(name: str, conf: ConfigRepository) -> Repo | None:
        logger.info(f"克隆仓库 {conf.git_url} 的 {conf.branch} 分支...")
        path: str = os.path.join(config.global_config.temp_dir, "git", name)
        try:
            os.makedirs(path, exist_ok=True)
            repo: Repo = git.Repo(path)
            if repo.remote("origin").repo != conf.git_url:
                raise Exception("目标仓库不匹配")
            repo.git.fetch("origin")
            repo.git.checkout(conf.branch)
            repo.git.reset("HEAD", "hard")
        except Exception:
            try:
                shutil.rmtree(path)
                repo: Repo = git.Repo.clone_from(conf.git_url, path, branch=conf.branch)
            except Exception as e:
                logger.warning(f"仓库克隆失败：{name}（{conf.git_url}）\n{e}")
                return None

        return repo
