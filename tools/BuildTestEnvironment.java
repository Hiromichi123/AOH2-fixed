import javassist.*;
public class BuildTestEnvironment { public static void main(String[] args)throws Exception {ClassPool p=new ClassPool(true);p.insertClassPath("original/AoC2-exe.jar");CtClass c=p.get("age.of.civilizations2.jakowski.lukasz.CFG");c.getClassInitializer().setBody("{}");c.writeFile("patch/test-classes");}}
