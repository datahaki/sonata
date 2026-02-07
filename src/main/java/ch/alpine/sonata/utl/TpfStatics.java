// code by jph
package ch.alpine.sonata.utl;

import ch.alpine.bridge.lang.SI;
import ch.alpine.tensor.Scalar;

public enum TpfStatics {
  ;
  /** inverse to itself, because 7*7 = 49 mod 12 = 1 */
  public static int invert(int value) {
    return Math.floorMod(value * 7 + 6, 12) - 6;
  }

  public static String friendlyTime(Scalar value) {
    int seconds = SI.SECONDS.intValue(value);
    return String.format("%2d:%02d", seconds / 60, seconds % 60);
  }

  /** @param pitch
   * @return */
  public static int mod12(int pitch) {
    return Math.floorMod(pitch, 12);
  }
}
