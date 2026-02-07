// code by jph
package ch.alpine.sonata.mid.cmp;

import java.util.Collection;

import sys.mat.IntegerMath;

public class SwingHelper {
  final int mod;
  final int range;

  public SwingHelper(int mod, int range) {
    this.mod = mod;
    this.range = range;
  }

  public boolean allClear(Collection<Integer> collection) {
    return IntegerMath.gcd(collection) % mod == 0;
  }

  public int getRyhtmDelta(int ticks) {
    if (ticks % mod != 0)
      throw new RuntimeException();
    ticks %= range;
    return ticks == 0 ? 0 : (range - ticks);
  }
}
