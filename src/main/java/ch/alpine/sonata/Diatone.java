// code by jph
package ch.alpine.sonata;

/** match is ivory % 7 -> 0 1 2 3 4 5 6 */
public enum Diatone {
  C(0),
  D(2),
  E(4),
  F(5),
  G(7),
  A(9),
  B(11);

  /** in the range 0, ..., 11 */
  private final int white_delta;
  private final char lowerCase;

  private Diatone(int white) {
    this.white_delta = white;
    lowerCase = Character.toLowerCase(name().charAt(0));
  }

  public int white_delta() {
    return white_delta;
  }

  public char lowerCase() {
    return lowerCase;
  }
}
