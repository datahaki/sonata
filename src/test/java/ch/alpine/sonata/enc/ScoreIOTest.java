// code by jph
package ch.alpine.sonata.enc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import ch.alpine.bridge.lang.SI;
import ch.alpine.sonata.scr.Score;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.chq.ExactScalarQ;

class ScoreIOTest {
  @Test
  void testMxl() {
    Path file = Unprotect.resourcePath("/io/mxl/bwv0525_1.mxl");
    Score score = ScoreIO.read(file);
    assertEquals(score.voices(), 3);
  }

  @Test
  void testNvm() {
    Path file = Unprotect.resourcePath("/io/nvm/bwv0528_2.nvm");
    Score score = ScoreIO.read(file);
    assertEquals(score.voices(), 3);
  }

  @Test
  void testSt1() {
    Path file = Unprotect.resourcePath("/io/st1/twv0098_2.st1");
    Score score = ScoreIO.read(file);
    assertEquals(score.voices(), 4);
  }

  @Test
  void testSt2() {
    Path file = Unprotect.resourcePath("/io/st2/twv41a5a_2.st2");
    Score score = ScoreIO.read(file);
    assertEquals(score.voices(), 2);
  }

  @Test
  void testKrn1() {
    Path file = Unprotect.resourcePath("/io/krn/alv01_05.krn");
    Score score = ScoreIO.read(file);
    assertEquals(score.voices(), 3);
  }

  @Test
  void testKrn2() {
    Path file = Unprotect.resourcePath("/io/krn/bwv978-2.krn");
    Score score = ScoreIO.read(file);
    assertEquals(score.voices(), 9);
  }

  @Test
  void testMidi() {
    Path file = Unprotect.resourcePath("/io/mid/bwv1087_01.mid");
    Score score = ScoreIO.read(file);
    assertEquals(score.voices(), 2);
    ExactScalarQ.require(score.bpm);
    assertEquals(score.bpm, SI.PER_MINUTE.quantity(60));
  }
}
