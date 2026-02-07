// code by jph
package ch.alpine.sonata;

import java.util.Map;

import ch.alpine.tensor.ext.Integers;
import sys.mat.IntRange;

/** scale with complete {@link Natur} to {@link Tone} map */
public class DodecatonicScale extends Scale {
  protected static final int SIZE = 12;
  private static final IntRange VALID_BIAS = IntRange.closed(0, 5);

  /** all 12 keys have note associations thus transposing along scale is identical to transpose by pitch
   * 
   * other constructors are possible, in order to customize how accidental for outside of diatonic are chosen
   * total 5 choices, so input boolean[5]
   * but regardless: a rigid 12-note scale generally does not satisfy the needs
   * 
   * @param type between -7, -6, ..., 6, 7
   * @param bias between 0, 1, ..., 5. Best choice for J.S.Bach is 4. */
  public static DodecatonicScale chromatic(int type, int bias) {
    DodecatonicScale dodecatonicScale = new DodecatonicScale(CircleFifths.subMap(type - 1 + bias - 5, SIZE));
    VALID_BIAS.requireInside(bias);
    return dodecatonicScale;
  }

  public DodecatonicScale(Map<Natur, DiatoneAlter> map) {
    super(map);
    Integers.requireEquals(size(), SIZE);
  }
}
