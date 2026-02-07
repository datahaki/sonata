// code by jph
package ch.alpine.sonata.hrm;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import ch.alpine.sonata.Natur;
import ch.alpine.sonata.Pitch;
import ch.alpine.sonata.ScoreArray;
import ch.alpine.sonata.tri.Chord;
import ch.alpine.sonata.tri.Triad;
import sys.mat.IntRange;

public class ChordLookup {
  private final Map<Set<Natur>, Triad> map = new HashMap<>();

  public ChordLookup(Theory theory) {
    for (Triad triad : Triad.values()) {
      put(triad.set(), triad);
      // ---
      { // add single note from covering to musthave
        Chord chord = theory.getChord(triad);
        Set<Natur> diff = EnumSet.copyOf(chord.covering());
        diff.removeAll(triad.set());
        for (Natur natur : diff) {
          Set<Natur> set = EnumSet.copyOf(triad.set());
          set.add(natur);
          put(set, triad);
        }
      }
      // ---
      // TODO TPF add 2 notes if available
    }
    // System.out.println("" + map.size());
  }

  private void put(Set<Natur> set, Triad triad) {
    if (map.containsKey(set))
      System.out.println("warning double " + set + " " + triad);
    map.put(set, triad);
  }

  public Optional<Triad> matchesTriad(Set<Natur> set) {
    return Optional.ofNullable(map.get(set));
  }

  public NavigableMap<Integer, Triad> getTriadMap(ScoreArray scoreArray, boolean repeat) {
    NavigableMap<Integer, Triad> navigableMap = new TreeMap<>();
    Triad myPrev = null;
    for (int ticks : IntRange.positive(scoreArray.ticks())) {
      Set<Natur> set = EnumSet.noneOf(Natur.class);
      for (int voice : IntRange.positive(scoreArray.voices())) {
        Pitch myPitch = scoreArray.getPitchUnsafe(voice, ticks);
        if (Objects.nonNull(myPitch))
          set.add(Natur.fromPitch(myPitch.pitch()));
      }
      Optional<Triad> optional = matchesTriad(set);
      if (optional.isPresent()) {
        Triad triad = optional.get();
        if (repeat || !triad.equals(myPrev)) {
          navigableMap.put(ticks, triad);
          myPrev = triad;
        }
      }
    }
    return navigableMap;
  }
}
