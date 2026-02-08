// code by jph
package ch.alpine.sonata.enc.ly;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import ch.alpine.sonata.KeySignature;
import ch.alpine.sonata.enc.ScoreIO;
import ch.alpine.sonata.scr.Score;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.ext.HomeDirectory;

class LilypondPngFormatTest {
  @Test
  void test() {
    Score score = ScoreIO.read(Unprotect.resourcePath("/io/nvm/bwv1014_2.nvm"));
    assertEquals(score.keySignature, KeySignature.D);
    Path file = HomeDirectory.Music.path("test.png");
    ScoreIO.write(file, score);
  }
}
