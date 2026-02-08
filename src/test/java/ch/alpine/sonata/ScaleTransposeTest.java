// code by jph
package ch.alpine.sonata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ScaleTransposeTest {
  @Test
  void test() {
    HeptatonicScale diatonicScale = KeySignature.D.diatonicScale();
    ScaleTranspose scaleTranspose = new ScaleTranspose(diatonicScale);
    assertEquals(scaleTranspose.transpose(Tone.from(35, 1), 0), Tone.from(35, 1));
    assertEquals(scaleTranspose.transpose(Tone.from(36, -1), 0), Tone.from(35, 1));
    assertEquals(scaleTranspose.transpose(Tone.from(35, 1), 1), Tone.from(36, 0));
    assertEquals(scaleTranspose.transpose(Tone.from(35, 1), -1), Tone.from(34, 0));
  }
}
