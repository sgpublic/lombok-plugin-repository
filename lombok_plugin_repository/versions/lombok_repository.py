from xml.etree import ElementTree
from xml.etree.ElementTree import Element

from lombok_plugin_repository.config import ConfigRepository
from lombok_plugin_repository.versions import Idea_IdeaRelease


def create_repository_xml(versions: list[str], idea_infos: dict[str, Idea_IdeaRelease], target: ConfigRepository) -> Element:
    root = ElementTree.Element("plugin-repository")
    ff = ElementTree.SubElement(root, "ff")
    ff.text = "Tools Integration"
    category = ElementTree.SubElement(root, "category")
    category.set("name", "Tools Integration")
    for version in sorted(versions, reverse=True):
        idea_info: Idea_IdeaRelease = idea_infos[version]

        idea_plugin: Element = ElementTree.SubElement(category, "idea-plugin")
        idea_plugin.set("download", "0")
        idea_plugin.set("size", str(idea_info.downloads.windowsZip.size))
        idea_plugin.set("date", str(idea_info.date))
        idea_plugin.set("url", target.repo_url)
        ElementTree.SubElement(idea_plugin, "name").text = "Lombok"
        ElementTree.SubElement(idea_plugin, "id").text = "Lombook Plugin"
        ElementTree.SubElement(idea_plugin, "description").text = "Lombook Plugin"
        ElementTree.SubElement(idea_plugin, "version").text = version
        ElementTree.SubElement(idea_plugin, "rating").text = "0.0"
        ElementTree.SubElement(idea_plugin, "download-url").text = target.item_download_url(version)
        idea_version: Element = ElementTree.SubElement(category, "idea-version")
        idea_version.set("min", "n/a")
        idea_version.set("max", "n/a")
        idea_version.set("since-build", version)
        idea_version.set("until-build", version)

    return root
