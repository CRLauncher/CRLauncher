# CRLauncher

[![GitHub Downloads](https://img.shields.io/github/downloads/CRLauncher/CRLauncher/total?label=downloads&labelColor=27303D&color=0D1117&logo=github&logoColor=FFFFFF&style=flat)](https://github.com/CRLauncher/CRLauncher/releases)
[![GitHub Downloads](https://img.shields.io/badge/Join-CRLauncher-blue?logo=discord&color=0D1117&style=flat&labelColor=27303D)](https://discord.gg/TAMA6K6xuA)

> [!WARNING]  
> This project is unfinished. Do not have any expectations.

CRLauncher - an unofficial launcher for [Cosmic Reach](https://finalforeach.itch.io/cosmic-reach)

![Screenshot.png](images/Screenshot.png)

### Quick Start

You can either download a prebuilt jar from [Releases](https://github.com/CRLauncher/CRLauncher/releases) page, or build it yourself. To do that you will need at least JDK 17:
```shell
$ git clone https://github.com/CRLauncher/CRLauncher
$ cd CRLauncher
$ gradlew clean build
```

Done! To run the launcher, just do:
```shell
$ java -jar dist/CRLauncher.jar
```

If you want to change the location of launcher's files, add `--workDir` argument:
```shell
$ java -jar dist/CRLauncher.jar --workDir C:\Users\User\Documents\CRLauncher
```

Or you can use the `--useJarLocation` argument, which will make the launcher use the jar's location instead. In that case the `--workDir` argument will be ignored:
```shell
$ java -jar dist/CRLauncher.jar --useJarLocation
```
This way the launcher will create its files in dist


### Plans:
 - Add support for CRM-1 mod repositories