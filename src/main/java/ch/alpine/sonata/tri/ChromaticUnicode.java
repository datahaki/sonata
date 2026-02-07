// code by jph
package ch.alpine.sonata.tri;

import ch.alpine.sonata.DiatoneAlter;

public class ChromaticUnicode extends ChromaticText {
  public static String of(DiatoneAlter diatoneAlter) {
    return diatoneAlter.toStringUnicode().toUpperCase();
  }

  @Override
  String getString(DiatoneAlter diatoneAlter) {
    return of(diatoneAlter); // .toIvoryAccidentalUnicode().toUpperCase();
  }

  @Override
  char oChar() {
    return '\u00B0'; // DEGREE SIGN
  }

  @Override
  char six() {
    return '6'; // SUPERSCRIPT SIX 2076
  }

  @Override
  char seven() {
    return '7'; // SUPERSCRIPT SEVEN 2077
  }
}
