// code by jph
package ch.alpine.sonata.hrm;

import java.util.Set;

import ch.alpine.sonata.Natur;
import ch.alpine.sonata.tri.Chord;

/** for match: demands all musthave and tolerates any covering
 * musthave <= set <= covering */
public class InstanceEmitter extends InvariantEmitter {
  public InstanceEmitter(Chord chord) {
    super(chord);
  }

  @Override
  boolean sustains(Set<Natur> set) {
    return set.containsAll(chord.triad().set()) && chord.covering().containsAll(set);
  }
}
