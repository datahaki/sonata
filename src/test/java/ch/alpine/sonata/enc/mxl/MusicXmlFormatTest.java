package ch.alpine.sonata.enc.mxl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.alpine.bridge.lang.SI;
import ch.alpine.midkit.MidiInstrument;
import ch.alpine.sonata.Alter;
import ch.alpine.sonata.Clef;
import ch.alpine.sonata.Diatone;
import ch.alpine.sonata.Dynamic;
import ch.alpine.sonata.KeySignature;
import ch.alpine.sonata.Meter;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.Tone;
import ch.alpine.sonata.Voice;
import ch.alpine.sonata.enc.ScoreIO;
import ch.alpine.sonata.scr.Score;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.chq.ExactScalarQ;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.ext.Serialization;

class MusicXmlFormatTest {
  static void _checkTest01(Score score) {
    assertEquals(score.quarter, 1);
    assertEquals(score.title, "MyTitle");
    assertEquals(ExactScalarQ.require(score.bpm), SI.PER_MINUTE.quantity(100));
    assertEquals(score.voices.size(), 2);
    assertEquals(score.getMeter(), new Meter(3, 4));
    assertEquals(score.keySignature, KeySignature.A);
    {
      Voice voice = score.voices.get(0);
      assertEquals(voice.clef, Clef.TREBLE);
      List<Note> list = voice.allNotes().toList();
      assertEquals(list.get(0).tone(), Tone.from(Diatone.C, Alter.SHARP, 5));
      assertEquals(list.get(1).tone(), Tone.from(Diatone.G, Alter.SHARP, 5));
      assertEquals(list.get(2).tone(), Tone.from(Diatone.C, Alter.SHARP, 6));
    }
    {
      Voice voice = score.voices.get(1);
      assertEquals(voice.clef, Clef.BASS);
      List<Note> list = voice.allNotes().toList();
      assertEquals(list.get(0).tone(), Tone.from(Diatone.E, Alter.NEUTRAL, 4));
      assertEquals(list.get(1).tone(), Tone.from(Diatone.D, Alter.NEUTRAL, 4));
      assertEquals(list.get(2).tone(), Tone.from(Diatone.C, Alter.SHARP, 4));
    }
  }

  @Test
  void test01() {
    Score score = ScoreIO.read(Unprotect.path("/io/mxl/test_score01.mxl"));
    _checkTest01(score);
  }

  @Test
  void testWriteRead(@TempDir Path folder) throws ClassNotFoundException, IOException {
    Path file = folder.resolve("temp.mxl");
    {
      Score score = ScoreIO.read(Unprotect.path("/io/mxl/test_score01.mxl"));
      ScoreIO.write(file, score);
    }
    Score score = ScoreIO.read(file);
    _checkTest01(score);
    Serialization.copy(score);
  }

  @Test
  void test02() throws ClassNotFoundException, IOException {
    Score score = ScoreIO.read(Unprotect.path("/io/mxl/test_score02.mxl"));
    assertEquals(score.voices.size(), 4);
    Serialization.copy(score);
  }

  @Test
  void test03() throws ClassNotFoundException, IOException {
    Score score = ScoreIO.read(Unprotect.path("/io/mxl/test_score03.mxl"));
    assertEquals(score.voices.size(), 4);
    {
      assertEquals(score.voices.get(0).press.size(), 1);
      assertEquals(score.voices.get(0).press.get(3), Dynamic.PPP);
      assertEquals(score.voices.get(0).midiInstrument, MidiInstrument.VIOLIN);
    }
    {
      assertEquals(score.voices.get(1).press.size(), 1);
      assertEquals(score.voices.get(1).press.get(0), Dynamic.F);
      assertEquals(score.voices.get(1).midiInstrument, MidiInstrument.VIOLA);
    }
    {
      assertEquals(score.voices.get(2).press.size(), 1);
      assertEquals(score.voices.get(2).press.get(6), Dynamic.FFF);
      assertEquals(score.voices.get(2).midiInstrument, MidiInstrument.CELLO);
    }
    {
      assertEquals(score.voices.get(3).press.size(), 1);
      assertEquals(score.voices.get(3).press.get(3), Dynamic.MF);
      assertEquals(score.voices.get(3).midiInstrument, MidiInstrument.CONTRA_BASS);
    }
    Serialization.copy(score);
  }

  private static void _checkBwv1014_2(Score score) {
    assertEquals(score.voices(), 3);
    assertEquals(score.voices.get(0).midiInstrument, MidiInstrument.VIOLIN);
    assertEquals(score.voices.get(1).midiInstrument, MidiInstrument.GRAND_PIANO);
  }

  @TempDir
  File folder;

  @Test
  void testMidiInstr() throws Exception {
    Path xmlFile; // = new File(folder, "bwv1014_2.xml");
    xmlFile = HomeDirectory.Downloads.resolve("bwv1014_2.xml");
    {
      Path file = Unprotect.path("/io/nvm/bwv1014_2.nvm");
      Score score = ScoreIO.read(file);
      _checkBwv1014_2(score);
      ScoreIO.write(xmlFile, score);
    }
    Score score = ScoreIO.read(xmlFile);
    _checkBwv1014_2(score);
  }
}
