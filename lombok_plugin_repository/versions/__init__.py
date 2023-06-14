iu_version_collection = "https://data.services.jetbrains.com/products?code=IU&fields=releases"
as_version_collection = "https://jb.gg/android-studio-releases-list.json"


class Idea_IdeaRelease:
    def __init__(self):
        self.downloads: Idea_Downloads = Idea_Downloads()
        self.build: str = ""
        self.date: str = ""


class Idea_Downloads:
    def __init__(self):
        self.windowsZip: Idea_WindowsZip = Idea_WindowsZip()


class Idea_WindowsZip:
    def __init__(self):
        self.link: str = ""
        self.size: int = 0
        self.checksumLink: str = ""


class _Idea_Products:
    def __init__(self):
        self.releases: list[Idea_IdeaRelease] = []


class AS_AndroidStudioRelease:
    def __init__(self):
        self.platformVersion: str = ""
        self.platformBuild: str = ""
        self.version: str = ""
        self.name: str = ""
        self.build: str = ""
        self.channel: str = ""


class AS_Content:
    def __init__(self):
        self.items: list[AS_AndroidStudioRelease] = []


class _AS_Products:
    def __init__(self):
        self.content: AS_Content = AS_Content()
