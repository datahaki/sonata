// code by jph
package ch.alpine.sonata.utl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import ch.alpine.sonata.Division;
import ch.alpine.sonata.Metric;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.Torrent;
import ch.alpine.sonata.Voice;
import ch.alpine.sonata.scr.Score;
import sys.dat.MapOperations;

public enum Timescale {
  ;
  /** modification of myScore.division is not handled, but has to be managed outside function
   * 
   * @param myScore
   * @param gcd */
  static void pack(final Score myScore, int gcd) {
    for (Voice myVoice : myScore.voices)
      pack(myVoice, gcd);
    // ---
    myScore.quarter /= gcd;
    myScore.process.append("/pack=" + gcd);
    // final Fraction myFraction = new Fraction(1, gcd);
    for (Voice myVoice : myScore.voices) {
      MapOperations.scaleMapSafe(myVoice.shake, gcd);
      MapOperations.scaleMapSafe(myVoice.press, gcd);
      MapOperations.scaleMapSafe(myVoice.lyric, gcd);
      MapOperations.scaleMapSafe(myVoice.fbass, gcd);
      MapOperations.scaleMapSafe(myVoice.motif, gcd);
    }
    MapOperations.scaleMapSafe(myScore.triad, gcd);
    MapOperations.scaleMapSafe(myScore.text, gcd);
  }

  private static void pack(final Voice voice, final int gcd) {
    NavigableMap<Integer, Torrent> myNavigableMap = new TreeMap<>();
    for (Entry<Integer, Torrent> myEntry : voice)
      myNavigableMap.put(myEntry.getKey() / gcd, pack(myEntry.getValue(), gcd));
    voice.navigableMap = myNavigableMap;
  }

  private static Torrent pack(final Torrent myTorrent, final int gcd) {
    for (Note note : myTorrent)
      note.setTicks(note.ticks() / gcd);
    return myTorrent;
  }

  public static void stretch(Score score, Integer... factors) {
    stretch(score, List.of(factors));
  }

  /** @param score is final, as the changes are made to the object itself
   * @param factors */
  static void stretch(final Score score, List<Integer> factors) {
    for (int gcd : factors)
      if (1 < gcd) {
        List<Integer> list = new ArrayList<>();
        for (int myInt : score.division.getList())
          list.add(myInt * gcd);
        list.add(1);
        score.division = new Division(list);
        for (Voice myVoice : score.voices)
          stretchNotes(myVoice, gcd);
        score.quarter *= gcd;
        MapOperations.scaleMap(score.triad, gcd);
        MapOperations.scaleMap(score.text, gcd);
      }
    score.process.append("/stretch=" + factors);
  }

  public static void stretchNotes(final Voice voice, final int gcd) {
    NavigableMap<Integer, Torrent> myNavigableMap = new TreeMap<>();
    for (Entry<Integer, Torrent> myEntry : voice)
      myNavigableMap.put(myEntry.getKey() * gcd, stretch(myEntry.getValue(), gcd));
    voice.navigableMap = myNavigableMap;
    MapOperations.scaleMap(voice.shake, gcd);
    MapOperations.scaleMap(voice.press, gcd);
    MapOperations.scaleMap(voice.lyric, gcd);
    MapOperations.scaleMap(voice.fbass, gcd);
    MapOperations.scaleMap(voice.motif, gcd);
  }

  private static Torrent stretch(final Torrent myTorrent, final int gcd) {
    for (Note note : myTorrent)
      note.setTicks(note.ticks() * gcd);
    return myTorrent;
  }

  public static boolean canStretch(Score score, int factor) {
    return Metric.measuresList.contains(score.measure() * factor) //
        && Metric.quartersList.contains(score.quarter * factor);
  }
}
