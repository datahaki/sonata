// code by jph
package ch.alpine.sonata;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/** immutable */
public class Pitch implements Serializable {
  private static final int MAX = 128;
  private static final List<Pitch> array0 = IntStream.range(0, MAX).mapToObj(pitch -> new Pitch(pitch, false)).toList();
  private static final List<Pitch> array1 = IntStream.range(0, MAX).mapToObj(pitch -> new Pitch(pitch, true)).toList();

  public static Pitch from(int pitch, boolean hits) {
    return hits //
        ? array1.get(pitch)
        : array0.get(pitch);
  }

  // ---
  private final int pitch;
  private final boolean hits;
  private final int uniqueId; // value also used for comparison/ordering

  private Pitch(int pitch, boolean hits) {
    this.pitch = pitch;
    this.hits = hits;
    uniqueId = -pitch * 2 + (hits ? 1 : 0);
  }

  public int pitch() {
    return pitch;
  }

  public boolean isHits() {
    return hits;
  }

  /** @param _pitch
   * @return true if myPitch != null and both pitch values match */
  public boolean equalsPitch(Pitch _pitch) {
    return Objects.nonNull(_pitch) //
        && pitch() == _pitch.pitch();
  }

  public Pitch withHits(boolean hits) {
    return from(pitch(), hits);
  }

  public Pitch withPitch(int pitch) {
    return from(pitch, isHits());
  }

  public int toCompareInt() {
    // TODO TPF bad style, comparator should reveal rationale explicitly
    return uniqueId;
  }

  @Override
  public int hashCode() {
    return uniqueId;
  }

  @Override
  public boolean equals(Object object) {
    return object instanceof Pitch pitch //
        && uniqueId == pitch.uniqueId;
  }

  @Override
  public String toString() {
    return pitch() + (isHits() ? "." : "");
  }
}
