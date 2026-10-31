"""Wrap the built JAR in the unchanged native launcher; writes dist/ only."""
from pathlib import Path
import hashlib,json,zipfile
root=Path(__file__).resolve().parents[1]
source=root/'original/AoC2-before-launcher-patch.exe';jar=root/'dist/AoC2-Bugfix.jar';output=root/'dist/AoC2-fixed.exe'
with zipfile.ZipFile(source) as original:offset=min(i.header_offset for i in original.infolist())
original=source.read_bytes()
if hashlib.sha256(original).hexdigest()!='5300d13579f51e8e8dc69f0a24cf6bafe22297754cfebb4701f7a92e6b5ebe66':raise SystemExit('Original launcher checksum mismatch')
output.write_bytes(original[:offset]+jar.read_bytes())
with zipfile.ZipFile(output) as exe,zipfile.ZipFile(jar) as fixed:
 assert exe.testzip() is None
 assert exe.namelist()==fixed.namelist()
 for name in fixed.namelist():assert exe.read(name)==fixed.read(name),name
manifest={'version':'population-diplomacy-economy99','original_exe_sha256':hashlib.sha256(original).hexdigest(),'patched_exe_sha256':hashlib.sha256(output.read_bytes()).hexdigest(),'patched_jar_sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'unchanged_launcher_bytes':offset}
(root/'dist/launcher-patch-manifest.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')
print('Built',output,'with unchanged',offset,'byte native launcher prefix')
