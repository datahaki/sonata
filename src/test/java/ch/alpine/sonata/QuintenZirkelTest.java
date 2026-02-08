// code by jph
package ch.alpine.sonata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class QuintenZirkelTest {
  @Test
  void test() {
    assertEquals(QuintenZirkel.typeToDiatoneAlter(0), DiatoneAlter.from(Diatone.C, Alter.NEUTRAL));
    assertEquals(QuintenZirkel.typeToDiatoneAlter(1), DiatoneAlter.from(Diatone.G, Alter.NEUTRAL));
  }
}
