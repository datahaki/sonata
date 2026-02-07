// code by jph
package ch.alpine.sonata.tri;

import ch.alpine.sonata.DiatoneAlter;

public interface ChromaticTriads {
  String major(DiatoneAlter diatoneAlter);

  String minor(DiatoneAlter diatoneAlter);

  String diminished(DiatoneAlter diatoneAlter);

  String majorSixth(DiatoneAlter diatoneAlter);

  String augmented(DiatoneAlter diatoneAlter);

  String diminishedSeventh(DiatoneAlter diatoneAlter);
}
