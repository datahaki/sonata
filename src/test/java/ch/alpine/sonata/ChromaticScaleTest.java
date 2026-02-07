// code by jph
package ch.alpine.sonata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ChromaticScaleTest {
  @Test
  void test() {
    DodecatonicScale chromaticScale = DodecatonicScale.chromatic(3, 4);
    assertEquals(KeySignature.A.dodecatonicScale(), chromaticScale);
  }

  @ParameterizedTest
  @EnumSource
  void testKeys(KeySignature keySignature) {
    DodecatonicScale chromaticScale = keySignature.dodecatonicScale();
    List<Tone> list = IntStream.range(1, 128).mapToObj(chromaticScale::getTone).distinct().toList();
    assertEquals(list.size(), 128 - 1);
  }
}
