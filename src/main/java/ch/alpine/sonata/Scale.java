// code by jph
package ch.alpine.sonata;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** methods are final to prevent confusion
 * 
 * immutable */
public class Scale implements Serializable {
  private final Map<Natur, DiatoneAlter> map;
  private final Set<DiatoneAlter> set;

  public Scale(Map<Natur, DiatoneAlter> map) {
    this.map = Collections.unmodifiableMap(new EnumMap<>(map));
    set = map.values().stream().collect(Collectors.toSet());
  }

  /** @param pitch
   * @return
   * @throws Exception if pitch is not represented in scale */
  public final Ivory getIvory(int pitch) {
    Natur natur = Natur.fromPitch(pitch);
    DiatoneAlter diatoneAlter = map.get(natur);
    int white = pitch - diatoneAlter.alter().delta();
    int octave = Math.floorDiv(white, 12);
    int ivory = diatoneAlter.diatone().ordinal();
    return Ivory.from(octave * 7 + ivory);
  }

  /** @param pitch
   * @return
   * @throws Exception if pitch is not represented in scale */
  public final Tone getTone(int pitch) {
    Ivory ivory = getIvory(pitch);
    return Tone.from(ivory, pitch - ivory.white());
  }

  public final DiatoneAlter getDiatoneAlter(Natur natur) {
    return Objects.requireNonNull(map.get(natur));
  }

  public final boolean containsExact(DiatoneAlter diatoneAlter) {
    return set.contains(diatoneAlter);
  }

  public final Map<Natur, DiatoneAlter> map() {
    return map;
  }

  public final int size() {
    return map.size();
  }

  @Override
  public final int hashCode() {
    return map.hashCode();
  }

  @Override
  public final boolean equals(Object object) {
    return object instanceof Scale scale //
        && map.equals(scale.map);
  }
}
