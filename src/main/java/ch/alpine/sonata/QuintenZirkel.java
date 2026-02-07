// code by jph
package ch.alpine.sonata;

/** lists representatives of pitches between 0, 1, ..., 11 with all possible accidentals.
 * useful to construct DiatonicScale and ChromaticScale */
public enum QuintenZirkel {
  // & and $ are fine, but triple flats not allowed.
  // --- $
  FN2(Diatone.F, -2), // -08 04 F$ ! (0)
  CN2(Diatone.C, -2), // -07 11 C$ !
  GN2(Diatone.G, -2), // -13 05 G$ (0)
  DN2(Diatone.D, -2), // -12 00 D$
  AN2(Diatone.A, -2), // -11 07 A$
  EN2(Diatone.E, -2), // -10 02 E$
  BN2(Diatone.B, -2), // -09 09 B$
  // --- &
  FN1(Diatone.F, -1), // -08 04 F& ! (7)
  CN1(Diatone.C, -1), // -07 11 C& !
  GN1(Diatone.G, -1), // -06 06 G& [min for chromatic scale for type 0 bias 0]
  DN1(Diatone.D, -1), // -05 01 D&
  AN1(Diatone.A, -1), // -04 08 A&
  EN1(Diatone.E, -1), // -03 03 E&
  BN1(Diatone.B, -1), // -02 10 B&
  // ---
  FP0(Diatone.F, 0), // -01 05 F (14) [max for chromatic scale for type 0 bias 5]
  CP0(Diatone.C, 0), // 00 00 C (15 = middle)
  GP0(Diatone.G, 0), // 01 07 G
  DP0(Diatone.D, 0), // 02 02 D
  AP0(Diatone.A, 0), // 03 09 A
  EP0(Diatone.E, 0), // 04 04 E
  BP0(Diatone.B, 0), // 05 11 B [min for chromatic scale for type 0 bias 0]
  // --- #
  FP1(Diatone.F, 1), // 06 06 F#
  CP1(Diatone.C, 1), // 07 01 C#
  GP1(Diatone.G, 1), // 08 08 G#
  DP1(Diatone.D, 1), // 09 03 D#
  AP1(Diatone.A, 1), // 10 10 A# [max for chromatic scale for type 0 bias 5]
  EP1(Diatone.E, 1), // 11 05 E# !
  BP1(Diatone.B, 1), // 12 00 B# !
  // --- x
  FP2(Diatone.F, 2), // 13 07 Fx
  CP2(Diatone.C, 2), // 14 02 Cx
  GP2(Diatone.G, 2), // 15 09 Gx
  DP2(Diatone.D, 2), // 16 04 Dx
  AP2(Diatone.A, 2), // 17 11 Ax
  EP2(Diatone.E, 2), // 11 05 Ex !
  BP2(Diatone.B, 2); // 12 00 Bx !
  // # and x are fine, but triple sharp not allowed.

  private static final int MIDDLE = 15;
  // ---
  private final DiatoneAlter diatoneAlter;

  QuintenZirkel(Diatone diatone, int alter) {
    diatoneAlter = DiatoneAlter.from(diatone, Alter.fromDelta(alter));
  }

  public DiatoneAlter diatoneAlter() {
    return diatoneAlter;
  }

  public static DiatoneAlter typeToDiatoneAlter(int type) {
    return values()[MIDDLE + type].diatoneAlter;
  }
}
