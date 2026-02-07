// code by jph
package ch.alpine.sonata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ch.alpine.tensor.num.PrimeQ;
import sys.mat.IntRange;

/** immutable */
public class Division implements Serializable {
  /** upper bound for myList.size() == maxDepth() + 1 */
  // public static final int MAX_DEPTH_LIMIT_HINT = 20;
  public static final Division FALLBACK = new Division(8, 4, 2, 1);
  public static final Division simple4 = new Division(4, 2, 1);
  // ---
  private final List<Integer> list;
  public final int measure;
  public final int maxDepth;
  private final int[] depth;

  public Division(Integer... integers) {
    this(List.of(integers));
  }

  public Division(List<Integer> list) {
    this.list = Collections.unmodifiableList(new ArrayList<>(list));
    measure = list.get(0);
    maxDepth = list.size() - 1;
    // ---
    depth = new int[measure];
    for (int ticks : IntRange.positive(measure))
      depth[ticks] = slow_depthAt(ticks);
    // ---
    consistencyCheck();
  }

  /** check that list is descending and has 1 as last entry */
  private void consistencyCheck() {
    int cmp = Integer.MAX_VALUE; // pack operation might follow
    for (int myInt : list)
      if (myInt < cmp)
        cmp = myInt;
      else
        throw new RuntimeException("division non-decreasing: " + list);
    if (cmp != 1)
      throw new RuntimeException("last entry of division is not 1: " + list);
    // all factors should be prime
    getFactorList().forEach(PrimeQ::require);
  }

  public List<Integer> getList() {
    return new ArrayList<>(list);
  }

  public List<Integer> getFactorList() {
    Iterator<Integer> iterator = list.iterator();
    List<Integer> list = new LinkedList<>();
    int prev = iterator.next();
    while (iterator.hasNext()) {
      int next = iterator.next();
      list.add(prev / next); // TODO NOTATION math
      prev = next;
    }
    return list;
  }

  public int indexOf(int myInt) {
    return list.indexOf(myInt);
  }

  public int get3or4() {
    throw new UnsupportedOperationException();
  }

  /** @param depth
   * @return if depth is negative (measure * 2 ^ depth) */
  public int valueAtSafe(int depth) {
    return depth < 0 ? measure << -depth : list.get(Math.min(depth, maxDepth));
  }

  public int depthAt(int ticks) {
    return depth[Math.floorMod(ticks, measure)];
  }

  /** only called during initialization
   * 
   * @param ticks assumed to be positive
   * @return */
  private int slow_depthAt(int ticks) {
    int depth = 0;
    for (int mod : list) {
      if (ticks % mod == 0)
        return depth;
      ++depth;
    }
    return depth;
  }

  public int modMeasure(int ticks) {
    return Math.floorMod(ticks, measure);
  }

  public int ticksToNextBar(int ticks) {
    return measure - modMeasure(ticks);
  }

  public int leq(int max) {
    for (int myInt : list)
      if (myInt <= max)
        return myInt;
    return 1;
  }

  public int floorToMeasure(int ticks) {
    return ticks - modMeasure(ticks);
  }

  // ---
  @Override
  public int hashCode() {
    return list.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return object instanceof Division division //
        && list.equals(division.list);
  }

  public static String toPlainString(List<Integer> list) {
    return list.toString().replace(",", "").replace("[", "").replace("]", "");
  }

  @Override
  public String toString() {
    return list.toString();
  }

  /** only for testing
   * 
   * @param myFinal
   * @return */
  public static Division fromFactorList(List<Integer> myFinal) {
    List<Integer> myFactors = new ArrayList<>(myFinal);
    Collections.reverse(myFactors);
    List<Integer> list = new ArrayList<>();
    int prod = 1;
    list.add(prod);
    for (int myInt : myFactors) {
      prod *= myInt;
      list.add(prod);
    }
    Collections.reverse(list);
    return new Division(list);
  }

  /** for division=[8 4 2 1] ticks=1 -> 0 ticks=2 -> 1 ticks=3 -> 1 ticks=4 -> 2
   * 
   * @param ticks
   * @return */
  public int log(int ticks) {
    return maxDepth - indexOf(leq(ticks));
  }
}
