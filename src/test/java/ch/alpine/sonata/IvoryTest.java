// code by jph
package ch.alpine.sonata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import ch.alpine.tensor.ext.Serialization;
import sys.mat.IntRange;

class IvoryTest {
  @Test
  void testSimple() {
    for (int ivory = 0; ivory < Ivory.SIZE; ivory += 7) {
      assertEquals(Ivory.from(ivory + 0).diatone(), Diatone.C);
      assertEquals(Ivory.from(ivory + 1).diatone(), Diatone.D);
    }
  }

  @Test
  void testEquals() throws ClassNotFoundException, IOException {
    for (int ivory : IntRange.positive(Ivory.SIZE))
      assertEquals(Ivory.from(ivory), Serialization.copy(Ivory.from(ivory)));
    for (int ivory : IntRange.positive(Ivory.SIZE))
      assertSame(Ivory.from(ivory), Ivory.from(ivory));
  }

  @Test
  void testSpecific() {
    Ivory ivory = Ivory.from(35);
    assertEquals(ivory.white(), 60);
    assertEquals(ivory.octave(), 5);
    assertEquals(ivory.diatone(), Diatone.C);
    assertEquals(ivory.ivory(), 35);
  }

  @Test
  void testSpecific2() {
    Ivory ivory = Ivory.from(36);
    assertEquals(ivory.white(), 62);
    assertEquals(ivory.octave(), 5);
    assertEquals(ivory.diatone(), Diatone.D);
    assertEquals(ivory.ivory(), 36);
  }

  @Test
  void testIv() {
    int count = 0;
    for (Ivory ivory : Ivory.all())
      assertEquals(ivory.ivory(), count++);
    assertEquals(Ivory.SIZE, count);
  }

  @Test
  void testFail() {
    assertThrows(Exception.class, () -> Ivory.all().clear());
  }
}
