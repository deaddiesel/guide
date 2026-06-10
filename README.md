# [Guide](https://github.com/deaddiesel/guide)

[![Latest Release](https://img.shields.io/github/v/release/deaddiesel/guide?style=flat-square&label=Release)](https://github.com/deaddiesel/guide/releases)
[![License](https://img.shields.io/badge/License-MIT-red.svg?style=flat-square)](https://opensource.org/licenses/MIT)

Interactive in-game guidebooks layout.
Render all the complex multiblocks.
Parse Markdown text right inside the UI!

## Setting up a Workspace / Compiling from Source

Note: Git MUST be installed and in the system path to use our scripts.
* **Setup**: Import Guide as a Gradle project into IntelliJ IDEA. Let it sync dependencies.
* **Build**: Run the `.\gradlew build` command through Terminal. The compiled jar will be in `build/libs/`.
* **Troubleshooting**: If obscure Gradle issues are found, try running `.\gradlew --stop` and `.\gradlew clean`.

## Issue Reporting

Please include the following when creating an issue on our [Issue Tracker](https://github.com/deaddiesel/guide/issues):

* Minecraft version (Strictly 1.20.1)
* Guide mod version
* Forge version (47.4.20+)
* Versions of any mods potentially related to the issue
* Any relevant screenshots or layout bugs are greatly appreciated.
* **For crashes**:
	* Detailed steps to reproduce
	* Full `latest.log` or `crash-report` inside code blocks

## Credits

Special thanks to the incredible community, developers, and open-source tools that made this mod possible:

* **mezz** (https://github.com/mezz) — For developing the Just Enough Items (JEI) API, which powers our in-game recipe integration.
* **vladsch** (https://github.com/vladsch) — For the Flexmark-Java library, the robust Markdown parsing engine used inside our custom UI screens.
* **LexManos** (https://github.com/LexManos), **cpw** (https://github.com/cpw) & **Minecraft Forge Team** — For creating the FML loading ecosystem, MCP tools, and the excellent modding platform we build upon.
* **The Open Source Community** — For tireless dedication to submitting bug reports, documentation guides, and supporting independent mod development.

## Licenses

Code, textures, and binaries are licensed under the [MIT License](https://opensource.org/licenses/MIT).

You are allowed to use this mod in your modpack. Any modpack which uses Guide takes **full** responsibility for user support queries. We only support official builds, not custom modified jars.
