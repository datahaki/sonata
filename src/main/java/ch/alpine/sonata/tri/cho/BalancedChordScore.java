// code by jph
package ch.alpine.sonata.tri.cho;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ch.alpine.sonata.Natur;
import ch.alpine.sonata.Torrent;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.itp.BSplineBasis;

public class BalancedChordScore extends ChordScore {
  static final double spread = 1 / 12.;
  // ---
  private final int order;
  private final ScalarUnaryOperator spline;
  final double amplification;

  /** @param order of sophistication
   * @param channel */
  public BalancedChordScore(int order, int channel) {
    super(channel);
    this.order = order;
    spline = BSplineBasis.of(order);
    amplification = spline.apply(RealScalar.ZERO).reciprocal().number().doubleValue();
  }

  private double eval(int pitch) {
    return spline.apply(Rational.of(pitch - pitch_mean, 12)).number().doubleValue() * amplification;
  }

  @Override
  public void put(int ticks, int duration, Set<Natur> set, int velocity) {
    List<Torrent> list = new LinkedList<>();
    for (Natur natur : set) {
      int mod = Math.floorMod(natur.ordinal() - pitch_mean, 12);
      for (int d = -order - 1; d <= order; ++d) { // this is not optimized
        int pitch = pitch_mean + mod + 12 * d;
        double weight = eval(pitch);
        add(list, pitch, duration, weight * velocity);
      }
    }
    append(list, ticks, duration);
  }
}
