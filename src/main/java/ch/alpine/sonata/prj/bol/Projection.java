// code by jph
package ch.alpine.sonata.prj.bol;

import ch.alpine.sonata.PitchRange;

public abstract class Projection {
  /** ScoreModel is only required for {@link PitchingProjection} for access to {@link PitchRange},
   * and also {@link BoundingProjection},
   * but in the future all configurable parameters are handed via this mechanism. */
  protected ProjectionAttributes projectionAttributes = null;

  /** @param projectionAttributes */
  public void setAttributes(ProjectionAttributes projectionAttributes) {
    this.projectionAttributes = projectionAttributes;
  }

  /** when projection instance is used on different scores
   * use of this function helps to avoid errors
   * 
   * @param scoreImages */
  public abstract void update(ScoreImages scoreImages);

  /** @param scoreImages
   * @param beg is first index in myScoreImages.myScoreArray,
   * for instance myScoreImages.myScoreArray.getInteger(beg) is meaningful
   * @param end */
  public abstract void update(ScoreImages scoreImages, int beg, int end);

  public abstract boolean pass();

  public abstract boolean pass(int ticks);
}
