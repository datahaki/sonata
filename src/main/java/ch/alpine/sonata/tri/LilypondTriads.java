// code by jph
package ch.alpine.sonata.tri;

import ch.alpine.sonata.DiatoneAlter;

/** chord format displaying absolute pitch */
public class LilypondTriads implements ChromaticTriads {
  public static String of(DiatoneAlter diatoneAlter) {
    int alter = diatoneAlter.alter().delta();
    return diatoneAlter.diatone().lowerCase() + (0 < alter ? "is" : "es").repeat(Math.abs(alter));
  }

  @Override
  public String major(DiatoneAlter tone) {
    return of(tone);
  }

  @Override
  public String minor(DiatoneAlter tone) {
    return of(tone) + ":m";
  }

  @Override
  public String diminished(DiatoneAlter tone) {
    return of(tone) + ":dim";
  }

  @Override
  public String majorSixth(DiatoneAlter tone) {
    return of(tone) + ":6";
  }

  @Override
  public String augmented(DiatoneAlter tone) {
    return of(tone) + ":aug";
  }

  @Override
  public String diminishedSeventh(DiatoneAlter tone) {
    return of(tone) + ":dim7";
  }
}
