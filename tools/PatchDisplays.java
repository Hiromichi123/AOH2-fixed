import jdk.internal.org.objectweb.asm.*;
import jdk.internal.org.objectweb.asm.tree.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
public class PatchDisplays implements Opcodes {
 static String prefix="age/of/civilizations2/jakowski/lukasz/";
 static AbstractInsnNode next(AbstractInsnNode n){do{n=n.getNext();}while(n!=null&&n.getOpcode()<0);return n;}
 static boolean count(MethodInsnNode n){return n.owner.startsWith(prefix)&&(n.name.equals("countPopulation")||n.name.equals("countPopulation_WithoutOccupied")||n.name.equals("countAlliance_Population"))&&n.desc.endsWith(")I");}
 static boolean append(AbstractInsnNode n){return n instanceof MethodInsnNode&&((MethodInsnNode)n).owner.equals("java/lang/StringBuilder")&&((MethodInsnNode)n).name.equals("append")&&((MethodInsnNode)n).desc.equals("(I)Ljava/lang/StringBuilder;");}
 static LocalVariableNode variable(MethodNode m,int slot,AbstractInsnNode n,boolean store){
  int pos=m.instructions.indexOf(n);LocalVariableNode best=null;
  if(m.localVariables==null)return null;
  for(LocalVariableNode v:m.localVariables){if(v.index!=slot||!v.desc.equals("I"))continue;int start=m.instructions.indexOf(v.start),end=m.instructions.indexOf(v.end);if(pos>=start&&pos<end)return v;if(store&&start>pos&&start<=pos+3)best=v;}
  return best;
 }
 public static void main(String[] args)throws Exception {
  int strings=0,shadows=0,sums=0,classes=0;
  try(ZipFile z=new ZipFile("original/AoC2-exe.jar")){
   for(ZipEntry e:Collections.list(z.entries())){
    if(!e.getName().startsWith(prefix)||!e.getName().endsWith(".class"))continue;
    Path out=Paths.get("patch/classes",e.getName());byte[] bytes=Files.exists(out)?Files.readAllBytes(out):z.getInputStream(e).readAllBytes();
    ClassNode c=new ClassNode();new ClassReader(bytes).accept(c,0);boolean changed=false;
    for(MethodNode m:c.methods){
     Map<LocalVariableNode,Integer> shadow=new HashMap<>();List<AbstractInsnNode> old=new ArrayList<>();for(AbstractInsnNode n:m.instructions)old.add(n);
     for(AbstractInsnNode n:old){if(!(n instanceof MethodInsnNode)||!count((MethodInsnNode)n))continue;MethodInsnNode call=(MethodInsnNode)n;AbstractInsnNode after=next(n);
      if(append(after)){call.name+="Long";call.desc=call.desc.substring(0,call.desc.length()-1)+"J";((MethodInsnNode)after).desc="(J)Ljava/lang/StringBuilder;";strings++;changed=true;}
      else if(after!=null&&after.getOpcode()==ISTORE){LocalVariableNode v=variable(m,((VarInsnNode)after).var,after,true);if(v==null)continue;
       Integer slot=shadow.get(v);if(slot==null){slot=m.maxLocals;m.maxLocals+=2;shadow.put(v,slot);}
       call.name+="Long";call.desc=call.desc.substring(0,call.desc.length()-1)+"J";
       InsnList insert=new InsnList();insert.add(new InsnNode(DUP2));insert.add(new VarInsnNode(LSTORE,slot));insert.add(new MethodInsnNode(INVOKESTATIC,prefix+"BugFixSupport","bounded","(J)I",false));m.instructions.insert(call,insert);changed=true;
      }
     }
     for(AbstractInsnNode n:old){if(n.getOpcode()==ILOAD&&append(next(n))){LocalVariableNode v=variable(m,((VarInsnNode)n).var,n,false);Integer slot=shadow.get(v);if(slot!=null){((VarInsnNode)n).var=slot;((VarInsnNode)n).setOpcode(LLOAD);((MethodInsnNode)next(n)).desc="(J)Ljava/lang/StringBuilder;";shadows++;changed=true;}}}
     for(AbstractInsnNode n:old){if(n.getOpcode()!=IADD)continue;AbstractInsnNode after=next(n);boolean protect=false;
      if(after!=null&&after.getOpcode()==ISTORE){LocalVariableNode v=variable(m,((VarInsnNode)after).var,after,true);protect=v!=null&&v.name.toLowerCase().contains("pop");}
      if((c.name.contains("WorldPopulation")||c.name.contains("ContinentPopulation")||c.name.endsWith("/TimelapseManager"))&&after instanceof MethodInsnNode){MethodInsnNode a=(MethodInsnNode)after;protect|=a.owner.equals("java/lang/Integer")&&a.name.equals("valueOf");}
      if(protect){m.instructions.set(n,new MethodInsnNode(INVOKESTATIC,prefix+"BugFixSupport","add","(II)I",false));sums++;changed=true;}
     }
    }
    if(changed){ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES) { protected ClassLoader getClassLoader(){return PatchDisplays.class.getClassLoader();} };c.accept(writer);Files.createDirectories(out.getParent());Files.write(out,writer.toByteArray());classes++;}
   }
  }
  System.out.println("Display classes="+classes+" exact-long strings="+strings+" exact-long stored strings="+shadows+" protected sums="+sums);
 }
}
