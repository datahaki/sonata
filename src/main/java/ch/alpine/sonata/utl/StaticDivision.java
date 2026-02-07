// code by jph
package ch.alpine.sonata.utl;

import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import ch.alpine.sonata.Division;
import ch.alpine.sonata.Divisions;
import ch.alpine.sonata.ScoreArray;
import ch.alpine.sonata.scr.Score;
import sys.mat.IntRange;

// ??? many functions require 5, 7
public enum StaticDivision {
  ;
  // ??? redundant to SA ?
  private static NavigableMap<Integer, Integer> getHitCount(ScoreArray myScoreArray) {
    // myScoreArray.getHitEntries().stream() //
    // .map(se -> se.ticks) //
    // .collect(Collectors.groupingBy(e -> e, () -> new TreeMap<Integer, Integer>(), Collectors.counting()));
    // Collectors.groupingBy
    NavigableMap<Integer, Integer> myNavigableMap = new TreeMap<>();
    for (int c1 : IntRange.positive(myScoreArray.ticks())) {
      int count = 0;
      for (int c0 : IntRange.positive(myScoreArray.voices()))
        count += myScoreArray.getPitch(c0, c1) != null && myScoreArray.getPitch(c0, c1).isHits() ? 1 : 0;
      if (0 < count)
        myNavigableMap.put(c1, count);
    }
    return myNavigableMap;
  }

  public static Division estimateDivision(Score score, int measure, List<String> myDebug) {
    List<Division> myDivisionList = Divisions.all(measure);
    int[] myInt = new int[measure];
    ScoreArray myScoreArray = ScoreOps.create(score);
    for (Entry<Integer, Integer> myEntry : getHitCount(myScoreArray).entrySet())
      myInt[myEntry.getKey() % measure] += myEntry.getValue();
    double cmp = 0;
    int index = 0;
    int count = 0;
    for (Division myDivision : myDivisionList) {
      int sum = 0;
      int den = 0;
      for (int ticks : IntRange.positive(measure)) {
        int wgt = myDivision.maxDepth - myDivision.depthAt(ticks);
        if (wgt < 0)
          System.out.println("WGT NO GOOD");
        sum += wgt * myInt[ticks];
        den += wgt;
      }
      double res = sum / (double) den;
      myDebug.add(myDivision + " " + res);
      if (cmp < res) {
        cmp = res;
        index = count;
      }
      ++count;
    }
    return myDivisionList.get(index);
  }
}
