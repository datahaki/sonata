package ch.alpine.sonata.seq;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SimpleChannelMapTest {
  @Test
  void test() {
    assertEquals(SimpleChannelMap.skipDrums(8), 8);
    assertEquals(SimpleChannelMap.skipDrums(9), 10);
    assertEquals(SimpleChannelMap.skipDrums(10), 11);
  }
}
