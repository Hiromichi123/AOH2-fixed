package age.of.civilizations2.jakowski.lukasz;
import java.io.*;import java.nio.file.*;import java.util.*;import java.util.stream.*;
public class SavedRelationsTest {
 static Object read(Path p)throws Exception{try(ObjectInputStream in=new ObjectInputStream(Files.newInputStream(p))){return in.readObject();}}
 public static void main(String[] args)throws Exception{
  Path dir=Paths.get(args[0]);String tag=dir.getFileName().toString();List<Path> files=new ArrayList<Path>();try(Stream<Path>s=Files.list(dir)){s.filter(p->p.getFileName().toString().matches(".*_2X[0-9]*")).forEach(files::add);}
  files.sort(Comparator.comparingInt(p->{String name=p.getFileName().toString();String suffix=name.substring(name.indexOf("_2X")+3);return suffix.isEmpty()?0:Integer.parseInt(suffix);}));
  List<Save_Civ_GameData> data=new ArrayList<Save_Civ_GameData>();for(Path p:files){Object o=read(p);java.lang.reflect.Field f=o.getClass().getDeclaredField("lCivsData");f.setAccessible(true);data.addAll((List<Save_Civ_GameData>)f.get(o));}
  Game game=PatchTests.blank(Game.class);CFG.game=game;List<Civilization> civs=new ArrayList<Civilization>();civs.add(PatchTests.civ(0,new ArrayList<Integer>()));long invalid=0;int id=1;
  List<List<Float>> before=new ArrayList<List<Float>>();
  for(Save_Civ_GameData d:data){Civilization c=PatchTests.civ(id++,new ArrayList<Integer>());c.civGameData=d;civs.add(c);before.add(new ArrayList<Float>(d.lRelation.subList(0,Math.min(data.size(),d.lRelation.size()))));for(Float v:d.lRelation)if(v==null||!BugFixSupport.finite(v))invalid++;}
  PatchTests.set(game,"lCivs",civs);PatchTests.set(game,"iCivsSize",civs.size());BugFixSupport.updateRelations();long changedValid=0;int max=0;
  for(int i=1;i<civs.size();i++){List<Float> values=civs.get(i).civGameData.lRelation;max=Math.max(max,values.size());for(int j=0;j<values.size();j++){if(!BugFixSupport.finite(values.get(j)))throw new AssertionError("remaining invalid");if(j<before.get(i-1).size()&&BugFixSupport.finite(before.get(i-1).get(j))&&!values.get(j).equals(before.get(i-1).get(j)))changedValid++;}}
  PatchTests.check(changedValid==0,"all valid in-range relationships preserved");PatchTests.check(max<=data.size(),"relationship tables bounded to country count");System.out.println("PASS real save "+tag+" repaired invalid="+invalid+" remainingInvalid=0 in memory only; no save files written");
 }
}
