// code by jph
package ch.alpine.sonata.prj.bol;

import ch.alpine.sonata.Pitch;

/** pitch of any hit shall not be lower than pitch of following voices up to a margin
 * does not depend on myTicks */
public class HoveringProjection extends PitchRangeProjection {
  @Override
  public char getChar() {
    return 'h';
  }

  @Override
  boolean requireTest(Pitch myPitch) {
    return true;
  }

  @Override
  int getTreshold() {
    return projectionAttributes.hovering_threshold;
  }
}
