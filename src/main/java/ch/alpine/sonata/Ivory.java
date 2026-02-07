// code by jph
package ch.alpine.sonata;

import java.io.Serializable;
import java.util.List;
import java.util.stream.IntStream;

/** ivory is the index of white on the white keys of the midi pitch scale keyboard
 * 
 * immutable */
public class Ivory implements Serializable {
  public static final int SIZE = 75;
  private static final int MOD = 7;
  private static final List<Ivory> LIST = IntStream.range(0, SIZE).mapToObj(Ivory::new).toList();

  /** default constructor
   * 
   * @param ivory between 0 and 74
   * @return */
  public static Ivory from(int ivory) {
    return LIST.get(ivory);
  }

  public static List<Ivory> all() {
    return LIST;
  }

  // ---
  /** Middle C is 0. All C's are {..., -7, 0, 7, ...}. */
  private final int ivory;
  /** 0, 1, 2, 3, ... */
  private final int octave;
  private final Diatone diatone;
  /** pitch of white key that is used for reference for instance c# has white=0, and d& has white=2 */
  private final int white;

  private Ivory(int ivory) {
    this.ivory = ivory;
    octave = Math.floorDiv(ivory, MOD);
    diatone = Diatone.values()[Math.floorMod(ivory, MOD)];
    white = octave * 12 + diatone.white_delta();
  }

  public Ivory add(int delta) {
    return from(ivory + delta);
  }

  // TODO TPF misnomer
  public int ivory() {
    return ivory;
  }

  public int octave() {
    return octave;
  }

  public Diatone diatone() {
    return diatone;
  }

  public int white() {
    return white;
  }

  public Ivory transposeByOctaves(int octaves) {
    return from(ivory + octaves * MOD);
  }

  @Override
  public int hashCode() {
    return ivory;
  }

  @Override
  public boolean equals(Object object) {
    return object instanceof Ivory ivory //
        && hashCode() == ivory.ivory;
  }
}
