// code by jph
package ch.alpine.sonata;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.alpine.sonata.utl.TpfStatics;

public enum Naturs {
  ;
  private static final Map<Natur, KeySignature> NATUR_KEY = Stream.of(Natur.values()) //
      .collect(Collectors.toMap(n -> n, n -> KeySignature.fromType(TpfStatics.invert(n.ordinal()))));

  /** Careful: mapping is not surjective!
   * in particular, the values Cb, Fs, Cs are never returned
   * 
   * @param natur
   * @return between Gb and B */
  public static KeySignature keySignature(Natur natur) {
    return NATUR_KEY.get(natur);
  }
  //
  // public static Natur from(DiatoneAlter diatoneAlter) {
  //
  // return Natur.fromPitch(diatoneAlter.diatone().white_delta() + diatoneAlter.alter().delta());
  // }
}
