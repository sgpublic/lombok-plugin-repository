import argparse
import os.path
import signal
import sys
from datetime import timedelta
from typing import Any

from apscheduler.schedulers.blocking import BlockingScheduler
from crontab import CronTab
from loguru import logger

from lombok_plugin_repository.action import action
from lombok_plugin_repository.setting import load_setting


class Main:
    scheduler = BlockingScheduler()

    @staticmethod
    def main():
        parser = argparse.ArgumentParser(
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
        arg = parser.parse_args()

        logger.remove()
        logger.add(sys.stdout,
                   format="<green>{time:YYYY-MM-DD HH:mm:ss.SSS}</green> "
                          "<level>{level: <8}</level> <cyan>({file}:{line})</cyan>: "
                          "<level>{message}</level>",
                   level="DEBUG" if arg.debug else "INFO")

        if not os.path.exists(arg.config):
            open(arg.config, "w+").close()
        config = load_setting(arg.config)
        if not arg.debug:
            logger.add(os.path.join(config.logging.path, "lombok-plugin-repository.log"),
                       format="{time} | {level: <8} | {file}:{line} - {message}",
                       rotation=timedelta(seconds=config.logging.aging),
                       level=config.logging.level)

        if len(config.repo.git_urls) == 0:
            logger.error("请至少为 repo.git_urls 添加一个仓库：\n"
                         "  HTTP：https://username@auth_token:github.com/user/example.git\n"
                         "  SSH：git@github.com/user/example.git")
            exit(0)

        if arg.now:
            action()
            exit(0)

        cron = CronTab(tab=config.cron)[0]
        Main.scheduler.add_job(action, trigger="cron", minute=cron.minute)

        signal.signal(signal.SIGINT, Main.stop)
        signal.signal(signal.SIGTERM, Main.stop)

        Main.scheduler.start()

    @staticmethod
    def stop(signum: int, frame: Any):
        logger.info("服务退出")
        Main.scheduler.shutdown()


if __name__ == '__main__':
    Main.main()
