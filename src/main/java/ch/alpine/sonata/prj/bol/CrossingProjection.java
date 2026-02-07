// code by jph
package ch.alpine.sonata.prj.bol;

import ch.alpine.sonata.Pitch;

/** pitch of hits shall decrease as voices increase
 * does not depend on myTicks */
public class CrossingProjection extends PitchRangeProjection {
  @Override
  public char getChar() {
    return 'x';
  }

  @Override
  boolean requireTest(Pitch pitch) {
    return pitch.isHits();
  }

  @Override
  int getTreshold() {
    return projectionAttributes.crossing_threshold;
  }
}
