// code by jph
package ch.alpine.sonata;

public enum Suffix {
  DECR("-"), // & b
  NONE(""),
  INCR("+"); // # 6\

  public final String string;

  private Suffix(String string) {
    this.string = string;
  }

  /** @return chromatic delta */
  public int delta() {
    return ordinal() - 1;
  }

  public String getAppendix() {
    return string;
  }

  public static Suffix fromAbbreviation(char chr) {
    switch (chr) {
    case '+':
    case '#': // non-native: stage2
    case '/': // non-native: stage2
    case '\\': // non-native: stage2
      return INCR;
    case '-':
    case 'f': // non-native: stage2
      return DECR;
    default:
      return null;
    }
  }

  /** throws exception if delta out of range
   * 
   * @param delta
   * @return */
  public static Suffix fromDelta(int delta) {
    return values()[delta + 1];
  }
}
