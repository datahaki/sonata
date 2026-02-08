// code by jph
package ch.alpine.sonata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class PitchTest {
  @Test
  void test() {
    Set<Integer> set = new HashSet<>();
    for (int pitch = 0; pitch < 128; ++pitch) {
      set.add(Pitch.from(pitch, true).hashCode());
      set.add(Pitch.from(pitch, false).hashCode());
    }
    assertEquals(set.size(), 128 * 2);
  }
}
