// code by jph
package ch.alpine.sonata.tri;

import ch.alpine.sonata.DiatoneAlter;

/** inspired by Lilypond format and documentation
 * used in gui */
abstract class ChromaticText implements ChromaticTriads {
  @Override
  public String major(DiatoneAlter diatoneAlter) {
    return getString(diatoneAlter);
  }

  @Override
  public String minor(DiatoneAlter diatoneAlter) {
    return getString(diatoneAlter) + 'm';
  }

  @Override
  public String diminished(DiatoneAlter diatoneAlter) {
    // TODO TPF offer alternative G7 instead of Bo
    return getString(diatoneAlter) + oChar();
  }

  @Override
  public String majorSixth(DiatoneAlter diatoneAlter) {
    return getString(diatoneAlter) + six(); // major sixth
  }

  @Override
  public String augmented(DiatoneAlter diatoneAlter) {
    return getString(diatoneAlter) + '+';
  }

  @Override
  public String diminishedSeventh(DiatoneAlter diatoneAlter) {
    return getString(diatoneAlter) + oChar() + "" + seven();
  }

  abstract String getString(DiatoneAlter tone);

  abstract char oChar();

  abstract char six();

  abstract char seven();
}
