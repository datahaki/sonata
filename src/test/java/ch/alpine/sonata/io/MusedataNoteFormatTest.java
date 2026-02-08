// code by jph
package ch.alpine.sonata.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

import ch.alpine.sonata.DiatoneAlter;
import ch.alpine.sonata.Ivory;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.Tone;

class MusedataNoteFormatTest {
  private static void _check(NoteFormat noteFormat) {
    Random random = new Random();
    for (Tone tone : Tone.all()) {
      // System.out.println(tone);
      DiatoneAlter diatoneAlter = tone.diatoneAlter();
      Ivory ivory = tone.ivory();
      // System.out.println("---");
      // System.out.println("ivory1="+ivory.ivory());
      int octave = ivory.ivory() / 7;
      int ticks = random.nextInt(1, 10);
      Note note1 = new Note(tone, ticks);
      String string = diatoneAlter.toString() + "" + (octave - 1) + " " + ticks;
      Note note2 = noteFormat.parseNote(string);
      // System.out.println("ivory2="+note2.tone().ivory().ivory());
      assertEquals(note1, note2);
    }
  }

  @Test
  void test() {
    _check(MusedataNoteFormat.INSTANCE);
  }
}
