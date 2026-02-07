// code by jph
package ch.alpine.sonata.utl;

import java.util.ArrayList;
import java.util.List;

import ch.alpine.sonata.Voice;
import ch.alpine.sonata.jnt.ScoreArrays;
import ch.alpine.sonata.prj.bol.BooleanProjection;
import ch.alpine.sonata.prj.bol.CrossingProjection;
import ch.alpine.sonata.prj.bol.ProjectionAttributes;
import ch.alpine.sonata.prj.bol.ScoreImages;
import ch.alpine.sonata.scr.Score;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.io.Primitives;
import ch.alpine.tensor.lie.Permutations;

/** determines permutation of voices with the smallest crossing */
public enum Shuffler {
  ;
  private static final int IntPermutations_max = 6;

  public static int arrange(Score score, List<Integer> myResult) {
    int ticks = score.ticks();
    int voices = score.voices();
    int cmp = Integer.MAX_VALUE;
    int index = 0;
    if (voices < IntPermutations_max) {
      int count = 0;
      for (List<Integer> list : get(voices)) {
        List<Voice> myVoices = new ArrayList<>();
        for (int myInt : list)
          myVoices.add(score.voices.get(myInt));
        ScoreImages scoreImages = new ScoreImages(score, ScoreArrays.create(myVoices, ticks), null);
        BooleanProjection booleanProjection = new CrossingProjection();
        booleanProjection.setAttributes(new ProjectionAttributes());
        booleanProjection.update(scoreImages);
        int value = booleanProjection.navigableMap.size();
        if (myResult != null)
          myResult.add(value);
        if (value < cmp) {
          cmp = value;
          index = count;
        }
        ++count;
      }
    }
    return index;
  }

  public static boolean arrange(Score score) {
    boolean myBoolean = false;
    int voices = score.voices();
    if (voices < IntPermutations_max) {
      int index = arrange(score, null);
      myBoolean = 0 < index;
      if (myBoolean) {
        List<Voice> myVoices = new ArrayList<>();
        for (int myInt : get(voices).get(index))
          myVoices.add(score.voices.get(myInt));
        score.voices = myVoices;
        score.process.append("/permute");
      }
    }
    return myBoolean;
  }

  public static List<List<Integer>> get(int index) {
    return Permutations.of(Range.of(0, index)).stream() //
        .map(Primitives::toListInteger) //
        .toList();
  }
}
