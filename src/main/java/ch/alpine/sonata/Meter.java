// code by jph
package ch.alpine.sonata;

import java.io.Serializable;
import java.util.List;

import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.ext.Integers;
import sys.mat.Ratio;

/** the denominator has to be of the form 2^n (requirement also in MIDI)
 * 
 * quarter => implies measure
 * 
 * let q = r * 2 ^ p then m = r * f, where f is any
 * 
 * equation: m / (4 * q) = n / 2 ^ k */
public record Meter(int num, int den) implements Serializable {
  public static Meter of(int quarter, Division division) {
    Ratio ratio = Ratio.of(RationalScalar.of(division.measure, 4 * quarter));
    // ---
    int index = division.maxDepth;
    if (division.getList().contains(quarter))
      index = division.indexOf(quarter);
    int divider = divider(division.getFactorList().subList(0, index));
    ratio = ratio.mulBoth(1 << divider);
    return standardize(ratio, quarter, division.measure);
  }

  /** @param num
   * @param den */
  public static Meter of(Ratio ratio, int quarter, int measure) {
    return standardize(ratio, quarter, measure);
  }

  public static Meter of(Ratio ratio) {
    return new Meter( //
        ratio.num(), //
        ratio.den());
  }

  private static Meter standardize(Ratio ratio, int quarter, int measure) {
    int bas = measure <= quarter * 4 //
        ? 4
        : (measure <= quarter * 8 ? 2 : 1);
    if (bas % ratio.den() == 0) {
      int fac = bas / ratio.den();
      ratio = ratio.mulBoth(fac);
    }
    return Meter.of(ratio);
  }

  /** @param mySubdivision
   * @return first index of factor !=2, or 0 */
  static int divider(List<Integer> list) {
    int divider = 0;
    for (int factor : list)
      if (factor == 2)
        ++divider;
      else
        return divider;
    return 0;
  }

  public Meter(int num, int den) {
    this.num = Integers.requirePositive(num);
    this.den = Integers.requirePowerOf2(den);
  }

  public int getMetronomeHint() {
    if (num % 3 == 0)
      return num % 2 == 0 //
          ? 2
          : 3;
    return 4;
  }

  @Override
  public String toString() {
    return num + "/" + den;
  }
}
