// code by jph
package ch.alpine.sonata.enc.ly;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import ch.alpine.sonata.enc.ScoreIO;
import ch.alpine.sonata.scr.Score;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.ext.HomeDirectory;

class TupletCacherTest {
  @Test
  void test() {
    Score score = ScoreIO.read(Unprotect.resourcePath("/io/nvm/bwv1017_3.nvm"));
    // System.out.println(score.quarter);
    Path file = HomeDirectory.Music.resolve("test.pdf");
    ScoreIO.write(file, score);
  }
}
