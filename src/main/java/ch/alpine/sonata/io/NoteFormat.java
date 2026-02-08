// code by jph
package ch.alpine.sonata.io;

import ch.alpine.sonata.Note;
import ch.alpine.sonata.Tone;

public interface NoteFormat {
  Note parseNote(String string);

  String format(Note note);

  Tone parseTone(String string);
}
