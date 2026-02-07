// code by jph
package ch.alpine.sonata.prj.bol;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.alpine.sonata.ScoreArray;
import ch.alpine.sonata.scr.Score;
import sys.mat.IntRange;

/** ScoreImages holds all the information to perform the projections on a {@link ScoreArray}.
 * 
 * relevant for Order1Div and Contains projections, because of score.division
 * but also for Boundary because of clef in voices */
public final class ScoreImages {
  public final Score myScore; // voice.clef, transpose, division
  public final ScoreArray scoreArray;
  private final List<Integer> myTicks;

  public ScoreImages(Score score, ScoreArray scoreArray, List<Integer> myTicks) {
    this.myScore = score;
    this.scoreArray = scoreArray;
    this.myTicks = myTicks;
  }

  /** @param ticks represents either index of myTicks list or real ticks in score
   * @return */
  public final int mapToScoreTicks(int ticks) {
    return isAbsolute() ? ticks : myTicks.get(ticks);
  }

  public boolean isAbsolute() {
    return Objects.isNull(myTicks);
  }

  /** @param score
   * @param scoreArray complete or only part of score (!)
   * @param ticks_offset
   * @return */
  public static ScoreImages create(Score score, ScoreArray scoreArray, int ticks_offset) {
    List<Integer> myTicks = new ArrayList<>();
    for (int ticks : IntRange.positive(scoreArray.ticks()))
      myTicks.add(ticks_offset + ticks);
    return new ScoreImages(score, scoreArray, myTicks);
  }

  @Override
  public String toString() {
    return "scoreimage [" + scoreArray.ticks() + "] " + myTicks;
  }
}
