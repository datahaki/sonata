package ch.alpine.sonata.enc.mxl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ch.alpine.sonata.enc.ScoreIO;
import ch.alpine.sonata.scr.Score;
import ch.alpine.tensor.Unprotect;

class MusicXmlReaderTest {
  @Test
  void test() {
    Score score = ScoreIO.read(Unprotect.path("/io/mxl/bwv1014_1.mxl"));
    assertEquals(score.staffPartition, "{2, 2, 1}");
  }
}
