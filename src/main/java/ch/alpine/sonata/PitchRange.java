// code by jph
package ch.alpine.sonata;

import sys.mat.IntRange;

/** Ambitus */
public class PitchRange extends IntRange {
  @SuppressWarnings("unchecked")
  @Override
  protected PitchRange create(int min, int max) {
    return new PitchRange(min, max);
  }

  /** @param min
   * @param max equals upper bound + 1 for inclusion */
  public PitchRange(int min, int max) {
    super(min, max);
  }

  /** only called in EnvelopeRow
   * 
   * @param delta
   * @return */
  public PitchRange incrementWidth(int delta) {
    return getWidth() % 2 == 0 //
        ? new PitchRange(min() - delta, max())
        : new PitchRange(min(), max() + delta);
  }
}
