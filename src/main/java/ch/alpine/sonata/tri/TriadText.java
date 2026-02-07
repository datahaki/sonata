// code by jph
package ch.alpine.sonata.tri;

import ch.alpine.sonata.KeyMode;
import ch.alpine.sonata.KeySignature;

public enum TriadText {
  HARMONIC,
  CHROMATIC;

  public TriadFormat getTriadFormat(KeySignature keySignature, KeyMode keyMode) {
    return switch (this) {
    case HARMONIC -> new SheafFormat(keySignature, keyMode);
    case CHROMATIC -> new ChromaticFormat(keySignature, new ChromaticUnicode());
    default -> throw new IllegalStateException();
    };
  }
}
