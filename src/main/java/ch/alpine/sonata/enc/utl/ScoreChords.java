// code by jph
package ch.alpine.sonata.enc.utl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ch.alpine.sonata.KeySignature;
import ch.alpine.sonata.hrm.AmbienceEmitter;
import ch.alpine.sonata.hrm.InstanceEmitter;
import ch.alpine.sonata.hrm.MusthaveEmitter;
import ch.alpine.sonata.hrm.Theory;
import ch.alpine.sonata.tri.Chord;
import ch.alpine.sonata.tri.Triad;
import ch.alpine.sonata.utl.ScoreNatur;
import ch.alpine.tensor.ext.Integers;

public class ScoreChords {
  public final Map<Chord, ChordCoverage> map = new HashMap<>();

  // ---
  public static ScoreChords ambience(Theory theory) {
    ScoreChords scoreChords = new ScoreChords();
    for (Chord chord : theory)
      scoreChords.map.put(chord, new ChordCoverage(new AmbienceEmitter(chord)));
    Integers.requireEquals(scoreChords.map.size(), Triad.values().length);
    return scoreChords;
  }

  public static ScoreChords instance(Theory theory) {
    ScoreChords scoreChords = new ScoreChords();
    for (Chord chord : theory)
      scoreChords.map.put(chord, new ChordCoverage(new InstanceEmitter(chord))); // TODO TPF this is better done with ChordLookup
    return scoreChords;
  }

  public static ScoreChords musthave(Theory theory) {
    ScoreChords scoreChords = new ScoreChords();
    for (Chord chord : theory)
      scoreChords.map.put(chord, new ChordCoverage(new MusthaveEmitter(chord)));
    return scoreChords;
  }

  public void updateCoverage(KeySignature keySignature, ScoreNatur scoreNatur, int ticks_total) {
    for (ChordCoverage chordCoverage : map.values())
      chordCoverage.updateMap(keySignature, scoreNatur, ticks_total);
  }

  public int numel() {
    int myInt = 0;
    for (ChordCoverage chordCoverage : map.values())
      myInt += chordCoverage.navigableMap.size();
    return myInt;
  }

  public boolean isCovering(int ticks, Chord chord) {
    return Objects.nonNull(map.get(chord).coveringEntry(ticks));
  }
}
