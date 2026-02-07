package ch.alpine.sonata.enc.nvm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.alpine.bridge.lang.SI;
import ch.alpine.midkit.MidiInstrument;
import ch.alpine.sonata.enc.ScoreIO;
import ch.alpine.sonata.scr.Score;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.chq.ExactScalarQ;
import ch.alpine.tensor.ext.Serialization;

class NativeFormatTest {
  @Test
  void testNvm() throws ClassNotFoundException, IOException {
    Path file = Unprotect.path("/io/nvm/bwv0528_2.nvm");
    Score score = ScoreIO.read(file);
    assertEquals(score.voices(), 3);
    Serialization.copy(score);
  }

  @TempDir
  Path folder;

  @Test
  void testInstr() {
    Score score = ScoreIO.read(Unprotect.path("/io/nvm/bwv0528_2.nvm"));
    ExactScalarQ.require(score.bpm);
    assertEquals(score.bpm, SI.PER_MINUTE.quantity(99));
    assertEquals(score.voices.get(0).midiInstrument, MidiInstrument.GRAND_PIANO);
    score.voices.get(0).midiInstrument = MidiInstrument.VIOLIN;
    score.voices.get(1).midiInstrument = MidiInstrument.TRUMPET;
    score.voices.get(2).midiInstrument = MidiInstrument.NYLON_GUITAR;
    Path file = folder.resolve("test.nvm");
    score.bpm = SI.PER_MINUTE.quantity(117);
    ScoreIO.write(file, score);
    Score score2 = ScoreIO.read(file);
    assertEquals(score2.voices.get(0).midiInstrument, MidiInstrument.VIOLIN);
    assertEquals(score2.voices.get(1).midiInstrument, MidiInstrument.TRUMPET);
    assertEquals(score2.voices.get(2).midiInstrument, MidiInstrument.NYLON_GUITAR);
    ExactScalarQ.require(score2.bpm);
    assertEquals(score2.bpm, SI.PER_MINUTE.quantity(117));
  }
}
