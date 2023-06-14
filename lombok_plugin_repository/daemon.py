import argparse
import os.path
import signal
import sys
from datetime import timedelta
from typing import Any

from apscheduler.schedulers.blocking import BlockingScheduler
from loguru import logger

from lombok_plugin_repository.action import Action
from lombok_plugin_repository.config.setting import load_setting


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
        logger.add(sys.stdout,
                   format="<green>{time:YYYY-MM-DD HH:mm:ss.SSS}</green> "
                          "<level>{level: <8}</level> <cyan>\"{extra[file_path]}:{line}\"</cyan>: "
                          "<level>{message}</level>",
                   level="DEBUG" if arg.debug else "INFO")

        global_config = load_setting(arg.config)

        if not os.path.exists(arg.config):
            open(arg.config, "w+").close()
        if not arg.debug:
            logger.add(os.path.join(global_config.logging.path, "lombok-plugin-repository.log"),
                       format="{time} | {level: <8} | {file}:{line} - {message}",
                       rotation=timedelta(seconds=global_config.logging.aging),
                       level=global_config.logging.level)

        logger.debug(f"使用配置：\n{global_config}")

        if len(global_config.repos) == 0:
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
