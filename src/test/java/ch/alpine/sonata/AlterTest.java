package ch.alpine.sonata;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AlterTest {
  @Test
  void test() {
    assertTrue(Alter.NEUTRAL.isNeutral());
    assertFalse(Alter.FLAT.isNeutral());
  }

  @Test
  void testFail() {
    assertThrows(Exception.class, () -> Alter.fromDelta(-3));
    assertThrows(Exception.class, () -> Alter.fromDelta(+3));
  }
}
