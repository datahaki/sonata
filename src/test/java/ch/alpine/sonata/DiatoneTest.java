// code by jph
package ch.alpine.sonata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class DiatoneTest {
  @Test
  void testSimple() {
    List<Integer> list1 = Stream.of(Diatone.values()).map(Diatone::white_delta).toList();
    List<Integer> list2 = Stream.of(Diatone.values()).map(Diatone::white_delta).sorted().distinct().toList();
    assertEquals(list1, list2);
    assertEquals(list2.size(), Diatone.values().length);
  }
}
