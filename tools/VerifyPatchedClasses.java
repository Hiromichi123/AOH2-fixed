import java.nio.file.*;
import java.util.stream.*;
public class VerifyPatchedClasses {
 public static void main(String[] args)throws Exception {int count=0;try(Stream<Path>s=Files.walk(Paths.get("patch/classes"))){for(Path p:(Iterable<Path>)s.filter(p->p.toString().endsWith(".class"))::iterator){String n=Paths.get("patch/classes").relativize(p).toString().replace('\\','.');n=n.substring(0,n.length()-6);Class<?> c=Class.forName(n,false,VerifyPatchedClasses.class.getClassLoader());c.getDeclaredMethods();c.getDeclaredConstructors();count++;}}System.out.println("Verified all "+count+" patched classes without initializing game UI.");}
}
