// code by jph
package ch.alpine.sonata.hrm;

import ch.alpine.sonata.KeyMode;
import ch.alpine.sonata.KeySignature;

public enum TheoryType {
  UNIFORM,
  ADAPTED;

  public Theory getTheory(KeySignature keySignature, KeyMode keyMode) {
    return switch (this) {
    case ADAPTED -> Theory.getChromaticTheory(keySignature);
    default -> Theory.uniform;
    };
  }
}
