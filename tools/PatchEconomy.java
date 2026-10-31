import jdk.internal.org.objectweb.asm.*;
import jdk.internal.org.objectweb.asm.tree.*;
import java.nio.file.*;import java.util.*;import java.util.zip.*;
public class PatchEconomy implements Opcodes {
 static AbstractInsnNode previous(AbstractInsnNode n){do{n=n.getPrevious();}while(n!=null&&n.getOpcode()<0);return n;}
 public static void main(String[] args)throws Exception {
  String name="age/of/civilizations2/jakowski/lukasz/Turn_NewTurn.class";byte[] bytes;
  try(ZipFile z=new ZipFile("original/AoC2-exe.jar")){bytes=z.getInputStream(z.getEntry(name)).readAllBytes();}
  ClassNode c=new ClassNode();new ClassReader(bytes).accept(c,0);int fixes=0;
  for(MethodNode m:c.methods)for(AbstractInsnNode n:m.instructions.toArray()){
   if(n.getOpcode()!=FSUB)continue;AbstractInsnNode div=previous(n);if(div==null||div.getOpcode()!=FDIV)continue;
   AbstractInsnNode cast=previous(div);if(cast==null||cast.getOpcode()!=I2F)continue;AbstractInsnNode method=previous(cast);
   if(!(method instanceof MethodInsnNode))continue;MethodInsnNode call=(MethodInsnNode)method;
   if(!call.owner.equals("age/of/civilizations2/jakowski/lukasz/Game_Scenarios")||!call.name.equals("getScenario_StartingEconomy")||!call.desc.equals("()I"))continue;
   // Fail closed if the surrounding population-recovery expression changes.
   boolean population=false;AbstractInsnNode search=method;for(int i=0;i<12&&search!=null;i++,search=previous(search))if(search instanceof MethodInsnNode&&((MethodInsnNode)search).name.equals("getPopulation"))population=true;
   if(!population)throw new IllegalStateException("Unexpected recovery formula");
   m.instructions.set(n,new MethodInsnNode(INVOKESTATIC,"age/of/civilizations2/jakowski/lukasz/BugFixSupport","economyRecoveryFactor","(FF)F",false));fixes++;
  }
  if(fixes!=2)throw new IllegalStateException("Expected 2 player/spectator recovery sites, found "+fixes);
  ClassWriter w=new ClassWriter(0);c.accept(w);Path out=Paths.get("patch/classes",name);Files.createDirectories(out.getParent());Files.write(out,w.toByteArray());System.out.println("Patched economy recovery sites="+fixes+" (normal and spectator turn paths)");
 }
}
