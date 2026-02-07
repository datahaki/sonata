// code by jph
package ch.alpine.sonata.enc.utl;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

import ch.alpine.sonata.hrm.Theory;
import ch.alpine.sonata.tri.Chord;
import ch.alpine.sonata.tri.Triad;
import sys.mat.IntRange;

/** bundle preserves all information */
public class CoverageBundle implements Serializable {
  /** ticks -> map (chord_end, ChordCoverage)
   * 
   * suitable for use of subMap */
  public final NavigableMap<Integer, NavigableMap<Integer, List<ChordCoverage>>> navigableMap = new TreeMap<>();

  public CoverageBundle(ScoreChords scoreChords) {
    for (ChordCoverage chordCoverage : scoreChords.map.values())
      for (Entry<Integer, Integer> entry : chordCoverage.navigableMap.entrySet()) {
        int ticks = entry.getKey();
        if (!navigableMap.containsKey(ticks))
          navigableMap.put(ticks, new TreeMap<>());
        int end = entry.getValue();
        if (!navigableMap.get(ticks).containsKey(end))
          navigableMap.get(ticks).put(end, new LinkedList<>());
        // if (myNavigableMap.get(ticks).containsKey(end))
        // new RuntimeException("duplicate chord of same duration!").printStackTrace();
        navigableMap.get(ticks).get(end).add(chordCoverage);
      }
  }

  /** @param ticks
   * @return position lower or equal than ticks from which ticks is covered, or null if no cover is detected */
  public Integer isCoveredAt(final int ticks) {
    Integer myInteger = ticks + 1;
    for (@SuppressWarnings("unused")
    int __ : IntRange.positive(3)) {
      myInteger = navigableMap.lowerKey(myInteger);
      if (Objects.isNull(myInteger))
        break;
      if (ticks < navigableMap.get(myInteger).lastKey())
        return myInteger;
    }
    return null;
  }

  public Integer getEnd() {
    return navigableMap.isEmpty() ? null : Collections.max(navigableMap.lastEntry().getValue().keySet());
  }

  /** @param myTriadFormat
   * @return maps ticks -> longest lasting chord starting at ticks */
  public NavigableMap<Integer, Triad> projectOnsets() {
    NavigableMap<Integer, Triad> projection = new TreeMap<>();
    for (Entry<Integer, NavigableMap<Integer, List<ChordCoverage>>> entry : navigableMap.entrySet())
      projection.put(entry.getKey(), entry.getValue().lastEntry().getValue().get(0).getChord().triad());
    return projection;
  }

  // public Vector<ChordArea> getListing() {
  // Vector<ChordArea> myVector = new Vector<ChordArea>();
  // for (Entry<Integer, NavigableMap<Integer, ChordCoverage>> myEntry : myNavigableMap.entrySet())
  // for (Entry<Integer, ChordCoverage> myChordEntry : myEntry.getValue().entrySet())
  // myVector.add(new ChordArea(myEntry.getKey(), myChordEntry.getKey(), myChordEntry.getValue().getChord()));
  // return myVector;
  // }
  /** joins adjacent identical map entries to interval entry
   * 
   * @param triadMap
   * @return */
  public static CoverageBundle fromChords(NavigableMap<Integer, Triad> triadMap) {
    ScoreChords scoreChords = ScoreChords.ambience(Theory.uniform);
    if (!triadMap.isEmpty()) {
      Integer ticks = triadMap.firstKey();
      while (Objects.nonNull(ticks)) {
        Triad triad = triadMap.get(ticks);
        int count = ticks + 1;
        while (triadMap.containsKey(count) && triadMap.get(count).equals(triad))
          ++count;
        Chord chord = Theory.uniform.getChord(triad);
        if (scoreChords.map.containsKey(chord)) {
          scoreChords.map.get(chord).navigableMap.put(ticks, count);
        } else {
          System.out.println("chord not in map: " + chord);
          break;
        }
        ticks = triadMap.higherKey(count - 1);
      }
    }
    return new CoverageBundle(scoreChords);
  }
}
