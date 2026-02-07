// code by jph
package ch.alpine.sonata;

public enum Alter {
  FLAT2("$"),
  FLAT("&"),
  NEUTRAL(""),
  SHARP("#"),
  SHARP2("x");

  public static final String flatUnicode = "\u266D";
  public static final String sharpUnicode = "\u266F";
  public static final String neutralUnicode = "\u266E";
  private static final String sharp2Unicode = "\u00D7";
  /** other unicode characters related to accidentals are
   * x=\u00D7 multiplication sign
   * x=\u2A2F cross prod */
  private static final String[] ACCIDENTAL_Unicode = { "$", flatUnicode, "", sharpUnicode, sharp2Unicode }; // double sharp \u266F\u266F
  private static final String[] ACCIDENTAL_Absolute = { "$", "&", neutralUnicode, "#", sharp2Unicode }; // double sharp \u266F\u266F
  private static final String[] ACCIDENTAL_AbsoluteUnicode = { "$", flatUnicode, neutralUnicode, sharpUnicode, sharp2Unicode }; // double sharp \u266F\u266F

  /** @param delta between -2 and +2
   * @return */
  public static Alter fromDelta(int delta) {
    return values()[2 + delta];
  }

  // ---
  private final String accidental;

  Alter(String accidental) {
    this.accidental = accidental;
  }

  public int delta() {
    return ordinal() - 2;
  }

  public boolean isNeutral() {
    return equals(NEUTRAL);
  }

  public String getAccidental() {
    return accidental;
  }

  public String getAccidentalUnicode() {
    return ACCIDENTAL_Unicode[ordinal()];
  }

  public String getAccidentalAbsolute() {
    return ACCIDENTAL_Absolute[ordinal()];
  }

  public String getAccidentalAbsoluteUnicode() {
    return ACCIDENTAL_AbsoluteUnicode[ordinal()];
  }
}
