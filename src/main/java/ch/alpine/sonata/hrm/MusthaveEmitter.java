// code by jph
package ch.alpine.sonata.hrm;

import java.util.Set;

import ch.alpine.sonata.Natur;
import ch.alpine.sonata.tri.Chord;

/** for match: demands equality with musthave; additional notes are not permitted */
public class MusthaveEmitter extends InvariantEmitter {
  public MusthaveEmitter(Chord chord) {
    super(chord);
  }

  /** Set.equals() compares the specified object with this set for equality.
   * Returns true if the specified object is also a set,
   * the two sets have the same size,
   * and every member of the specified set is contained in this set
   * (or equivalently, every member of this set is contained in the specified set). */
  @Override
  public boolean sustains(Set<Natur> set) {
    return set.equals(chord.triad().set());
  }
}
