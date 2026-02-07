// code by jph
package ch.alpine.sonata.hrm;

import java.util.EnumSet;
import java.util.Set;

import ch.alpine.sonata.Natur;
import ch.alpine.sonata.tri.Chord;

/** for match: demands 1 musthave at all times and tolerates any covering */
public class AmbienceEmitter extends ChordEmitter {
  private boolean isStarted; // initialized in super.constructor by calling finish()
  private int beg; // initialized in super.constructor by calling finish()
  private Set<Natur> myTodo; // initialized in super.constructor by calling finish()

  public AmbienceEmitter(Chord chord) {
    super(chord);
  }

  @Override
  public Integer digest(int ticks, Set<Natur> set) {
    if (sustains(set)) {
      if (!isStarted) {
        isStarted = true;
        beg = ticks;
        myTodo.addAll(chord.triad().set());
      }
      myTodo.removeAll(set);
    } else {
      if (isStarted) {
        isStarted = false;
        if (myTodo.isEmpty())
          return beg;
      }
    }
    return null;
  }

  @Override
  public Integer finish() {
    Integer myInteger = isStarted && myTodo.isEmpty() ? beg : null;
    isStarted = false;
    beg = -1;
    myTodo = EnumSet.noneOf(Natur.class);
    return myInteger;
  }

  /** @param set
   * @return true if mySet contains at least one of musthave and is subset of covering */
  private boolean sustains(Set<Natur> set) {
    for (Natur natur : set)
      if (chord.triad().set().contains(natur))
        return chord.covering().containsAll(set);
    return false;
  }
}
