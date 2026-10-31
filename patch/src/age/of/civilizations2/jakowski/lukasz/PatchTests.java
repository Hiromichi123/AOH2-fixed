package age.of.civilizations2.jakowski.lukasz;
import java.lang.reflect.*;
import java.util.*;
public class PatchTests {
 static final Object unsafe; static final Method allocate;
 static {try {Class<?> c=Class.forName("sun.misc.Unsafe");Field f=c.getDeclaredField("theUnsafe");f.setAccessible(true);unsafe=f.get(null);allocate=c.getMethod("allocateInstance",Class.class);}catch(Exception e){throw new RuntimeException(e);}}
 static <T>T blank(Class<T> c)throws Exception{return c.cast(allocate.invoke(unsafe,c));}
 static void set(Object o,String name,Object value)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);f.set(o,value);}
 static Civilization civ(int id,List<Integer> provinces)throws Exception{Civilization c=blank(Civilization.class);set(c,"iCivID",id);set(c,"iNumOfProvinces",provinces.size());set(c,"lProvinces",provinces);c.civGameData=blank(Save_Civ_GameData.class);c.civGameData.lRelation=new ArrayList<Float>(Arrays.asList(50f,50f,50f));return c;}
 static Province province(int pop,int owner,int trueOwner)throws Exception{Province p=blank(Province.class);Save_Provinces_GameData data=blank(Save_Provinces_GameData.class);Province_Population population=blank(Province_Population.class);set(population,"iPopulation",pop);data.oPopulation=population;data.iTrueOwnerOfProvince=trueOwner;Province_Army army=blank(Province_Army.class);set(army,"iCivID",owner);data.lArmies=new ArrayList<Province_Army>(Arrays.asList(army));p.saveProvinceData=data;return p;}
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);System.out.println("PASS "+message);}
 public static void main(String[] args)throws Exception{
  Game game=blank(Game.class);CFG.game=game;CFG.oR=new Random(42);Civilization c0=civ(0,new ArrayList<Integer>()),c1=civ(1,new ArrayList<Integer>(Arrays.asList(0,1))),c2=civ(2,new ArrayList<Integer>(Arrays.asList(2))),c3=civ(3,new ArrayList<Integer>());
  set(game,"lCivs",new ArrayList<Civilization>(Arrays.asList(c0,c1,c2,c3)));set(game,"iCivsSize",4);
  for(int[] values:new int[][]{{900000000,900000000},{1073741823,1073741824},{1073741824,1073741824},{1200000000,1200000000},{2000000000,2000000000}}){
   set(game,"lProvinces",new ArrayList<Province>(Arrays.asList(province(values[0],1,1),province(values[1],1,2),province(100,2,2))));long expected=(long)values[0]+values[1];
   check(c1.countPopulationLong()==expected,"exact national population "+expected);
   check(c1.countPopulation()==Math.min(Integer.MAX_VALUE,expected),"legacy population cannot wrap "+expected);
   check(c1.countPopulation_WithoutOccupiedLong()==values[0],"occupied province filter "+expected);
  }
  c1.setRelation(1,75f);c1.setRelation(1,Float.NaN);check(c1.getRelation(1)==75f,"NaN update retains healthy relation");
  c1.setRelation(1,Float.POSITIVE_INFINITY);check(c1.getRelation(1)==75f,"infinite update rejected");
  c1.setRelation(1,200f);check(c1.getRelation(1)==100f,"normal upper bound preserved");
  int size=c1.civGameData.lRelation.size();for(int i=0;i<100;i++){c1.getRelation(-1);c1.setRelation(-1,50f);}check(c1.civGameData.lRelation.size()==size,"negative indices do not grow relation table");
  c1.civGameData.lRelation.set(1,Float.NaN);c2.civGameData.lRelation.set(0,63f);check(c1.getRelation(1)==63f,"saved NaN recovers finite reverse relation");
  c1.civGameData.lRelation.set(1,Float.NaN);c2.civGameData.lRelation.set(0,Float.NaN);check(c1.getRelation(1)==0f&&c2.getRelation(0)==0f,"irrecoverable pair receives finite neutral fallback");
  set(c3,"iNumOfProvinces",1);set(c2,"iNumOfProvinces",0);c1.setRelation(2,50f);c3.setRelation(0,50f);c1.setRelation(1,50f);while(c1.civGameData.lRelation.size()<100)c1.civGameData.lRelation.add(10f);
  BugFixSupport.updateRelations();check(Math.abs(c1.getRelation(2)-49.705f)<.001f,"decay uses live country ID 3 after country 2 eliminated");check(c1.getRelation(1)==50f,"eliminated country skipped");check(c1.civGameData.lRelation.size()==3,"redundant relation tail removed");
  set(c1,"iRankScore",0);set(c3,"iRankScore",0);check(BugFixSupport.finite(DiplomacyManager.getImproveRelation(1,3)),"zero rank pair produces finite improvement");
  check(BugFixSupport.add(2000000000,2000000000)==Integer.MAX_VALUE,"legacy population addition protected");

  float starting=1000f,population=1000000f,growth=.5f,development=.5f,baseGrowth=3f;
  float originalBonus=starting*.00425f*(1f-population/starting)*growth*.7548f*Math.min(development*3.165134f,1f);
  float fixedBonus=starting*.00425f*BugFixSupport.economyRecoveryFactor(1f,population/starting)*growth*.7548f*Math.min(development*3.165134f,1f);
  check(Math.max(99,(int)(99f+baseGrowth+originalBonus))==99,"original recovery reproduces 99 lock at high population");
  check(Math.max(99,(int)(99f+20f+baseGrowth+originalBonus))==99,"original recovery erases active investment");
  check((int)(99f+baseGrowth+fixedBonus)>99,"fixed recovery allows growth above 99 without reducing population");
  check((int)(99f+20f+baseGrowth+fixedBonus)>=119,"fixed recovery preserves investment gain");
  check(BugFixSupport.economyRecoveryFactor(1f,.25f)==.75f,"normal low-population recovery unchanged");
  check(BugFixSupport.economyRecoveryFactor(1f,1f)==0f,"recovery at population threshold unchanged");
  check(BugFixSupport.economyRecoveryFactor(1f,Float.POSITIVE_INFINITY)==0f,"invalid recovery ratio cannot inject non-finite value");
  check((int)(99f-3f+fixedBonus)<99,"patch does not cancel intentional negative base growth");
  System.out.println("ALL PATCH TESTS PASSED");
 }
}
