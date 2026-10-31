from pathlib import Path
import json,zipfile,hashlib,shutil
folder=Path('release/AoC2-Bugfix-1.0.0')
folder.mkdir(parents=True,exist_ok=True)
for source in Path('installer').iterdir():
 if source.is_file():shutil.copy2(source,folder/source.name)
readme='''# AoH2 非官方修复补丁 1.0.0（Windows，需手动安装）

适用于 Age of History II 原版，Steam App ID 603850。不是 Definitive Edition（3381680）。订阅或下载不会自动生效：本补丁修改 Java 游戏逻辑，需要运行安装脚本。

## 安装

1. 退出游戏，解压本文件夹。建议先备份要继续游玩的存档。
2. 双击 Install.cmd。脚本尝试查找 Steam 游戏目录；找不到时输入包含 AoC2.exe 的目录。
3. 安装成功后，照常从 Steam 或原 AoC2.exe 快捷方式启动。

也可在 PowerShell 中执行：

```powershell
.\Install.ps1 -GameDirectory 'D:\\SteamLibrary\\steamapps\\common\\AoCII' -Mode Check
.\Install.ps1 -GameDirectory 'D:\\SteamLibrary\\steamapps\\common\\AoCII' -Mode Install
```

只需要 Windows PowerShell 和已安装的游戏，不需要 Python、JDK 或另行下载构建库。CMD 中的执行策略参数仅用于本次 PowerShell 进程，不修改系统策略。不要求管理员身份；如果游戏目录不可写，应先解决该目录的写入权限。

## 版本与冲突

安装器只接受以下原版 AoC2.exe 的 SHA-256：
`5300d13579f51e8e8dc69f0a24cf6bafe22297754cfebb4701f7a92e6b5ebe66`

安装后 EXE 的 SHA-256：
`b154e0f73e33fe3e18650b379ed410e4140ecec26178e04379b5cb8dd1635b4b`

其他更新版本、AoC2_Old.exe、独立 AoC2.jar、已有其他代码修改的 EXE 均不适用，脚本会拒绝覆盖。不通过跳过校验来强行安装。地图或剧本内容的兼容性没有逐项验证。

## 修复内容与边界

- 国家人口主要显示改用 64 位汇总；旧 int 接口及选定加法防止回绕成负数。部分旧图表、世界/大陆统计、历史记录、排名和占比仍会封顶或丢失上限后的精度；单省人口仍是 int。不是全游戏无限人口支持。
- 外交：拦截 NaN/Infinity，修正国家 ID 错误与关系表膨胀；旧异常关系优先借用健康的反向关系，无法恢复时回退中立 0，不能精确找回已丢失的原值。有效的 0 不会被当作损坏项处理。
- 经济 99：修正低经济恢复项在人口较高时变成负扣减的问题，普通和观察者回合路径均修改。保留低投资、物资不足等原有惩罚；不是无条件增加经济。

安装器只改 AoC2.exe，不读写存档。运行游戏后，外交异常会在内存中按规则恢复；之后的常规存档会保存恢复后的状态。需要回退存档状态时，应使用自己的旧存档备份。

## 卸载

退出游戏，双击 Uninstall.cmd，并在需要时指定游戏目录。原版备份名：AoC2.exe.aoc2bugfix-original.bak。卸载仅在当前 EXE 和备份校验正确时恢复；不会还原后来保存的存档状态。Steam 更新/验证文件可能覆盖补丁，更新后的新版本须重新验证兼容性。

## 测试状态

游戏自带 Java 8 的人口、外交、经济逻辑测试通过；38 个修改类通过 JVM 校验。另有旧存档内存恢复测试。安装器已在独立测试目录验证：精确重建、重复安装、卸载和错误版本拒绝。尚未完成完整游戏界面与长期实机对局验证；工坊上传和订阅下载流程也尚未验证。

## 工坊使用说明

此文件夹可作为待上传内容，但它不是游戏能自动加载的地图/剧本模组。下载后，订阅者须找到下载文件夹并运行 Install.cmd；取消订阅不等同于卸载 EXE 修改，应先运行 Uninstall.cmd。

Steam 官方机制由游戏自行加载订阅内容：https://partner.steamgames.com/doc/features/workshop/implementation
本游戏工坊：https://steamcommunity.com/app/603850/workshop/

## 包内容

patch-data.zip 只包含从原 EXE 重建修复版所需的差分数据与校验清单；不包含完整 EXE/JAR、游戏资源、个人存档、Steam 凭据或构建依赖。安装依赖玩家自己的匹配版本原 EXE。
'''
(folder/'README-中文.md').write_text(readme,encoding='utf-8-sig')
(folder/'README-English.md').write_text('''# AoH2 Unofficial Bugfix 1.0.0 — MANUAL INSTALLATION

Windows, original Age of History II (Steam app 603850), not Definitive Edition. Subscribing alone does not activate this code patch.

Close the game, extract this folder, and run Install.cmd. Select the directory containing AoC2.exe if automatic Steam-library detection fails. Launch normally through Steam after installation. No Python or JDK is required.

The installer requires the exact original EXE with SHA-256:
5300d13579f51e8e8dc69f0a24cf6bafe22297754cfebb4701f7a92e6b5ebe66
It refuses unsupported versions or other executable code mods. It backs up the original as AoC2.exe.aoc2bugfix-original.bak. Run Uninstall.cmd before unsubscribing to restore that backup. Steam updates or file verification may overwrite the patch.

Fixes:
- Exact 64-bit national population on primary displays; compatibility guards prevent selected legacy integer totals from wrapping negative. Some old graphs, world/continent totals, ranking comparisons and percentages retain integer limits. Individual province populations remain int. This is not an unlimited-population conversion of the entire game.
- Reject non-finite diplomatic relationships; correct country-ID iteration and relationship-table growth. Corrupted saved relationships use a valid reverse relationship where possible, otherwise neutral 0. Lost original values cannot be reconstructed exactly.
- Prevent the low-economy recovery term from becoming an extra negative penalty at high population, which can trap economy at the minimum of 99. Normal and spectator turn paths are patched. Intentional penalties remain.

Installation does not read or write saves. During play, corrupted diplomacy is repaired in memory and may later be saved by normal game saving. Back up saves before continuing a campaign. Uninstalling the executable patch does not undo changes already saved during play.

Java 8 logic tests and verification of 38 modified classes passed. Installer round-trip, idempotence and rejection tests passed. Full in-game and long-campaign testing, plus Workshop upload/subscription testing, are pending.

The package contains a binary delta, installer and documentation, not a complete EXE/JAR, game assets or personal saves. See README-中文.md for details.
''',encoding='utf-8')
(folder/'CHANGELOG.md').write_text('''# 1.0.0 — 2026-10-31

- Population overflow compatibility guards and primary 64-bit displays.
- Diplomacy non-finite value guards, recovery rules, ID iteration and table cleanup.
- Economy 99 negative recovery-term correction in normal/spectator modes.
- Portable differential installer with strict source/target hashes, backup and uninstall.

Initial release candidate. Full gameplay and Workshop distribution have not yet been validated.
''',encoding='utf-8')
(folder/'TEST-RESULTS.txt').write_text((Path('analysis/release-installer-tests.txt').read_text(encoding='utf-8-sig') if Path('analysis/release-installer-tests.txt').is_file() else 'Installer tests: run tools/TestRelease.ps1 to generate local results.\n')+'\n'+(Path('analysis/economy-patch-tests.txt').read_text(encoding='utf-8-sig') if Path('analysis/economy-patch-tests.txt').is_file() else 'Logic tests: run tools/run_tests.py to generate local results.\n'),encoding='utf-8')
with zipfile.ZipFile(folder/'patch-data.zip') as payload:
 release_manifest=json.loads(payload.read('manifest.json'))
for filename in ['README-中文.md','README-English.md']:
 p=folder/filename
 p.write_text(p.read_text(encoding='utf-8-sig').replace('b154e0f73e33fe3e18650b379ed410e4140ecec26178e04379b5cb8dd1635b4b',release_manifest['target_sha256']),encoding='utf-8-sig')
# Hash the readable installer and payload as well as documentation for review.
checks={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in folder.iterdir() if p.is_file() and p.name!='SHA256SUMS.txt'}
(folder/'SHA256SUMS.txt').write_text(''.join(h+'  '+n+'\n' for n,h in sorted(checks.items())),encoding='utf-8')
kit=Path('release/AoC2-Bugfix-Publishing-Kit-1.0.0');kit.mkdir(exist_ok=True)
content=kit/'content';content.mkdir(exist_ok=True)
for p in folder.iterdir():
 if p.is_file():shutil.copy2(p,content/p.name)
(kit/'工坊介绍.txt').write_text('''AoH2 非官方修复：人口溢出 / 外交异常 / 经济99（需手动安装）

【重要】这是原版 Age of History II（603850）的 EXE 代码差分补丁，不适用于决定版。订阅不会自动生效，下载后需运行 Install.cmd。取消订阅前先运行 Uninstall.cmd。

内容：
• 主要国家人口显示使用64位，兼容接口防止溢出回绕。部分旧统计仍受整数上限限制。
• 拦截外交 NaN 异常和传播，修正国家ID与关系表膨胀。损坏关系按反向关系/中立规则恢复，不能精确还原。
• 修正高人口省份低经济恢复项变成额外扣减、经济卡在99的问题。

使用：退出游戏→找到本条目下载目录→运行 Install.cmd→选择含 AoC2.exe 的游戏目录→照常从Steam启动。

安装器只接受README指定校验值的原版EXE，其他代码模组或其他版本会被拒绝。自动备份原EXE；安装本身不改存档。建议另备份存档，运行中的外交恢复可能随后被常规存档保存。

目前是1.0.0候选版：Java8逻辑与安装/卸载测试通过，尚未完成完整界面、长期对局和工坊下载验证。

本包为差分补丁，不含完整游戏、资源或个人存档。非官方项目。
''',encoding='utf-8-sig')
(kit/'workshop_build.vdf.example').write_text('''"workshopitem"
{
    "appid" "603850"
    "publishedfileid" "0"
    "contentfolder" "C:\\REPLACE_WITH_PATH\\content"
    "visibility" "2"
    "title" "AoH2 Bugfix: Population, Diplomacy, Economy 99 [Manual Install]"
    "description" "Manual-install executable delta for the original Age of History II. Subscribing alone does not activate it. Read the included installation instructions and compatibility limits."
    "changenote" "1.0.0 release candidate: population, diplomacy and economy-99 fixes."
}
''',encoding='utf-8')
(kit/'发布说明.md').write_text('''# 工坊发布准备材料

content/ 是待上传的实际内容文件夹。工坊介绍.txt 是中文文案，可编辑后用于条目正文。不是把外层 ZIP 上传后就能自动生效的游戏模组。

本机游戏的 SteamManager.createItem(String) 为空；对全部游戏类的引用检查只发现政府类型管理器使用订阅目录，没有发现代码 JAR 的订阅加载路径。因此当前交付按“下载后手动安装”设计，不伪装为可自动启用的模组。

workshop_build.vdf.example 是 Valve 文档中的 SteamCMD 测试上传方式所需字段模板，尚未使用真实账号验证 App 603850 是否允许该方式上传。请复制为 .vdf，填写 contentfolder 的实际绝对路径；初次测试 publishedfileid 为0、visibility为2（私有）。需要预览图时自行添加 previewfile 的绝对路径。成功创建后应保留写回的 publishedfileid，避免下次误建重复条目。

Valve 将 SteamCMD 上传描述为测试用途；真实发行应使用游戏支持的发布工具或作者提供的流程。若603850拒绝上传，需先解决该游戏的上传入口/权限问题，不能把决定版3381680的工坊当作替代目标。也可将同一差分包用于论坛等手动下载发布。

官方文档：https://partner.steamgames.com/doc/features/workshop/implementation#SteamCmdIntegration
本游戏工坊：https://steamcommunity.com/app/603850/workshop/

这次只打包，没有登录Steam、创建条目或上传发布。
''',encoding='utf-8-sig')
for src,name in [(folder,'AoC2-Bugfix-1.0.0.zip'),(kit,'AoC2-Bugfix-Publishing-Kit-1.0.0.zip')]:
 out=Path('release')/name
 with zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED) as z:
  for p in sorted(src.rglob('*')):
   if p.is_file():z.write(p,Path(src.name)/p.relative_to(src))
 with zipfile.ZipFile(out) as z:assert z.testzip() is None
 print(name,out.stat().st_size,'bytes',hashlib.sha256(out.read_bytes()).hexdigest())
