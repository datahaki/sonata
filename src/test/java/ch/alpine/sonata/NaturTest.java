package ch.alpine.sonata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class NaturTest {
  @Test
  void testNatur() {
    Set<KeySignature> set = Stream.of(Natur.values()).map(Naturs::keySignature).collect(Collectors.toSet());
    assertEquals(set.size(), 12);
    assertFalse(set.contains(KeySignature.Cb));
    assertTrue(set.contains(KeySignature.Gb));
    assertFalse(set.contains(KeySignature.Fs));
    assertFalse(set.contains(KeySignature.Cs));
  }
}
