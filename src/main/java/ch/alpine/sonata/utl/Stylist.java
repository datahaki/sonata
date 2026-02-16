// code by jph
package ch.alpine.sonata.utl;

import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.alpine.sonata.Clef;
import ch.alpine.sonata.Voice;
import ch.alpine.sonata.scr.Score;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.sca.Ceiling;
import sys.mat.IntRange;
import sys.mat.Ratio;

public enum Stylist {
  ;
  public static void defaultStyle(Score score) {
    Map<Clef, List<Integer>> myMap = new EnumMap<>(Clef.class);
    // ---
    // assign clefs
    {
      int voice = 0;
      for (Voice myVoice : score.voices) {
        assignBestClef(myVoice, Clef.piano_sheet);
        // the default choice applies only to non-empty voices
        // if (!defaultStyle(0, myVoice))
        // TODO TPF
        // myVoice.myClef = voice < (myScore.voices() + 1) / 2 ? Clef.TREBLE : Clef.BASS; // assignment in case voice is empty
        if (!myMap.containsKey(myVoice.clef))
          myMap.put(myVoice.clef, new LinkedList<>());
        myMap.get(myVoice.clef).add(voice);
        ++voice;
      }
    }
    // assign stems (depends on clef assignment
    for (List<Integer> myList : myMap.values()) {
      int split = Ceiling.intValueExact(Rational.of(myList.size(), 2));
      for (int voice : IntRange.positive(split)) {
        int index = myList.get(voice);
        // myScore.myVoices.get(index).myStem = Stem.UP; // FIXME TPF
      }
      for (int voice : new IntRange(split, myList.size())) { // = split; voice < myList.size(); ++voice
        int index = myList.get(voice);
        // myScore.myVoices.get(index).myStem = Stem.DN; // FIXME TPF
      }
    }
  }

  public static void assignBestClef(Voice myVoice, Collection<Clef> collection) {
    if (myVoice.navigableMap.isEmpty())
      return;
    // ---
    Ratio ratio = myVoice.ivoryNotesRatio();
    double cmp = 100;
    // TODO TPF use ArgMin on list!
    Clef myBest = collection.iterator().next();
    for (Clef clef : collection) {
      double dif = Math.abs(clef.center_ivory.ivory() - ratio.toDouble());
      if (dif < cmp) {
        myBest = clef;
        cmp = dif;
      }
    }
    myVoice.clef = myBest;
  }

  /** for values of measure above 2, such as 12/4
   * 
   * @param score */
  public static void reasonableMeter(Score score) {
    int fac = 1;
    while (fac * 2 * 4 * score.quarter < score.measure())
      fac *= 2;
    if (1 < fac) {
      // System.out.println("reasonableMeter " + myScore.title + " " + fac);
      score.quarter *= fac;
      score.process.append("/quarter*=" + fac);
    }
  }
}
