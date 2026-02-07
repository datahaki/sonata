// code by jph
package ch.alpine.sonata.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

import ch.alpine.sonata.Note;
import ch.alpine.sonata.Tone;

class NoteFormatTest {
  private static void _check(NoteFormat noteFormat) {
    Random random = new Random();
    for (Tone tone : Tone.all()) {
      Note note1 = new Note(tone, random.nextInt(1, 10));
      String string = noteFormat.format(note1);
      Note note2 = noteFormat.parseNote(string);
      assertEquals(note1, note2);
    }
  }

  @Test
  void test() {
    _check(NativeNoteFormat.INSTANCE);
  }
}
