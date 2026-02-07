// code by jph
package ch.alpine.sonata.utl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import ch.alpine.sonata.Division;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.ScoreArray;
import ch.alpine.sonata.Voice;
import ch.alpine.sonata.scr.Score;
import ch.alpine.sonata.scr.VoiceBuffer;
import sys.mat.IntRange;

public enum Duplicates {
  ;
  public static boolean hasEmptyVoice(Score score) {
    return score.voices.stream().filter(myVoice -> myVoice.navigableMap.isEmpty()).findAny().isPresent();
  }

  public static void removeEmptyVoices(Score score) {
    score.voices = score.voices.stream().filter(myVoice -> !myVoice.navigableMap.isEmpty()).collect(Collectors.toList());
  }

  /** computes how much each voice is contained in any other voice
   * 
   * @param score
   * @return */
  public static double[][] redundant(Score score) {
    ScoreArray myScoreArray = ScoreOps.create(score);
    int voices = myScoreArray.voices();
    double[][] myDouble = new double[voices][voices];
    int[] myInt = new int[voices];
    Set<Integer> myNavigableSet = myScoreArray.getHits();
    for (int c0 : IntRange.positive(voices)) {
      for (int c1 : myNavigableSet)
        if (myScoreArray.getPitch(c0, c1) != null)
          ++myInt[c0];
      // ---
      for (int v0 : IntRange.positive(voices))
        if (c0 != v0 && 0 < myInt[c0]) {
          for (int c1 : myNavigableSet)
            if (myScoreArray.getPitch(c0, c1) != null && myScoreArray.getPitch(v0, c1) != null
                && TpfStatics.mod12(myScoreArray.getPitch(c0, c1).pitch() - myScoreArray.getPitch(v0, c1).pitch()) == 0)
              ++myDouble[c0][v0];
          myDouble[c0][v0] /= myInt[c0];
        }
    }
    return myDouble;
  }

  public static void remove(final Score score) {
    double[][] myDouble = redundant(score);
    List<Integer> myList = new LinkedList<>();
    int voices = score.voices();
    for (int c0 = voices - 1; 0 <= c0; --c0) {
      boolean drop = false;
      for (int c1 : IntRange.positive(voices))
        if (!myList.contains(c1))
          drop |= Math.abs(myDouble[c0][c1] - 1) < 1e-4;
      if (drop)
        myList.add(c0);
    }
    if (!myList.isEmpty()) {
      for (int index : myList)
        score.voices.remove(index);
      Collections.reverse(myList);
      score.process.append("/drop=" + Division.toPlainString(myList));
      System.out.println("here");
    }
  }

  public static int removeDuplicateNotes(Score score, final int voice) {
    return removeDuplicateNotes(score, voice, IntRange.positive(score.ticks()));
  }

  public static int removeDuplicateNotes(Score score, final int voice, IntRange myIntRange) {
    List<Map<Integer, Note>> myList = new LinkedList<>();
    for (int count : IntRange.positive(score.voices()))
      if (count != voice)
        myList.add(myIntRange.capMap(score.voices.get(count).getNoteMapAbsolute()));
    VoiceBuffer myVoiceBuffer = new VoiceBuffer();
    Voice myVoice = score.voices.get(voice);
    int count = 0;
    for (Entry<Integer, Note> myEntry : myVoice.getNoteMapAbsolute().entrySet()) {
      int ticks = myEntry.getKey();
      Note note = myEntry.getValue();
      boolean myBoolean = false;
      for (Map<Integer, Note> myMap : myList)
        myBoolean |= myMap.containsKey(ticks) && myMap.get(ticks).equals(note);
      if (!myBoolean)
        myVoiceBuffer.put(note, false, ticks);
      else
        ++count;
    }
    myVoice.navigableMap.clear();
    if (!myVoiceBuffer.isEmpty())
      // helps to preserve press/motif
      myVoice.navigableMap.putAll(myVoiceBuffer.getVoice().navigableMap);
    return count;
  }
}
