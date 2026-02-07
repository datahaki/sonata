// code by jph
package ch.alpine.sonata;

import ch.alpine.sonata.utl.TpfStatics;

public enum KeyMode {
  LYDIAN, // F B C F \lydian
  IONIAN, // C F G C \ionian (major/dur)
  // ---
  /** Mixolydian:
   * "Gelobest seist Du Jesu Christ" BWV 314 & BWV 91.6 (verified)
   * "Dies sind die heiligen zehn Gebote" BWV 298 (verified)
   * "Du sollst Gott, deinen Herren lieben" BWV 77.1 */
  MIXOLYDIAN, // G C D G \mixolydian
  DORIAN, // D G A D \dorian
  AEOLIAN, // A D E A \aeolian (minor/moll)
  /** Phrygian:
   * "Aus tiefer Not schrei ich zu dir" BWV 38
   * "Es woll uns Gott genaedig sein" in " Die Himmel erzaehlen die Ehre Gottes" BWV 76 */
  PHRYGIAN, // E A B E \phrygian
  LOCRIAN; // B E F B \locrian

  public final int type;
  public final int tonic;

  private KeyMode() {
    type = ordinal() - 1;
    tonic = TpfStatics.invert(type);
  }

  public String getShortName() {
    String string = toString();
    return string.substring(0, string.length() - 3) + '.';
  }

  public static KeyMode fromModernName(String string) {
    if (string.equalsIgnoreCase("major"))
      return IONIAN;
    if (string.equalsIgnoreCase("minor"))
      return AEOLIAN;
    return null;
  }
}
