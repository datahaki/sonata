// code by jph
package ch.alpine.sonata.utl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import ch.alpine.sonata.Alter;
import ch.alpine.sonata.Clef;
import ch.alpine.sonata.Diatone;
import ch.alpine.sonata.KeySignature;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.Tone;
import ch.alpine.sonata.Voice;
import ch.alpine.sonata.enc.ScoreIO;
import ch.alpine.sonata.scr.Score;
import ch.alpine.tensor.Unprotect;

class NormalizationTest {
  @Test
  void testXml() {
    Path file = Unprotect.resourcePath("/io/mxl/test_score01.mxl");
    Score score = ScoreIO.read(file);
    assertEquals(score.keySignature, KeySignature.A);
    Normalization.keySignature0(score);
    assertEquals(score.keySignature, KeySignature.C);
    {
      Voice voice = score.voices.get(0);
      assertEquals(voice.clef, Clef.TREBLE);
      List<Note> list = voice.allNotes().toList();
      assertEquals(list.get(0).tone(), Tone.from(Diatone.E, Alter.NEUTRAL, 5));
      assertEquals(list.get(1).tone(), Tone.from(Diatone.B, Alter.NEUTRAL, 5));
      assertEquals(list.get(2).tone(), Tone.from(Diatone.E, Alter.NEUTRAL, 6));
    }
    {
      Voice voice = score.voices.get(1);
      assertEquals(voice.clef, Clef.BASS);
      List<Note> list = voice.allNotes().toList();
      assertEquals(list.get(0).tone(), Tone.from(Diatone.G, Alter.NEUTRAL, 4));
      assertEquals(list.get(1).tone(), Tone.from(Diatone.F, Alter.NEUTRAL, 4));
      assertEquals(list.get(2).tone(), Tone.from(Diatone.E, Alter.NEUTRAL, 4));
    }
  }
}
