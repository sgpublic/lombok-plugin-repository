import requests as requests
from loguru import logger
from sortedcontainers import SortedDict

from lombok_plugin_repository.versions import iu_version_collection, as_version_collection, _AS_Products, \
    _Idea_Products, AS_AndroidStudioRelease


def list_idea_versions() -> SortedDict[str, _Idea_Products] | None:
    logger.info(f"Getting JetBrains IntelliJ IDEA versions from {iu_version_collection}")
    release = requests.get(iu_version_collection).json(
        object_hook=lambda d: _Idea_Products
    )[0].releases
    if len(release) <= 0:
        logger.error("empty result of IntelliJ IDEA Ultimate versions")
        return None

    idea_versions = SortedDict()

    for version in release:
        idea_versions[version.Build] = version

    return idea_versions


def list_as_versions() -> SortedDict[str, _AS_Products] | None:
    logger.info(f"Getting Google Android Studio versions from {as_version_collection}")
    release: list[AS_AndroidStudioRelease] = requests.get(as_version_collection)\
        .json(object_hook=lambda d: _AS_Products).content.items
    if len(release) <= 0:
        logger.error("empty result of Android Studio versions")
        return None

    as_versions = SortedDict()

    for version_item in release:
        if len(version_item.version.split(".")[0]) < 4:
            continue
        build = int(version_item.platformBuild.split(".")[0])
        if build <= 202:
            continue
        value = as_versions.get(version_item.platformBuild, [])
        value.append(version_item)
        as_versions[version_item.platformBuild] = value

    return as_versions
