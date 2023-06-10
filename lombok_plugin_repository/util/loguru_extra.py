import inspect
import os


# 自定义占位符函数
def log_file_path(record):
    frame = inspect.currentframe().f_back
    file_path = os.path.abspath(frame.f_code.co_filename)
    return file_path
