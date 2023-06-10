from crontab import CronTab

if __name__ == '__main__':
    result = CronTab(tab='0 0 2 * * *')[0]  # 设置cron表达式

    print(f'分钟：{result.minute}')
    print(f'小时：{result.hour}')
    print(f'日期：{result.day}')
    print(f'月份：{result.month}')
    print(f'星期：{result.dow}')
    print(f'命令：{result.command}')
