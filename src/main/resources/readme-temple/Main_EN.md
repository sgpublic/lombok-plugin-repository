# lombok-plugin-repository

[简体中文](/README.md) | English

This is a repository for Lombok plugin incompatibility issues with Android Studio.

## How to use

### Plugin repository (Recommend)

1. Go to `File` -> `Settings` -> `Plugins`, click the settings gear icon to the right of `Install`.
2. Select `Manage Plugin repositories...`.
3. Please add the repository link to the list based on the version you are using:
   + Release: `%RELEASE%`
   + Beta or Canary (includes Release): `%FULL%`
4. Search `Lombok` in `Marketplace` and install, enjoy!

### Manual installation

1. Go to the [repository branch](%BRANCH%) of this repository, find the Android Studio version you are using from README.md, and download `lombok-xxxx.x.x.tar.gz`.
2. Go to `File` -> `Settings` -> `Plugins`, click the settings gear icon to the right of `Install`.
3. Select `Install Plugin from Disk...`.
4. Select the downloaded `zip` file and click `OK`.
5. Lombok is successfully installed, enjoy!

## Versions

This table only lists the Release versions. For a complete list, please visit the [Wiki](%WIKI_URL%).

| Lombok Plugin Version | From IDEA Ultimate Version | For Android Studio Version |
| -------------------- | ---------------------------- | ------------------------------------- |
%VERSIONS%
