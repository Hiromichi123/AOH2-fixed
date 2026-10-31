"""Rebuild the compatibility patch from the locally extracted EXE archive."""
from pathlib import Path
import os, subprocess, zipfile, hashlib, json
ROOT=Path(__file__).resolve().parents[1]
os.chdir(ROOT)
JDK=Path(os.environ.get('AOC2_BUILD_JDK',r'C:\Program Files\Java\jdk-17'))/'bin'
JAVA=str(JDK/'java.exe'); JAVAC=str(JDK/'javac.exe')
EXPORTS=['--add-exports','java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED','--add-exports','java.base/jdk.internal.org.objectweb.asm.tree=ALL-UNNAMED']
def run(*args): subprocess.run(args,check=True)
for required in ['original/AoC2-exe.jar','tools/javassist-3.30.2-GA.jar']:
 if not (ROOT/required).is_file():raise SystemExit('Missing build input: '+required+'; see README.md')
(ROOT/'dist').mkdir(exist_ok=True)
classes=ROOT/'patch/classes';classes.mkdir(parents=True,exist_ok=True)
for p in classes.rglob('*.class'):
 if not p.resolve().is_relative_to(classes.resolve()):raise RuntimeError('Unexpected output path')
 p.unlink()
run(JAVAC,'--release','8','-encoding','UTF-8','-cp','original/AoC2-exe.jar','-d',str(classes),'patch/src/age/of/civilizations2/jakowski/lukasz/BugFixSupport.java')
run(JAVAC,'-encoding','UTF-8','-cp','tools/javassist-3.30.2-GA.jar','-d','tools','tools/BuildPatch.java')
run(JAVA,'-cp','tools;tools/javassist-3.30.2-GA.jar','BuildPatch')
run(JAVAC,'-encoding','UTF-8',*EXPORTS,'-d','tools','tools/PatchDisplays.java')
run(JAVA,*EXPORTS,'-cp','tools;patch/classes;original/AoC2-exe.jar','PatchDisplays')
run(JAVAC,'-encoding','UTF-8',*EXPORTS,'-d','tools','tools/PatchEconomy.java')
run(JAVA,*EXPORTS,'-cp','tools;patch/classes;original/AoC2-exe.jar','PatchEconomy')
source=ROOT/'original/AoC2-exe.jar';output=ROOT/'dist/AoC2-Bugfix.jar'
changes={p.relative_to(classes).as_posix():p for p in classes.rglob('*.class')}
manifest={'base':'Java archive embedded in the local AoC2.exe','base_jar_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'patched_classes':sorted(changes)}
with zipfile.ZipFile(source) as original,zipfile.ZipFile(output,'w',zipfile.ZIP_DEFLATED) as target:
 for info in original.infolist():
  if info.filename.upper().startswith('META-INF/') and info.filename.upper().endswith(('.SF','.RSA','.DSA')):continue
  data=changes.pop(info.filename).read_bytes() if info.filename in changes else original.read(info.filename)
  target.writestr(info,data)
 for name,path in changes.items():target.writestr(name,path.read_bytes())
manifest['patched_jar_sha256']=hashlib.sha256(output.read_bytes()).hexdigest()
(ROOT/'dist/patch-manifest.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')
print('Built',output,'with',len(manifest['patched_classes']),'patched classes')
