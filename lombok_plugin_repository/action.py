from datetime import datetimefrom typing import Optionalfrom croniter import croniterfrom git import Repofrom loguru import loggerfrom lombok_plugin_repository import configfrom lombok_plugin_repository.util.git_util import GitRepoclass Action:    _cron: croniter = None    _next: datetime = None    @staticmethod    def init() -> None:        logger.info("初始化中...")        for name, conf in config.global_config.repos.items():            GitRepo.checkout_git_repo(name, conf.git_url, conf.branch)            GitRepo.checkout_wiki_repo(name, conf.wiki_git)        Action._cron = croniter(config.global_config.cron)        Action._next = datetime.fromtimestamp(Action._cron.get_next())        logger.info("初始化完成")    @staticmethod    def schedule() -> None:        if Action._next is None or datetime.now() < Action._next:            return        Action._next = Action._cron.get_next()        Action.single()    @staticmethod    def single() -> None:        logger.info("开始更新 Lombok Plugin")        from lombok_plugin_repository.versions.target_versions import list_as_versions        as_vers = list_as_versions()        from lombok_plugin_repository.versions.target_versions import list_idea_versions        idea_vers = list_idea_versions()        if as_vers is None or idea_vers is None:            return        from lombok_plugin_repository.versions.target_versions import compare_versions        logger.info(f"此次检索共 {len(as_vers)} 个版本")        for ver_name, as_info in as_vers.items():            logger.info(f"检索 Android Studio 版本信息：{ver_name}")            idea_target: Optional[dict]            try:                idea_target = idea_vers[ver_name]            except Exception:                idea_target = None            if idea_target is not None:                logger.info(f"与 {ver_name} 对应的 IntelliJ IDEA Ultimate 版本存在")                Action.checkout_existing_version(ver_name, as_info, idea_target)                continue            logger.warning(f"与 {ver_name} 对应的 IntelliJ IDEA Ultimate 版本不存在，寻找代替版本...")            idea_ver_target: Optional[str] = None            for idea_ver_name, idea_info in idea_vers.items():                compare: Optional[int] = compare_versions(idea_ver_name, ver_name)                if compare is None:                    del idea_vers[idea_ver_name]                    continue                if compare < 0:                    continue                idea_ver_target = idea_ver_name                logger.info(f"找到 {ver_name} 代替版本：{idea_ver_name}")                Action.checkout_wrapped_version(ver_name, as_info, idea_ver_target, idea_info)                break            if idea_ver_target is None:                logger.error(f"寻找代替版本出错：{ver_name}")    @staticmethod    def checkout_existing_version(ver_name: str, as_info: dict, idea_info: dict) -> None:        for name, conf in config.global_config.repos.items():            repo: Repo = GitRepo.checkout_git_repo(name, conf.git_url, conf.branch)            Action.checkout_existing_single_version(ver_name, as_info, idea_info, repo)    @staticmethod    def checkout_wrapped_version(ver_name: str, as_info: dict, wrapped_ver_name: str, idea_info: dict) -> None:        for name, conf in config.global_config.repos.items():            repo: Repo = GitRepo.checkout_git_repo(name, conf.git_url, conf.branch)            Action.checkout_wrapped_single_version(ver_name, as_info, idea_info, wrapped_ver_name, repo)    @staticmethod    def checkout_existing_single_version(ver_name: str, as_info: dict, idea_info: dict, repo: Repo) -> None:        pass    @staticmethod    def checkout_wrapped_single_version(ver_name: str, as_info: dict, idea_info: dict, wrapped_ver_name: str, repo: Repo) -> None:        pass