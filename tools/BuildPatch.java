import javassist.*;
import java.nio.file.*;
public class BuildPatch {
 public static void main(String[] args)throws Exception {
  ClassPool pool=new ClassPool(true);pool.insertClassPath("original/AoC2-exe.jar");pool.insertClassPath("patch/classes");
  String p="age.of.civilizations2.jakowski.lukasz.";CtClass civ=pool.get(p+"Civilization");
  for(String name:new String[]{"countPopulation","countPopulation_WithoutOccupied"}) {
   boolean unoccupied=name.endsWith("WithoutOccupied");
   civ.addMethod(CtNewMethod.make("protected final long "+name+"Long() { return "+p+"BugFixSupport.population(this,"+unoccupied+"); }",civ));
   civ.getDeclaredMethod(name).setBody("{ return "+p+"BugFixSupport.bounded("+name+"Long()); }");
  }
  civ.getDeclaredMethod("getRelation").setBody("{return "+p+"BugFixSupport.relation(this,$1);}");
  civ.getDeclaredMethod("setRelation").setBody("{"+p+"BugFixSupport.setRelation(this,$1,$2);}");
  civ.writeFile("patch/classes");
  CtClass action=pool.get(p+"Game_Action");action.getDeclaredMethod("updateRelations").setBody("{"+p+"BugFixSupport.updateRelations();}");action.writeFile("patch/classes");
  CtClass game=pool.get(p+"Game");
  game.getDeclaredMethod("getCivRelation_OfCivB").insertBefore("{if($1<=0||$2<=0||$1>=getCivsSize()||$2>=getCivsSize())return 0.0f;}");
  game.getDeclaredMethod("setCivRelation_OfCivB").insertBefore("{if($1<=0||$2<=0||$1>=getCivsSize()||$2>=getCivsSize())return;}");
  game.addMethod(CtNewMethod.make("protected final long countAlliance_PopulationLong(int id) { long total=0L; for(int i=0;i<getAlliance(id).getCivilizationsSize();i++)total+=getCiv(getAlliance(id).getCivilization(i)).countPopulationLong();return total;}",game));
  game.getDeclaredMethod("countAlliance_Population").setBody("{return "+p+"BugFixSupport.bounded(countAlliance_PopulationLong($1));}");game.writeFile("patch/classes");
  CtClass alliance=pool.get(p+"Alliance");
  alliance.addMethod(CtNewMethod.make("protected final long countPopulationLong() { long total=0L;for(int i=0;i<getCivilizationsSize();i++)total+="+p+"CFG.game.getCiv(getCivilization(i)).countPopulationLong();return total;}",alliance));
  alliance.getDeclaredMethod("countPopulation").setBody("{return "+p+"BugFixSupport.bounded(countPopulationLong());}");alliance.writeFile("patch/classes");
  CtClass dip=pool.get(p+"DiplomacyManager");
  dip.getDeclaredMethod("getImproveRelation").setBody("{float out=0.8425f+(float)"+p+"CFG.oR.nextInt(121)/100.0f;float a=(float)Math.max(1,"+p+"CFG.game.getCiv($1).getRankScore());float b=(float)Math.max(1,"+p+"CFG.game.getCiv($2).getRankScore());return 0.125f+out*(Math.min("+p+"CFG.game.getCivRelation_OfCivB($2,$1)+100.0f,145.0f)/200.0f)*Math.min(Math.max(0.325f,a/b),1.0f);}");dip.writeFile("patch/classes");
  CtClass next=pool.get(p+"Game_NextTurnUpdate");
  next.getDeclaredMethod("getDistanceFromAToB_PercOfMax").insertAfter("{if(!"+p+"BugFixSupport.finite($_))$_=1.0f;}");next.writeFile("patch/classes");
  System.out.println("Patched population and diplomacy logic; original method descriptors retained.");
 }
}
