// code by jph
package ch.alpine.sonata;

/** the alternative name is Chroma */
public enum Natur {
  C,
  CD,
  D,
  DE,
  E,
  F,
  FG,
  G,
  GA,
  A,
  AB,
  B;

  public Natur ascend(int delta) {
    return fromPitch(ordinal() + delta);
  }

  /** @param pitch
   * @return */
  public static Natur fromPitch(int pitch) {
    return values()[Math.floorMod(pitch, 12)];
  }
}
