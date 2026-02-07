package ch.alpine.sonata;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class NoteTest {
  @Test
  void test() {
    Note note = new Note(Tone.from(35, 1), 1);
  }

  @Test
  void testFail() {
    Tone tone = Tone.from(35, 1);
    assertThrows(Exception.class, () -> new Note(tone, 0));
    assertThrows(Exception.class, () -> new Note(null, 1));
  }
}
