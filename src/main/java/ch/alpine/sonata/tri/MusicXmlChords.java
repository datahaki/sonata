// code by jph
package ch.alpine.sonata.tri;

import ch.alpine.sonata.DiatoneAlter;

/** Chord labels of Sibelius match chord labels of Lilypond.
 * 
 * Chord labels of Finale 2011 are different: for instance, Cdim (Finale) instead of Co (Lilypond). */
public class MusicXmlChords implements ChromaticTriads {
  /** @param diatoneAlter
   * @param myText is not used to encourage application canonic chord labels
   * @param myKind
   * @return */
  static String of(DiatoneAlter diatoneAlter, String myText, String myKind) {
    StringBuilder myStringBuffer = new StringBuilder();
    myStringBuffer.append("<harmony>");
    myStringBuffer.append("<root>");
    myStringBuffer.append("<root-step>" + diatoneAlter.diatone() + "</root-step>");
    int delta = diatoneAlter.alter().delta();
    if (delta != 0)
      myStringBuffer.append("<root-alter>" + delta + "</root-alter>");
    myStringBuffer.append("</root>");
    // <kind text=\"" + myText + "\"> for non-canonic labeling in Finale 2011
    myStringBuffer.append("<kind>" + myKind + "</kind>");
    // myStringBuffer.append("<staff>1</staff>");
    myStringBuffer.append("</harmony>");
    return myStringBuffer.toString();
  }

  @Override
  public String major(DiatoneAlter tone) {
    return of(tone, "", "major");
  }

  @Override
  public String minor(DiatoneAlter tone) {
    return of(tone, "m", "minor");
  }

  @Override
  public String diminished(DiatoneAlter tone) {
    return of(tone, "o", "diminished");
  }

  @Override
  public String majorSixth(DiatoneAlter tone) {
    return of(tone, "6", "major-sixth");
  }

  @Override
  public String augmented(DiatoneAlter tone) {
    return of(tone, "+", "augmented");
  }

  @Override
  public String diminishedSeventh(DiatoneAlter tone) {
    return of(tone, "o7", "diminished-seventh");
  }
}
