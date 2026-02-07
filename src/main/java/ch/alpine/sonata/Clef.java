// code by jph
package ch.alpine.sonata;

import java.util.Collections;
import java.util.EnumSet;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public enum Clef {
  SOPRANINO('G', 13),
  FRENCH('G', 8), // ly, xml
  TREBLE('G', 6), // ly, xml center=B
  SOPRANO('C', 4), // ly, xml center=G
  MEZZOSOPRANO('C', 2), // ly center=E
  ALTO('C', 0), // ly, xml center=C
  TENOR('C', -2), // ly, xml center=A
  BARITONE('C', -4), // ly, xml center=F
  BASS('F', -6), // ly, xml center=D
  SUBBASS('F', -8), // ly
  WOOFER('F', -13);

  // ---
  public static final Set<Clef> piano_sheet = EnumSet.of(TREBLE, BASS);
  public static final Set<Clef> piano_extended = EnumSet.of(TREBLE, ALTO, BASS);
  public static final Set<Clef> satb = EnumSet.of(SOPRANO, ALTO, TENOR, BASS);
  // ---
  public final char sign;
  public final Ivory center_ivory;
  public final int ivory_helperline_lower;
  public final int ivory_helperline_upper;
  public final SortedSet<Integer> helperLines;

  /** @param sign as in MusicXML
   * @param ivory of center line */
  private Clef(char sign, int ivory) {
    this(sign, 35 + ivory, 35 + ivory - 6, 35 + ivory + 6);
  }

  /** @param lower
   * @param upper typically equals lower+36
   * @param ivory_helperline_lower
   * @param ivory_helperline_upper typically equals ivory_helperline_lower+12 */
  private Clef(char sign, int center_ivory, int ivory_helperline_lower, int ivory_helperline_upper) {
    this.sign = sign;
    this.center_ivory = Ivory.from(center_ivory);
    this.ivory_helperline_lower = ivory_helperline_lower;
    this.ivory_helperline_upper = ivory_helperline_upper;
    // ---
    NavigableSet<Integer> navigableSet = new TreeSet<>();
    for (int c0 = ivory_helperline_lower + 2; c0 < ivory_helperline_upper; c0 += 2)
      navigableSet.add(c0);
    helperLines = Collections.unmodifiableSortedSet(navigableSet);
  }

  /** @param sign
   * @param line
   * @return not null even if input is invalid */
  public static Clef fromSignLineSafe(String sign, int line) {
    return Optional.ofNullable(fromSignLine(sign, line)).orElse(ALTO);
  }

  static Clef fromSignLine(String sign, int line) {
    if (sign.equalsIgnoreCase("G"))
      switch (line) {
      case 1:
        return FRENCH;
      case 2:
      default:
        return TREBLE;
      }
    else //
    if (sign.equalsIgnoreCase("C"))
      switch (line) {
      case 1:
        return SOPRANO;
      case 2:
        return MEZZOSOPRANO;
      case 4:
        return TENOR;
      case 5:
        return BARITONE;
      case 3:
      default:
        return ALTO;
      }
    else //
    if (sign.equalsIgnoreCase("F"))
      switch (line) {
      case 5:
        return SUBBASS;
      case 4:
      default:
        return BASS;
      }
    return null;
  }
}
