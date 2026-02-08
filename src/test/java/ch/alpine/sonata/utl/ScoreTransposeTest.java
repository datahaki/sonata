// code by jph
package ch.alpine.sonata.utl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import ch.alpine.sonata.Ivory;
import ch.alpine.sonata.KeySignature;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.Tone;
import ch.alpine.sonata.enc.ScoreIO;
import ch.alpine.sonata.scr.Score;
import ch.alpine.tensor.Unprotect;

class ScoreTransposeTest {
  @Test
  void testNvm() {
    Path file = Unprotect.resourcePath("/io/nvm/mm_fuge01.nvm");
    Score score = ScoreIO.read(file);
    assertEquals(score.voices(), 3);
    {
      List<Note> list = score.voices.get(0).allNotes().limit(2).toList();
      assertEquals(list.get(0).tone(), Tone.from(Ivory.from(35 + 5), 0));
      assertEquals(list.get(1).tone(), Tone.from(Ivory.from(35 + 6), 0));
    }
    ScoreTranspose.keySignature0(score.keySignature).scoreInstance(score);
    {
      List<Note> list = score.voices.get(0).allNotes().limit(2).toList();
      assertEquals(list.get(0).tone(), Tone.from(Ivory.from(35 + 2), 0));
      assertEquals(list.get(1).tone(), Tone.from(Ivory.from(35 + 3), 1));
      assertEquals(score.keySignature, KeySignature.C);
    }
  }
}
