package age.of.civilizations2.jakowski.lukasz;
import java.util.List;
/** Compatibility-preserving guards. Does not change serialized fields or IDs. */
public final class BugFixSupport {
 private BugFixSupport() {}
 private static long rejected, recovered, neutral;
 public static int bounded(long value) { return (int)Math.max(Integer.MIN_VALUE,Math.min(Integer.MAX_VALUE,value)); }
 public static int add(int a,int b) { return bounded((long)a+b); }
 /** Low-economy recovery must not become a population-dependent penalty. */
 public static float economyRecoveryFactor(float baseline,float ratio) {
  float factor=baseline-ratio;return finite(factor)?Math.max(0f,factor):0f;
 }
 public static boolean finite(float v) { return !Float.isNaN(v)&&!Float.isInfinite(v); }
 public static long population(Civilization civ,boolean unoccupied) {
  long total=0L;
  for(int i=0;i<civ.getNumOfProvinces();i++) {
   Province province=CFG.game.getProvince(civ.getProvinceID(i));
   if(!unoccupied||!province.isOccupied())total+=(long)province.getPopulationData().getPopulation();
  }
  return total;
 }
 public static float relation(Civilization civ,int index) {
  if(index<0||CFG.game==null||index>=CFG.game.getCivsSize()-1)return 0f;
  List<Float> values=civ.civGameData.lRelation;
  if(index>=values.size())return 0f;
  Float boxed=values.get(index);float value=boxed==null?Float.NaN:boxed.floatValue();
  if(finite(value))return value;
  float replacement=0f; boolean fromReverse=false;
  int reverseIndex=civ.getCivID()-1;
  if(reverseIndex>=0) {
   Civilization other=CFG.game.getCiv(index+1);
   List<Float> reverse=other.civGameData.lRelation;
   if(reverseIndex<reverse.size()&&reverse.get(reverseIndex)!=null&&finite(reverse.get(reverseIndex).floatValue())) {
    replacement=reverse.get(reverseIndex).floatValue();fromReverse=true;
   }
  }
  replacement=Math.max(-100f,Math.min(100f,replacement));values.set(index,Float.valueOf(replacement));
  recovered++;if(!fromReverse)neutral++;
  if(recovered<=12||recovered%1000==0)System.err.println("[AoC2 bugfix] invalid saved relation recovered: civ="+civ.getCivID()+" target="+(index+1)+" reverse="+fromReverse+" recovered="+recovered+" neutral="+neutral);
  return replacement;
 }
 public static void setRelation(Civilization civ,int index,float value) {
  if(index<0||CFG.game==null||index>=CFG.game.getCivsSize()-1)return;
  if(!finite(value)) {if(++rejected<=12||rejected%1000==0)System.err.println("[AoC2 bugfix] non-finite relation rejected: civ="+civ.getCivID()+" target="+(index+1)+" rejected="+rejected);return;}
  List<Float> values=civ.civGameData.lRelation;
  while(values.size()<=index)values.add(Float.valueOf(0f));
  values.set(index,Float.valueOf(Math.max(-100f,Math.min(100f,value))));
 }
 public static void updateRelations() {
  for(int a=1;a<CFG.game.getCivsSize();a++) {
   Civilization civ=CFG.game.getCiv(a);
   int length=CFG.game.getCivsSize()-1;
   List<Float> values=civ.civGameData.lRelation;
   if(values.size()>length)values.subList(length,values.size()).clear();
   for(int k=0;k<Math.min(length,values.size());k++) {
    Float v=values.get(k);if(v==null||!finite(v.floatValue()))relation(civ,k);
   }
   if(civ.getNumOfProvinces()<=0)continue;
   for(int b=a+1;b<CFG.game.getCivsSize();b++) {
    if(CFG.game.getCiv(b).getNumOfProvinces()<=0)continue;
    decay(a,b);decay(b,a);
   }
  }
 }
 private static void decay(int a,int b) {
  float value=CFG.game.getCivRelation_OfCivB(a,b);
  if(value>30f)CFG.game.setCivRelation_OfCivB(a,b,value-.295f);
  else if(value< -20f&&!CFG.game.getCivsAtWar(a,b))CFG.game.setCivRelation_OfCivB(a,b,value+.0145f);
 }
}
