// code by jph
package ch.alpine.sonata;

import ch.alpine.sonata.utl.TpfStatics;

/** Harmony is used while loading the StackBuilder database */
public enum Harmony {
  DOM0, // tonic, tonika
  DOM1, // dominant, dominante
  DOM2, // supertoic
  DOM3, // subMediant
  DOM4, // mediant
  DOM5, // leading note
  SUB6, // DOM6
  SUB5, //
  SUB4, //
  SUB3, //
  SUB2, //
  SUB1; // subdominant, subdominante
  // ---

  /** 0 1 2 3 4 5 -6 -5 -4 -3 -2 -1 */
  private final int type;
  /** 0 -5 2 -3 4 -1 -6 1 -4 3 -2 5 */
  private final int tonic;

  private Harmony() {
    tonic = TpfStatics.invert(ordinal());
    type = TpfStatics.invert(tonic);
  }

  public Harmony ascend(int delta) { // i + 1 -> v
    return fromType(type() + delta);
  }

  public Harmony prev() {
    return ascend(-1);
  }

  public static Harmony fromType(int type) {
    return values()[Math.floorMod(type, 12)];
  }

  public String toInfoString() {
    return String.format("%s, type=%3d, tonic=%3d", toString(), type, tonic);
  }

  public int type() {
    return type;
  }

  public int tonic() {
    return tonic;
  }
}
