// code by jph
package ch.alpine.sonata.hrm;

import ch.alpine.sonata.Natur;
import ch.alpine.sonata.tri.Chord;

interface AbstractTheory {
  Chord major(Natur pitch);

  Chord minor(Natur pitch);

  Chord augmented(Natur pitch);

  Chord diminished(Natur pitch);

  Chord majorSixth(Natur pitch);

  Chord diminishedSeventh(Natur pitch);
}
