// code by jph
package ch.alpine.sonata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import ch.alpine.tensor.ext.Integers;
import ch.alpine.tensor.ext.Serialization;

class ToneTest {
  @Test
  void testEquals() throws ClassNotFoundException, IOException {
    for (Ivory ivory : Ivory.all())
      for (int alter = -2; alter <= 2; ++alter)
        assertEquals(Tone.from(ivory.ivory(), alter), Serialization.copy(Tone.from(ivory.ivory(), alter)));
  }

  @Test
  void testSpecific() {
    Tone tone = Tone.from(35, 0);
    assertEquals(tone.diatoneAlter().alter().delta(), 0);
    assertEquals(tone.pitch(), 60);
    assertEquals(tone.diatoneAlter().diatone(), Diatone.C);
    String string = tone.toString();
    assertEquals(string, "C5");
  }

  @Test
  void testSpecific2() {
    Tone tone = Tone.from(35, 1);
    assertEquals(tone.diatoneAlter().alter().delta(), 1);
    assertEquals(tone.pitch(), 61);
    assertEquals(tone.diatoneAlter().diatone(), Diatone.C);
    String string = tone.toString();
    assertEquals(string, "C#5");
    // Tone t2 = NativeNoteFormat.INSTANCE.parseTone(string);
    // assertEquals(tone, t2);
  }

  @Test
  void testSpecific3() {
    Tone tone = Tone.from(35, -1);
    assertEquals(tone.diatoneAlter().alter().delta(), -1);
    assertEquals(tone.pitch(), 59);
    assertEquals(tone.diatoneAlter().diatone(), Diatone.C);
    String string = tone.toString();
    assertEquals(string, "C&5");
    // Tone t2 = NativeNoteFormat.INSTANCE.parseTone(string);
    // assertEquals(tone, t2);
  }

  @Test
  void testSize() {
    Integers.requireEquals(Tone.all().size(), 5 * Ivory.all().size());
  }
}
