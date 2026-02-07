// code by jph
package ch.alpine.sonata;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/** immutable, hashCode and equals are from Object */
public class Tone implements Serializable {
  private static final List<Tone> LIST = Ivory.all().stream() //
      .flatMap(ivory -> Stream.of(Alter.values()) //
          .map(alter -> new Tone(ivory, alter))) //
      // TODO TPF consider filtering out tones with pitch out of range
      .toList();

  /** @param ivory
   * @param alter */
  public static Tone from(Ivory ivory, Alter alter) {
    return LIST.get(ivory.ivory() * 5 + alter.ordinal());
  }

  public static Tone from(Ivory ivory, int delta) {
    return from(ivory, Alter.fromDelta(delta));
  }

  public static Tone from(int ivory, int delta) {
    return from(Ivory.from(ivory), delta);
  }

  public static Tone from(Diatone diatone, Alter alter, int octave) {
    return from(Ivory.from(diatone.ordinal() + octave * 7), alter);
  }

  public static List<Tone> all() {
    return LIST;
  }

  // ---
  private final Ivory ivory;
  /** 60 is middle c, 62 is d above that c ... */
  private final int pitch;
  private final DiatoneAlter diatoneAlter;

  private Tone(Ivory ivory, Alter alter) {
    this.ivory = ivory;
    pitch = ivory.white() + alter.delta();
    diatoneAlter = DiatoneAlter.from(ivory.diatone(), alter);
  }

  public Ivory ivory() {
    return ivory;
  }

  public int pitch() {
    return pitch;
  }

  public DiatoneAlter diatoneAlter() {
    return diatoneAlter;
  }

  public Tone transposeByOctaves(int octaves) {
    return from(ivory.transposeByOctaves(octaves), diatoneAlter.alter());
  }

  @Override
  public int hashCode() {
    return Objects.hash(ivory, diatoneAlter);
  }

  @Override
  public boolean equals(Object object) {
    return object instanceof Tone tone //
        && ivory.equals(tone.ivory) //
        && diatoneAlter.equals(tone.diatoneAlter);
  }

  @Override
  public String toString() {
    return diatoneAlter.toString() + ivory.octave();
  }
}
