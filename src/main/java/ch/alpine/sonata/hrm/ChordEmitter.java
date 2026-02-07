// code by jph
package ch.alpine.sonata.hrm;

import java.io.Serializable;
import java.util.Set;

import ch.alpine.sonata.Natur;
import ch.alpine.sonata.tri.Chord;

public abstract class ChordEmitter implements Serializable {
  protected final Chord chord;

  public ChordEmitter(Chord chord) {
    this.chord = chord;
    finish(); // mandatory call to initialize state variables
  }

  public final Chord getChord() {
    return chord;
  }

  public abstract Integer digest(int ticks, Set<Natur> set);

  /** finishes the current feed and resets {@link ChordEmitter}
   * 
   * @return what digest would return at this point of feed */
  public abstract Integer finish();
}
