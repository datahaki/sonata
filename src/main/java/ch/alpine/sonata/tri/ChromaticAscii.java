// code by jph
package ch.alpine.sonata.tri;

import ch.alpine.sonata.DiatoneAlter;

public class ChromaticAscii extends ChromaticText {
  @Override
  String getString(DiatoneAlter diatoneAlter) {
    return diatoneAlter.toString().toUpperCase();
    // return myNote.getMatch().upperCase + myNote.getAccidental();
  }

  @Override
  char oChar() {
    return 'o';
  }

  @Override
  char six() {
    return '6';
  }

  @Override
  char seven() {
    return '7';
  }
}
