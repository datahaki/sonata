// code by jph
package ch.alpine.sonata;

import org.junit.jupiter.api.Test;

class DivisionTest {
  @Test
  void testSimple() {
    // System.out.println(fallback.durationBucket(1));
    Division division = Division.FALLBACK;
    for (int ticks = 0; ticks < division.measure; ++ticks) {
      System.out.println(String.format( //
          "%2d %2d %2d %2d", //
          ticks, //
          division.depthAt(ticks), //
          division.log(ticks), //
          division.maxDepth));
    }
  }
}
