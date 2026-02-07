// code by jph
package ch.alpine.sonata.utl;

import java.util.NavigableMap;
import java.util.SortedMap;

import ch.alpine.sonata.Division;
import ch.alpine.sonata.Joint;
import ch.alpine.sonata.ScoreArray;
import ch.alpine.sonata.jnt.ScoreJoint;
import ch.alpine.sonata.jnt.ScoreJoint1;
import ch.alpine.sonata.scr.Score;

public enum Rhythm {
  ;
  private static int relax(NavigableMap<Integer, Joint> myNavigableMap, int beg, int end, int index, Division myDivision) {
    int myInt = 0;
    int delta = myDivision.valueAtSafe(index);
    for (int ticks = beg; ticks < end; ticks += delta) {
      SortedMap<Integer, Joint> mySortedMap = myNavigableMap.subMap(ticks, ticks + delta);
      switch (mySortedMap.size()) {
      case 0:
        break;
      case 1: {
        int entry = mySortedMap.firstKey();
        if (entry != ticks) {
          myNavigableMap.put(ticks, myNavigableMap.remove(entry));
          ++myInt;
        }
        break;
      }
      default:
        myInt += relax(myNavigableMap, ticks, ticks + delta, index + 1, myDivision);
      }
    }
    return myInt;
  }

  /** requires updated division list
   * 
   * @param myScore
   * @return */
  public static int relax(Score myScore) {
    int myInt = relax(myScore, 0, myScore.ticks());
    if (0 < myInt)
      myScore.process.append("/relax=" + myInt);
    return myInt;
  }

  public static int relax(Score score, int beg, int end) {
    if (score.division.modMeasure(beg) != 0)
      throw new RuntimeException("relax only works on entire measures");
    ScoreArray myScoreArray = ScoreOps.create(score);
    ScoreJoint myScoreJoint = new ScoreJoint1(myScoreArray, 0, myScoreArray.ticks());
    int myInt = relax(myScoreJoint.navigableMap, beg, end, 0, score.division);
    if (0 < myInt) {
      ScoreArray myRelaxed = ScoreJoint1.toScoreArray(myScoreJoint.navigableMap, myScoreArray.voices(), myScoreArray.ticks());
      new ScoreMerge(myRelaxed).updateVoices(score);
    }
    return myInt;
  }
}
