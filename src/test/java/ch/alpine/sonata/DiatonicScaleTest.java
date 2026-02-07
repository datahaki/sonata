// code by jph
package ch.alpine.sonata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DiatonicScaleTest {
  @Test
  void test() {
    HeptatonicScale diatonicScale = HeptatonicScale.diatonic(3);
    assertEquals(KeySignature.A.diatonicScale(), diatonicScale);
    Note note = new Note(diatonicScale.getToneFromIvory(Ivory.from(35 + 2)), 2);
    assertEquals(note.toString(), "E5 2");
  }
}
