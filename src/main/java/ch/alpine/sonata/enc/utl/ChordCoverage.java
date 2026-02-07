// code by jph
package ch.alpine.sonata.enc.utl;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import ch.alpine.sonata.KeySignature;
import ch.alpine.sonata.Natur;
import ch.alpine.sonata.hrm.ChordEmitter;
import ch.alpine.sonata.tri.Chord;
import ch.alpine.sonata.utl.ScoreNatur;

public class ChordCoverage implements Serializable {
  public final ChordEmitter chordEmitter;
  public final NavigableMap<Integer, Integer> navigableMap = new TreeMap<>(); // beg -> end // this could be Set<TicksRange> ...

  public ChordCoverage(ChordEmitter chordEmitter) {
    this.chordEmitter = chordEmitter;
  }

  public void updateMap(KeySignature keySignature, ScoreNatur scoreNatur, int ticks_total) {
    navigableMap.clear();
    for (Entry<Integer, Set<Natur>> entry : scoreNatur.navigableMap.entrySet()) {
      int ticks = entry.getKey();
      Set<Natur> set = entry.getValue();
      Integer myInteger = chordEmitter.digest(ticks, set);
      if (Objects.nonNull(myInteger))
        put(myInteger, ticks);
    }
    Integer myInteger = chordEmitter.finish();
    if (Objects.nonNull(myInteger))
      put(myInteger, ticks_total);
  }

  private void put(int beg, int end) {
    if (end <= beg)
      throw new RuntimeException("chord coverage has invalid entry: " + beg + " => " + end);
    navigableMap.put(beg, end);
  }

  public Entry<Integer, Integer> coveringEntry(int ticks) {
    Entry<Integer, Integer> entry = navigableMap.lowerEntry(ticks + 1);
    return Objects.nonNull(entry) && ticks < entry.getValue() ? entry : null;
  }

  public Chord getChord() {
    return chordEmitter.getChord();
  }
}
