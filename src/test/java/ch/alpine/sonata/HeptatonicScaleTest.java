// code by jph
package ch.alpine.sonata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class HeptatonicScaleTest {
  @ParameterizedTest
  @EnumSource
  void test(KeySignature keySignature) {
    HeptatonicScale diatonicScale = keySignature.diatonicScale();
    for (Ivory ivory : Ivory.all()) {
      Tone tone = diatonicScale.getToneFromIvory(ivory);
      assertEquals(tone.ivory(), ivory);
    }
  }
}
