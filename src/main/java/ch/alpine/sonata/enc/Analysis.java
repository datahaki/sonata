// code by jph
package ch.alpine.sonata.enc;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableSet;

import ch.alpine.sonata.Ornament;
import ch.alpine.sonata.ScoreArray;
import ch.alpine.sonata.Torrent;
import ch.alpine.sonata.Voice;
import ch.alpine.sonata.scr.Score;
import ch.alpine.sonata.scr.VoiceBuffer;
import ch.alpine.sonata.utl.ScoreOps;
import sys.mat.IntRange;

public enum Analysis {
  ;
  /** asserts that torrent are not overlapping in one voice
   * 
   * @param score */
  public static void assertTicksValid(Score score) {
    for (Voice myVoice : score.voices) {
      VoiceBuffer voiceBuffer = new VoiceBuffer();
      myVoice.getNoteMapAbsolute().entrySet().forEach( //
          myEntry -> voiceBuffer.put(myEntry.getValue(), false, myEntry.getKey()));
    }
  }

  public static void assertEntryOnTicks(Score score) {
    int voice = 0;
    for (Voice myVoice : score.voices) {
      NavigableSet<Integer> myNavigableSet = (NavigableSet<Integer>) myVoice.getNoteMapAbsolute().keySet();
      {
        List<Integer> myList = new LinkedList<>(myVoice.shake.keySet());
        for (int ticks : myList)
          if (!myNavigableSet.contains(ticks)) {
            int lower = myNavigableSet.lower(ticks);
            Ornament myOrnament = myVoice.shake.remove(ticks);
            myVoice.shake.put(lower, myOrnament);
            // System.out.println("voice " + voice + " shake off beat " + ticks + " " + myVoice.shake.get(ticks));
            // System.out.println(myNavigableSet);
          }
        // Collections.indexOfSubList(source, target)
      }
      {
        List<Integer> list = new LinkedList<>(myVoice.shake.keySet());
        for (int ticks : list)
          if (!myNavigableSet.contains(ticks)) {
            // int lower = myNavigableSet.lower(ticks);
            // Ornament myOrnament = myVoice.shake.remove(ticks);
            // myVoice.shake.put(lower, myOrnament);
            System.out.println("voice " + voice + " lyric off beat " + ticks + " " + myVoice.lyric.get(ticks));
            System.out.println(myNavigableSet);
          }
      }
      // {
      // if (!myNavigableSet.containsAll(myVoice.press.keySet()))
      // System.out.println("voice " + voice + " dynamics off beat ");
      // }
      // myBoolean &= mySet.containsAll(myVoice.shake.keySet());
      // myBoolean &= mySet.containsAll(myVoice.lyric.keySet());
      ++voice;
    }
    // if (!myBoolean)
    // System.out.println("press or shake inconsistent in "+myScore.title);
  }

  public static boolean isVoiceFlattened(Voice voice) {
    int last = -1;
    for (Entry<Integer, Torrent> myEntry : voice.navigableMap.entrySet()) {
      if (last == myEntry.getKey())
        return false;
      last = myEntry.getKey() + myEntry.getValue().ticks();
    }
    return true;
  }

  /** computes the number instances with more than 5 voices among all instances
   * 
   * @param myScore
   * @return */
  public static double moreThan5Voices(Score myScore) {
    if (myScore.voices() < 6)
      return 0;
    ScoreArray scoreArray = ScoreOps.create(myScore);
    int voices = scoreArray.voices();
    // ?? more efficient using scoreJoint
    NavigableSet<Integer> navigableMap = scoreArray.getHits();
    int total = 0;
    for (int c1 : navigableMap) {
      int count = 0;
      for (int c0 : IntRange.positive(voices))
        if (scoreArray.getPitch(c0, c1) != null)
          ++count;
      if (5 < count)
        ++total;
    }
    return total / (double) navigableMap.size();
  }
}
