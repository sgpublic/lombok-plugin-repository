import os.path

if __name__ == '__main__':
    config_file = os.path.abspath("./config.yaml")
    print(f"使用文件：{config_file}")
    opened = open(config_file, "r+")
    opened.seek(0)
    print(opened.read())
