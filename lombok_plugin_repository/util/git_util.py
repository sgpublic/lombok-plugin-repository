import os.path
import shutil
from typing import Optional

import git
from git import Repo
from loguru import logger

from lombok_plugin_repository import config


class GitRepo:
    @staticmethod
    def checkout_git_repo(name: str, git_url: str, branch: str) -> Optional[Repo]:
        logger.info(f"检出仓库 {git_url} 的 {branch} 分支...")
        path: str = os.path.join(config.global_config.temp_dir, "git", name)
        os.makedirs(path, exist_ok=True)
        try:
            repo: Repo = git.Repo(path)
            if repo.remote("origin").url != git_url:
                raise Exception("目标仓库不匹配")
            repo.git.fetch("origin")
            repo.git.checkout(branch)
            repo.git.reset("HEAD", "hard")
        except Exception as e1:
            logger.warning(f"仓库更新失败（{e1}），尝试重新克隆：{name}（{git_url}）")
            try:
                shutil.rmtree(path)
                repo: Repo = git.Repo.clone_from(git_url, path, branch=branch)
            except Exception as e2:
                logger.warning(f"仓库克隆失败：{name}（{git_url}）\n{e2}")
                return None
        return repo

    @staticmethod
    def checkout_wiki_repo(name: str, wiki_url: str) -> Optional[Repo]:
        logger.info(f"检出仓库 Wiki {wiki_url}...")
        path: str = os.path.join(config.global_config.temp_dir, "git", f"{name}.wiki")
        os.makedirs(path, exist_ok=True)
        try:
            repo: Repo = git.Repo(path)
            if repo.remote("origin").url != wiki_url:
                raise Exception("目标仓库不匹配")
            repo.git.fetch("origin")
            repo.git.reset("HEAD", "hard")
        except Exception as e1:
            logger.warning(f"仓库更新失败（{e1}），尝试重新克隆：{name}.wiki（{wiki_url}）")
            try:
                shutil.rmtree(path)
                repo: Repo = git.Repo.clone_from(wiki_url, path)
            except Exception as e2:
                logger.warning(f"仓库克隆失败：{name}（{wiki_url}）\n{e2}")
                return None
        return repo
