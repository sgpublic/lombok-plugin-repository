import argparse
import os.path
import signal
import sys
from datetime import timedelta
from typing import Any

from apscheduler.schedulers.blocking import BlockingScheduler
from loguru import logger

from lombok_plugin_repository.action import Action
from lombok_plugin_repository.setting import load_setting, ConfigObject
from lombok_plugin_repository.util.loguru_extra import log_file_path


class Main:
    scheduler: BlockingScheduler = BlockingScheduler()

    @staticmethod
    def main() -> None:
        parser: argparse.ArgumentParser = argparse.ArgumentParser(
            prog="lombok-plugin-repository",
            description="A repository for Lombok plugin incompatibility issues with Android Studio.",
        )
        parser.add_argument("-n", "--now", action="store_true",
                            help="立即运行单次同步")
        parser.add_argument("-d", "--debug", action="store_true",
                            help="开启 debug 日志")
        parser.add_argument("-c", "--config",
                            required=False, type=str, default="./config.yaml",
                            help="指定配置文件")
        arg: argparse.Namespace = parser.parse_args()

        logger.remove()
        logger.bind(file_path=log_file_path).add(sys.stdout,
                   format="<green>{time:YYYY-MM-DD HH:mm:ss.SSS}</green> "
                          "<level>{level: <8}</level> <cyan>\"{extra[file_path]}:{line}\"</cyan>: "
                          "<level>{message}</level>",
                   level="DEBUG" if arg.debug else "INFO")

        ConfigObject.config = load_setting(arg.config)

        if not os.path.exists(arg.config):
            open(arg.config, "w+").close()
        if not arg.debug:
            logger.add(os.path.join(ConfigObject.config.logging.path, "lombok-plugin-repository.log"),
                       format="{time} | {level: <8} | {file}:{line} - {message}",
                       rotation=timedelta(seconds=ConfigObject.config.logging.aging),
                       level=ConfigObject.config.logging.level)

        logger.debug(f"使用配置：\n{ConfigObject.config}")

        if len(ConfigObject.config.repos) == 0:
            logger.error("请至少为 repo 添加一个仓库！")
            exit(0)

        Action.init()

        if arg.now:
            Action.single()
        else:
            Main.scheduler.add_job(Action.single, trigger="cron", minute=1)

            signal.signal(signal.SIGINT, Main.stop)
            signal.signal(signal.SIGTERM, Main.stop)

            Main.scheduler.start()

    @staticmethod
    def stop(signum: int, frame: Any) -> None:
        logger.info("服务退出")
        if Main.scheduler.running:
            Main.scheduler.shutdown()


if __name__ == '__main__':
    Main.main()
