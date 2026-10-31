"""Run isolated tests against built classes without starting the game UI."""
from pathlib import Path
import os,subprocess
root=Path(__file__).resolve().parents[1];os.chdir(root)
jdk=Path(os.environ.get('AOC2_BUILD_JDK',r'C:\Program Files\Java\jdk-17'))/'bin'
java8=os.environ.get('AOC2_TEST_JAVA',str(jdk/'java.exe'))
javac=str(jdk/'javac.exe');java=str(jdk/'java.exe');jar='tools/javassist-3.30.2-GA.jar'
def run(*args):subprocess.run(args,check=True)
run(javac,'-encoding','UTF-8','-cp',jar,'-d','tools','tools/BuildTestEnvironment.java')
run(java,'-cp','tools;'+jar,'BuildTestEnvironment')
run(javac,'--release','8','-encoding','UTF-8','-cp','patch/classes;original/AoC2-exe.jar','-d','patch/test-classes','patch/src/age/of/civilizations2/jakowski/lukasz/PatchTests.java')
run(javac,'--release','8','-encoding','UTF-8','-d','tools','tools/VerifyPatchedClasses.java')
run(java8,'-Xverify:all','-cp','tools;dist/AoC2-Bugfix.jar','VerifyPatchedClasses')
result=subprocess.run([java8,'-Xverify:all','-cp','patch/test-classes;dist/AoC2-Bugfix.jar','age.of.civilizations2.jakowski.lukasz.PatchTests'],stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,encoding='utf-8',errors='replace')
(root/'analysis').mkdir(exist_ok=True)
(root/'analysis/economy-patch-tests.txt').write_text(result.stdout,encoding='utf-8')
print(result.stdout);result.check_returncode()
