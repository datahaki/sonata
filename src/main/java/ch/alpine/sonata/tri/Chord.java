// code by jph
package ch.alpine.sonata.tri;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import ch.alpine.sonata.Natur;

public class Chord implements Serializable {
  private final Triad triad;
  /** set of Natur perceived as "compatible" with myTriad */
  private final Set<Natur> covering;

  public Chord(Triad triad, Collection<Natur> collection) {
    this.triad = triad;
    covering = Collections.unmodifiableSet(EnumSet.copyOf(collection));
    // ---
    if (!covering.containsAll(triad.set()))
      throw new RuntimeException(); // "chord is not covered"
  }

  public Chord(Triad triad) {
    this(triad, triad.set());
  }

  public Triad triad() {
    return triad;
  }

  public Set<Natur> covering() {
    return covering;
  }

  /** deprecated */
  public boolean hasUnisonFrom(Chord chord) {
    for (Natur natur : triad.set())
      if (chord.triad.set().contains(natur))
        return true;
    return false;
  }

  /** deprecated */
  public boolean hasSemitoneFrom(Chord chord) {
    for (Natur natur : triad.set())
      if (chord.triad.set().contains(natur.ascend(1)) || chord.triad.set().contains(natur.ascend(-1)))
        return true;
    return false;
  }

  @Override
  public String toString() {
    Set<Natur> cov = EnumSet.copyOf(covering);
    cov.removeAll(triad.set());
    return triad.toString() + " + " + cov;
  }

  @Override
  public int hashCode() {
    return Objects.hash(triad, covering);
  }

  @Override
  public boolean equals(Object object) {
    return object instanceof Chord chord //
        && triad.equals(chord.triad) //
        && covering.equals(chord.covering);
  }
}
