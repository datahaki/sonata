// code by jph
package ch.alpine.sonata.scr;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import ch.alpine.sonata.Clef;
import ch.alpine.sonata.ScoreEntry;
import ch.alpine.sonata.Voice;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.sca.Ceiling;
import sys.mat.IntRange;

public enum Scores {
  ;
  public static <Type> void shiftScoreMap(SortedMap<ScoreEntry, Type> sortedMap, int beg, int end) {
    int delta = end - beg;
    Map<ScoreEntry, Type> map = new HashMap<>(sortedMap.tailMap(ScoreEntry.ticksMark(beg)));
    map.keySet().forEach(sortedMap::remove);
    map.entrySet().stream() //
        .filter(entry -> beg <= entry.getKey().ticks() + delta) //
        .forEach(entry -> sortedMap.put(entry.getKey().shift(delta), entry.getValue()));
  }

  /** removes all entries with key greater-equals beg;
   * then reenters ...
   * 
   * @param sortedMap
   * @param beg
   * @param end */
  static <Type> void shiftMap(SortedMap<Integer, Type> sortedMap, int beg, int end) {
    int delta = end - beg;
    Map<Integer, Type> map = new HashMap<>(sortedMap.tailMap(beg));
    map.keySet().forEach(sortedMap::remove);
    map.entrySet().stream() //
        .filter(entry -> beg <= entry.getKey() + delta) //
        .forEach(entry -> sortedMap.put(entry.getKey() + delta, entry.getValue()));
  }

  public static Score create(final int voices) { // use voice=0 for adaptive scores
    Score score = new Score();
    int semi = Ceiling.intValueExact(RationalScalar.of(voices, 2));
    for (int _voice : IntRange.positive(voices)) {
      Voice voice = new Voice();
      if (_voice < semi) {
        voice.clef = Clef.TREBLE;
        // TODO TPF chould use stylist for stem, otherwise redundant
        // myVoice.myStem = voice < new Fraction(semi, 2).ceil() ? Stem.UP : Stem.DN;
      } else {
        voice.clef = Clef.BASS;
        // myVoice.myStem = voice - semi < new Fraction(voices - semi, 2).ceil() ? Stem.UP : Stem.DN;
      }
      score.voices.add(voice);
    }
    score.staffPartition = Array.same(RealScalar.ONE, voices).toString();
    return score;
  }
}
