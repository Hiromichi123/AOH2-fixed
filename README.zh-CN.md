# AoH2 非官方修复补丁

[English](README.md) · [项目仓库](https://github.com/Hiromichi123/AOH2-fixed) · [版本下载](https://github.com/Hiromichi123/AOH2-fixed/releases)

适用于 Windows 上的**原版 Age of History II（Steam App ID 603850）**，不适用于决定版（3381680）。当前状态：**1.0.0 候选版**。逻辑测试和安装器测试通过，完整界面与长期实机对局仍待验证。

## 修复内容与限制

- **人口溢出**：主要人口显示增加 64 位国家/联盟汇总，选定旧整数运算防止回绕成负数。部分旧图表、世界/大陆统计、历史记录、排名和占比仍受整数上限限制；单省人口仍为 `int`，不是全游戏无限人口支持。
- **外交异常**：拦截 NaN/Infinity，修正国家 ID 错误与关系表膨胀。旧异常值优先借用健康反向关系，无法恢复时回退中立 0；这是恢复规则，不能精确找回已经丢失的原值。
- **经济卡在 99**：修正高人口省份的低经济恢复项反而扣减经济的问题，普通对局与观察者两条路径均修改。低投资、物资不足等原有惩罚保留。

## 玩家使用

在有可用版本后，从 [Releases](https://github.com/Hiromichi123/AOH2-fixed/releases) 下载玩家安装包。上传本仓库本身不会自动创建 Release 下载项。

1. 退出游戏，另行备份要继续游玩的存档。
2. 解压安装包，运行 `Install.cmd`；自动定位失败时选择包含 `AoC2.exe` 的目录。
3. 照常从 Steam 或原快捷方式启动。
4. 卸载时先退出游戏，再运行安装包内的 `Uninstall.cmd`。

安装器备份匹配的原 EXE 为 `AoC2.exe.aoc2bugfix-original.bak`，只替换 EXE，不读写存档。运行游戏时恢复的外交状态可能随后被游戏常规存档保存；卸载程序补丁不会撤销已经保存的对局变化。Steam 更新或验证文件完整性可能覆盖补丁。

要求原版 `AoC2.exe` 的 SHA-256 为：

```text
5300d13579f51e8e8dc69f0a24cf6bafe22297754cfebb4701f7a92e6b5ebe66
```

其他版本或已有代码模组的 EXE 会被拒绝。独立 `AoC2.jar` 与 `AoC2_Old.exe` 不能作为安装输入。当前补丁需手动安装，工坊订阅不能直接启用。

## 从源码构建

需要 Windows、Python **3.9+**、**JDK 17**。补丁辅助类面向 Java 8 编译。另需从 [Maven Central](https://repo.maven.apache.org/maven2/org/javassist/javassist/3.30.2-GA/javassist-3.30.2-GA.jar) 下载 Javassist **3.30.2-GA**，放到 `tools/javassist-3.30.2-GA.jar`；仓库不包含该依赖，其许可证见 [Javassist 项目](https://github.com/jboss-javassist/javassist)。

自行提供符合上述哈希的原版 EXE。如果本机已经安装补丁，请使用原 EXE 备份作为输入。在仓库根目录运行：

```powershell
$env:AOC2_BUILD_JDK = 'C:\Program Files\Java\jdk-17'
python tools/prepare_original.py 'D:\SteamLibrary\steamapps\common\AoCII\AoC2.exe'
python patch/build.py
python tools/build_launcher.py
python tools/run_tests.py
```

若要使用游戏自带的 Java 8 验证，运行测试前将 `AOC2_TEST_JAVA` 设为游戏的 `jre\bin\java.exe`。测试使用仅供测试的 CFG 初始化替身以跳过图形资源加载，其余关键逻辑来自实际补丁类；不等于完整对局测试。

生成差分发布包，并在隔离目录测试安装/卸载：

```powershell
python tools/PackageRelease.py
python tools/WriteReleaseDocs.py
powershell -NoProfile -ExecutionPolicy Bypass -File tools/TestRelease.ps1
```

`dist/AoC2-fixed.exe`、`dist/AoC2-Bugfix.jar` 是本地产物。`release/` 内生成的差分包不含完整游戏。安装器测试只操作 `release/tests/`，不会修改已安装游戏。构建脚本使用 JDK 内部 ASM 并显式开放模块访问，请使用文档指定的 JDK。

## 仓库结构

| 路径 | 内容 |
|---|---|
| `patch/src/` | 修复辅助类、隔离逻辑测试 |
| `patch/build.py` | 编译辅助类并局部替换方法 |
| `tools/` | 字节码变换、校验和打包工具 |
| `installer/` | 可审阅的 PowerShell/CMD 安装器源码 |
| `docs/` | 测试状态与发布说明 |

`.gitignore` 排除了原游戏程序、游戏资源、反编译代码、个人存档、下载依赖与构建产物。这里公开的是修复源码和工具，**不是完整游戏，也不是能够整体重编译的作者原始工程**。

这是非官方项目，构建和安装均依赖使用者自己的游戏文件。
