// code by jph
package ch.alpine.sonata.io;

import java.util.StringTokenizer;

import ch.alpine.sonata.Diatone;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.Tone;

public enum MusedataNoteFormat implements NoteFormat {
  INSTANCE;

  @Override
  public Note parseNote(String string) {
    StringTokenizer stringTokenizer = new StringTokenizer(string);
    return create(stringTokenizer.nextToken(), Integer.parseInt(stringTokenizer.nextToken()));
  }

  @Override
  public String format(Note note) {
    throw new UnsupportedOperationException();
  }

  /** @param string of type " C#-2 12 "
   * @return */
  /** @param string should be trimmed, i.e. of the form "C#-2"
   * @param ticks
   * @return */
  private Note create(String string, int ticks) { // input example: "C#-2", 12
    try {
      return new Note(parseTone(string), ticks);
    } catch (Exception exception) {
      System.err.println(string);
      throw new RuntimeException(exception);
    }
  }

  public static final int CENTER_OCTAVE = 1;

  /** @param string is trimmed, i.e. of the form "g#-2", or "G#-2"
   * @param ticks
   * @return */
  @Override
  public Tone parseTone(String string) { // throws Exception if conversion fails
    int index = 0;
    int alter = 0;
    string = string.toLowerCase(); // TODO TPF do characterwise
    boolean status = true;
    while (status)
      switch (string.charAt(++index)) {
      case '$': // $ native
        alter -= 2;
        break;
      case 'f': // f musedata
      case '&': // & native/guido
        --alter;
        break;
      case '#': // # musedata/guido
        ++alter;
        break;
      case 'x': // x native
        // case 'X': // as seen as ChromaticAscii!
        alter += 2;
        break;
      default:
        status = false;
        break;
      }
    int ivory = (CENTER_OCTAVE + Integer.parseInt(string.substring(index))) * 7 + //
        Diatone.valueOf(Character.toString(Character.toUpperCase(string.charAt(0)))).ordinal();
    return Tone.from(ivory, alter);
  }
}
