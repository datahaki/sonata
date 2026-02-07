// code by jph
package ch.alpine.sonata.tri;

import ch.alpine.sonata.DiatoneAlter;

/** identical names as in {@link MusicXmlChords} */
public enum EnglishChords implements ChromaticTriads {
  INSTANCE;

  @Override
  public String major(DiatoneAlter diatoneAlter) {
    return ChromaticUnicode.of(diatoneAlter) + " major";
  }

  @Override
  public String minor(DiatoneAlter diatoneAlter) {
    return ChromaticUnicode.of(diatoneAlter) + " minor";
  }

  @Override
  public String diminished(DiatoneAlter diatoneAlter) {
    return ChromaticUnicode.of(diatoneAlter) + " diminished";
  }

  @Override
  public String majorSixth(DiatoneAlter diatoneAlter) {
    return ChromaticUnicode.of(diatoneAlter) + " major-sixth";
  }

  @Override
  public String augmented(DiatoneAlter diatoneAlter) {
    return ChromaticUnicode.of(diatoneAlter) + " augmented";
  }

  @Override
  public String diminishedSeventh(DiatoneAlter diatoneAlter) {
    return ChromaticUnicode.of(diatoneAlter) + " diminished-seventh";
  }
}
