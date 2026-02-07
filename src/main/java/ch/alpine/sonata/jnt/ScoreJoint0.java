// code by jph
package ch.alpine.sonata.jnt;

import java.util.Map;
import java.util.stream.Collectors;

import ch.alpine.sonata.Joint;
import ch.alpine.sonata.ScoreArray;
import sys.mat.IntRange;

public class ScoreJoint0 extends ScoreJoint {
  public ScoreJoint0(ScoreArray scoreArray, int beg, int end) {
    this(scoreArray, beg, end, IntRange.positive(scoreArray.voices()));
  }

  public ScoreJoint0(ScoreArray scoreArray, int beg, int end, IntRange myIntRange) {
    for (int ticks : scoreArray.getHits(beg, end, myIntRange))
      navigableMap.put(ticks, new Joint(myIntRange.stream() //
          .mapToObj(voice -> scoreArray.getRelation0(voice, ticks))));
  }

  /** reduce first before calling getPitchMap
   * 
   * @return */
  public Map<Integer, Integer> getPitchMap() {
    // TODO TPF this is simply the frequency/occurrence of all pitches
    return navigableMap.values().stream() //
        .flatMap(Joint::stream) //
        .collect(Collectors.groupingBy( //
            myRelation -> myRelation.integer0, //
            Collectors.reducing(0, _ -> 1, Integer::sum)));
  }
}
