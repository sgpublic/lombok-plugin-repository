from typing import Optional

import requests as requests
from loguru import logger
from sortedcontainers import SortedDict

from lombok_plugin_repository.versions import iu_version_collection, as_version_collection


def list_idea_versions() -> Optional[SortedDict[str, dict]]:
    logger.info(f"从 {iu_version_collection} 获取 IntelliJ IDEA Ultimate 版本合集...")
    release = requests.get(iu_version_collection).json()[0]["releases"]
    if len(release) <= 0:
        logger.error("IntelliJ IDEA Ultimate 版本获取失败")
        return None

    idea_versions = SortedDict()

    for version in release:
        build = int(version["build"].split(".")[0])
        if build < 203:
            continue
        idea_versions[version["build"]] = version

    return idea_versions


def list_as_versions() -> Optional[SortedDict[str, dict]]:
    logger.info(f"从 {as_version_collection} 获取 Android Studio 版本合集...")
    release: list[dict] = requests.get(as_version_collection)\
        .json()["content"]["item"]
    if len(release) <= 0:
        logger.error("Android Studio 版本获取失败")
        return None

    as_versions = SortedDict()

    for version_item in release:
        if len(version_item["version"].split(".")[0]) < 4:
            continue
        build = int(version_item["platformBuild"].split(".")[0])
        if build < 203:
            continue
        value = as_versions.get(version_item["platformBuild"], [])
        value.append(version_item)
        as_versions[version_item["platformBuild"]] = value

    return as_versions


def compare_versions(ver1: str, ver2: str) -> Optional[int]:
    if ver1 == ver2:
        return 0
    ver1_info = ver1.split(".")
    ver2_info = ver2.split(".")

    if len(ver1_info) != 3 or len(ver2_info) != 3:
        logger.debug(f"版本对比出错：{ver1} & {ver2}")
        return None

    if int(ver1_info[0]) != int(ver2_info[0]):
        if int(ver1_info[0]) > int(ver2_info[0]):
            return 1
        else:
            return -1
    if int(ver1_info[1]) != int(ver2_info[1]):
        if int(ver1_info[1]) > int(ver2_info[1]):
            return 1
        else:
            return -1
    if int(ver1_info[2]) > int(ver2_info[2]):
        return 1
    else:
        return -1




