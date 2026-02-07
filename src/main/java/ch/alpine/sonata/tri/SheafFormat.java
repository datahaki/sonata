// code by jph
package ch.alpine.sonata.tri;

import ch.alpine.sonata.Harmony;
import ch.alpine.sonata.KeyMode;
import ch.alpine.sonata.KeySignature;
import ch.alpine.sonata.Natur;
import ch.alpine.sonata.utl.TpfStatics;

/** Upon changing the {@link SheafFormat}, CadenceStatic rebuilds the CadenceDatabase. */
public class SheafFormat extends TriadFormat {
  private static final String[] roman = new String[] { "i", "v", "ii", "vi", "iii", "vii", "iv" };
  private static final String[] subdom = new String[] { "sh", "sp", "sq", "st", "sd", "sm", "dz", "dm", "dd", "dt", "dq", "dp" };

  private static String getRoman(int type) {
    return roman[Math.floorMod(type, roman.length)];
  }

  public final KeyMode keyMode; // here to add value to ChordFormat

  public SheafFormat(KeySignature keySignature, KeyMode keyMode) {
    this.keyMode = keyMode;
    final int ordinal = keyMode.ordinal();
    int count = 0;
    for (Harmony harmony : Harmony.values()) {
      final int type = harmony.type();
      final String myString;
      if (TpfStatics.mod12(type + ordinal) < 7) {
        myString = getRoman(count);
        ++count; // hack
      } else
        myString = subdom[type + 6];
      final int _pitch = keySignature.tonic() + keyMode.tonic + harmony.tonic();
      Natur pitch = Natur.fromPitch(_pitch);
      put(Triad.major(pitch), myString.toUpperCase());
      put(Triad.minor(pitch), myString.toLowerCase());
      put(Triad.diminished(pitch), myString.toUpperCase() + 'o');
      put(Triad.majorSixth(pitch), myString.toUpperCase() + '6');
    }
    for (int type : new int[] { -1, 0, 1 }) {
      int _pitch = keySignature.tonic() + keyMode.tonic + type * 7;
      Natur pitch = Natur.fromPitch(_pitch);
      put(Triad.diminishedSeventh(pitch), getRoman(type).toUpperCase() + "o7");
    }
    for (int type : new int[] { -1, 0, 1, 2 }) {
      int _pitch = keySignature.tonic() + keyMode.tonic + type * 7;
      Natur pitch = Natur.fromPitch(_pitch);
      put(Triad.augmented(pitch), getRoman(type).toUpperCase() + "+");
    }
    check();
  }
}
