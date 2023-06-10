# lombok-plugin-repository

[简体中文](/README.md) | English

This is a repository for Lombok plugin incompatibility issues with Android Studio.

## How to use

### Plugin repository (Recommend)

1. Go to `File` -> `Settings` -> `Plugins`, click the settings gear icon to the right of `Install`.
2. Select `Manage Plugin repositories...`.
3. Please add the repository link to the list based on the version you are using:
   + Release: `https://raw.githubusercontent.com/sgpublic/lombok-plugin-repository/repository/release`
   + Beta or Canary (includes Release): `https://raw.githubusercontent.com/sgpublic/lombok-plugin-repository/repository/full`
4. Search `Lombok` in `Marketplace` and install, enjoy!

### Manual installation

1. Go to the [repository branch](https://github.com/sgpublic/lombok-plugin-repository/tree/repository) of this repository, find the Android Studio version you are using from README.md, and download `lombok-xxxx.x.x.tar.gz`.
2. Go to `File` -> `Settings` -> `Plugins`, click the settings gear icon to the right of `Install`.
3. Select `Install Plugin from Disk...`.
4. Select the downloaded `zip` file and click `OK`.
5. Lombok is successfully installed, enjoy!