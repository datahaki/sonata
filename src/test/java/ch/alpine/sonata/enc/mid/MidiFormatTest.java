package ch.alpine.sonata.enc.mid;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.alpine.midkit.MidiInstrument;
import ch.alpine.midkit.MidiListing;
import ch.alpine.sonata.enc.ScoreIO;
import ch.alpine.sonata.scr.Score;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.ext.HomeDirectory;

class MidiFormatTest {
  @TempDir
  Path folder;

  @Test
  void testMidi() {
    Path file = Unprotect.path("/io/mid/bwv1087_01.mid");
    Score score = ScoreIO.read(file);
    assertEquals(score.voices(), 2);
  }

  private static void _checkBwv1014_2(Score score) {
    assertEquals(score.voices(), 3);
    assertEquals(score.voices.get(0).midiInstrument, MidiInstrument.VIOLIN);
    assertEquals(score.voices.get(1).midiInstrument, MidiInstrument.GRAND_PIANO);
  }

  @Test
  void testMidiInstr() throws Exception {
    final Path midiFile = folder.resolve("bwv1014_2.mid");
    {
      Path file = Unprotect.path("/io/nvm/bwv1014_2.nvm");
      Score score = ScoreIO.read(file);
      _checkBwv1014_2(score);
      ScoreIO.write(midiFile, score);
      MidiListing midiListing = new MidiListing(midiFile);
      midiListing.exportToHtml(HomeDirectory.Downloads.resolve("bwv1014_2.html"));
    }
    Score score = ScoreIO.read(midiFile);
    // System.out.println(midiFile);
    _checkBwv1014_2(score); // might fail if metronome is added
  }
}
