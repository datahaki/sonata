// code by jph
package ch.alpine.sonata;

import ch.alpine.sonata.utl.TpfStatics;

/** represents key signature in sequence of increasing fifth's */
public enum KeySignature {
  Cb, // 7 flats == 5 sharps
  Gb, // 6 flats
  Db, // 5 flats
  Ab, // 4 flats
  Eb, // 3 flats
  Bb, // 2 flats
  F, // 1 flat
  C, // 0
  G, // 1 sharp
  D, // 2 sharps
  A, // 3 sharps
  E, // 4 sharps
  B, // 5 sharps
  Fs, // 6 sharps
  Cs; // 7 sharps

  private static final int MIDDLE = 7;

  /** @param type between -7, and +7 basically the number of sharps (positive), or flats (negative)
   * @return */
  public static KeySignature fromType(int type) {
    return values()[type + MIDDLE];
  }

  // ---
  /** -7, -6, ..., -1, 0, 1, ..., 6, 7 */
  private final int type;
  /** -6, -5, ..., 5 */
  private final int tonic;
  private final HeptatonicScale heptatonicScale;
  private final DodecatonicScale dodecatonicScale;

  private KeySignature() {
    this.type = ordinal() - MIDDLE;
    tonic = TpfStatics.invert(type);
    heptatonicScale = HeptatonicScale.diatonic(type);
    dodecatonicScale = DodecatonicScale.chromatic(type, 4);
  }

  // TODO TPF rename to fifth!?
  public int type() {
    return type;
  }

  // TODO TPF strictly should not be "int"
  public int tonic() {
    return tonic;
  }

  public HeptatonicScale diatonicScale() {
    return heptatonicScale;
  }

  public DodecatonicScale dodecatonicScale() {
    return dodecatonicScale;
  }

  public String getAccidentalsUnicode() {
    if (type() == 0)
      return "";
    return 0 < type() //
        ? type() + Alter.sharpUnicode
        : -type() + Alter.flatUnicode;
  }

  public String toStringUnicode() {
    return QuintenZirkel.typeToDiatoneAlter(type()).toStringUnicode().toUpperCase();
  }
}
