package ch.alpine.sonata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DiatoneAlterTest {
  @Test
  void test() {
    for (Diatone diatone : Diatone.values())
      for (Alter alter : Alter.values()) {
        DiatoneAlter diatoneAlter = DiatoneAlter.from(diatone, alter);
        assertEquals(diatoneAlter.diatone(), diatone);
        assertEquals(diatoneAlter.alter(), alter);
      }
  }

  @Test
  void testSize() {
    int size = 35;
    assertEquals(DiatoneAlter.all().size(), size);
    assertEquals(DiatoneAlter.all().stream().distinct().toList().size(), size);
    assertEquals(DiatoneAlter.all().stream().map(DiatoneAlter::toString).distinct().toList().size(), size);
    assertEquals(DiatoneAlter.all().stream().map(DiatoneAlter::toStringUnicode).distinct().toList().size(), size);
  }
}
