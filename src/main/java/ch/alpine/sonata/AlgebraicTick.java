// code by jph
package ch.alpine.sonata;

import ch.alpine.tensor.Rational;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.sca.Mod;

/** fractional location in score
 * the denominator is score.measure
 * for instance 10+1/3 means measure 10 + 1/3 measure */
// FIXME TPF math correct!? documentation, tests!
public class AlgebraicTick implements Comparable<AlgebraicTick> {
  public static AlgebraicTick mark(Scalar fraction) {
    return new AlgebraicTick(fraction, -1);
  }

  /** absolute ticks */
  public final Scalar fraction;
  public final int depth;

  private AlgebraicTick(Scalar fraction, int depth) {
    this.fraction = fraction;
    this.depth = depth;
  }

  public AlgebraicTick(int ticks, Division division) {
    this(Rational.of(ticks, division.measure), division.depthAt(ticks));
  }

  public boolean equalsModMeasure(AlgebraicTick algebraicTick) {
    Mod mod = Mod.function(1);
    return mod.apply(fraction).equals(mod.apply(algebraicTick.fraction)) //
        && depth == algebraicTick.depth;
  }

  public Scalar getTicks(Division division) {
    return fraction.multiply(RealScalar.of(division.measure));
  }

  @Override
  public int compareTo(AlgebraicTick algebraicTick) {
    return Scalars.compare(fraction, algebraicTick.fraction);
  }

  @Override
  public String toString() {
    return fraction + "@" + depth;
  }
}
