// code by jph
package ch.alpine.sonata.prj.bol;

import java.util.Objects;

import ch.alpine.sonata.Pitch;
import ch.alpine.sonata.ScoreArray;
import sys.mat.IntRange;

public abstract class PitchRangeProjection extends BooleanProjection {
  @Override
  protected final void protected_update(ScoreImages scoreImages, int beg, int end) {
    ScoreArray scoreArray = scoreImages.scoreArray;
    int threshold = getTreshold();
    for (IntRange intRange : projectionAttributes.getVoicePartition(scoreArray.voices()))
      if (1 < intRange.getWidth())
        for (int ticks : scoreArray.getHits(beg, end, intRange)) {
          Integer myValue = null;
          int pitch_last = 0;
          for (int voice : intRange) {
            Pitch pitch = scoreArray.getPitch(voice, ticks);
            if (pitch != null && requireTest(pitch)) {
              myValue = Objects.isNull(myValue) //
                  ? 0
                  : Math.max(myValue, pitch.pitch() - pitch_last);
              pitch_last = pitch.pitch();
            }
          }
          if (myValue != null && threshold < myValue)
            addViolationAt(ticks);
        }
  }

  abstract boolean requireTest(Pitch pitch);

  abstract int getTreshold();
}
