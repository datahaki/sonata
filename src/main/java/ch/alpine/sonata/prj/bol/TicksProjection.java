// code by jph
package ch.alpine.sonata.prj.bol;

import java.util.NavigableMap;
import java.util.TreeMap;

public abstract class TicksProjection<Type> extends Projection {
  public final NavigableMap<Integer, Type> navigableMap = new TreeMap<>();

  @Override
  public final void update(ScoreImages scoreImages) {
    navigableMap.clear();
    protected_update(scoreImages, 0, scoreImages.scoreArray.ticks());
  }

  @Override
  public final void update(ScoreImages scoreImages, int beg, int end) {
    navigableMap.subMap(beg, end).clear();
    final int max = scoreImages.scoreArray.ticks();
    navigableMap.tailMap(max).clear();
    // ---
    protected_update(scoreImages, beg, end);
  }

  protected abstract void protected_update(ScoreImages scoreImages, int beg, int end);

  @Override
  public final boolean pass() {
    return !navigableMap.values().stream() //
        .anyMatch(myType -> !passValue(myType)); // short circuit
  }

  @Override
  public boolean pass(int ticks) {
    return !navigableMap.containsKey(ticks) || passValue(navigableMap.get(ticks));
  }

  /** function is invoked with value myType of myNavigableMap
   * to quantify if value is permitted by projection
   * 
   * @param myType
   * @return */
  protected abstract boolean passValue(Type myType);

  /** function only used for rendering
   * 
   * @param ticks
   * @return value of myNavigableMap at ticks */
  public abstract int getInteger(int ticks);
}
