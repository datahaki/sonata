// code by jph
package ch.alpine.sonata.utl;

import java.util.Optional;

import ch.alpine.sonata.Voice;
import ch.alpine.sonata.scr.Score;
import sys.dat.MapOperations;

public enum Timeshift {
  ;
  /** shifts all torrents in voice by ticks
   * 
   * @param myVoice
   * @param ticks */
  public static void by(Voice myVoice, int ticks) {
    MapOperations.translate(myVoice.navigableMap, ticks);
    MapOperations.translate(myVoice.shake, ticks);
    MapOperations.translate(myVoice.press, ticks);
    MapOperations.translate(myVoice.lyric, ticks);
    MapOperations.translate(myVoice.fbass, ticks);
    MapOperations.translate(myVoice.motif, ticks);
  }

  /** shifts all torrents in score by ticks
   * 
   * @param myScore
   * @param ticks */
  public static void by(Score myScore, int ticks) {
    for (Voice myVoice : myScore.voices)
      by(myVoice, ticks);
    // ---
    MapOperations.translate(myScore.triad, ticks);
    MapOperations.translate(myScore.text, ticks);
  }

  /** removes blank measures at beginning of score
   * 
   * @param myScore
   * @return */
  public static int advanceEntry(final Score myScore) {
    int ticks = 0;
    Optional<Integer> myOptional = myScore.entry();
    if (myOptional.isPresent()) {
      ticks = myScore.division.floorToMeasure(myOptional.get());
      if (0 < ticks) {
        by(myScore, -ticks);
        myScore.process.append("/entry-=" + ticks);
      }
    }
    return ticks;
  }
}
