// code by jph
package ch.alpine.sonata.tri;

import java.util.HashMap;
import java.util.Map;

import ch.alpine.sonata.Harmony;
import ch.alpine.sonata.KeyMode;
import ch.alpine.sonata.KeySignature;
import ch.alpine.sonata.Natur;

public class HarmonicType {
  private final Map<Triad, Integer> map = new HashMap<>();

  public HarmonicType(KeySignature keySignature, KeyMode keyMode) {
    for (Harmony harmony : Harmony.values()) {
      final int type = harmony.type();
      final int value = type;
      int _pitch = keySignature.tonic() + keyMode.tonic + harmony.tonic();
      Natur natur = Natur.fromPitch(_pitch);
      put(Triad.major(natur), value);
      put(Triad.minor(natur), value);
      put(Triad.diminished(natur), value);
      put(Triad.majorSixth(natur), value);
    }
    for (int type : new int[] { -1, 0, 1 }) {
      int _pitch = keySignature.tonic() + keyMode.tonic + type * 7;
      Natur natur = Natur.fromPitch(_pitch);
      put(Triad.diminishedSeventh(natur), type); // why uppercase!?
    }
    for (int type : new int[] { -1, 0, 1, 2 }) {
      int _pitch = keySignature.tonic() + keyMode.tonic + type * 7;
      Natur natur = Natur.fromPitch(_pitch);
      put(Triad.augmented(natur), type);
    }
  }

  private void put(Triad triad, int myInt) {
    if (map.containsKey(triad))
      throw new RuntimeException("duplicate triad");
    map.put(triad, myInt);
  }

  public Integer getType(Triad triad) {
    return map.get(triad);
  }
}
