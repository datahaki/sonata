// code by jph
package ch.alpine.sonata.hrm;

import java.util.Set;

import ch.alpine.sonata.Natur;
import ch.alpine.sonata.tri.Chord;

abstract class InvariantEmitter extends ChordEmitter {
  private boolean isStarted;
  private int beg;

  InvariantEmitter(Chord chord) {
    super(chord);
  }

  abstract boolean sustains(Set<Natur> set);

  @Override
  public final Integer digest(int ticks, Set<Natur> set) {
    if (sustains(set)) {
      if (!isStarted) {
        isStarted = true;
        beg = ticks;
      }
    } else {
      if (isStarted) {
        isStarted = false;
        return beg;
      }
    }
    return null;
  }

  @Override
  public final Integer finish() {
    Integer myInteger = isStarted ? beg : null;
    isStarted = false;
    beg = -1;
    return myInteger;
  }
}
