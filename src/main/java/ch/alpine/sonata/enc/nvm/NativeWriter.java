// code by jph
package ch.alpine.sonata.enc.nvm;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map.Entry;

import ch.alpine.bridge.lang.SI;
import ch.alpine.sonata.Dynamic;
import ch.alpine.sonata.FiguredBass;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.Ornament;
import ch.alpine.sonata.ScoreEntry;
import ch.alpine.sonata.Torrent;
import ch.alpine.sonata.Voice;
import ch.alpine.sonata.io.NativeNoteFormat;
import ch.alpine.sonata.scr.Score;
import ch.alpine.sonata.tri.Triad;
import sys.dat.Manager;
import sys.mat.IntRange;

class NativeWriter {
  void put(Path file, Score score) throws Exception {
    try (PrintWriter printWriter = new PrintWriter(Files.newBufferedWriter(file, Manager.CHARSET))) {
      printWriter.println("Score.Title=" + score.title);
      printWriter.println("Score.Comment=" + score.comment);
      printWriter.println("Score.KeySignature=" + score.keySignature.type());
      printWriter.println("Score.KeyMode=" + score.keyMode());
      printWriter.println("Score.StaffPartition=" + score.getStaffPartition());
      printWriter.println("Score.Quarter=" + score.quarter);
      printWriter.println("Score.Atom=" + score.atom);
      printWriter.println("Score.Division=" + score.division);
      printWriter.println("Score.Period=" + score.period);
      printWriter.println("Score.Bpm=" + SI.PER_MINUTE.magnitude(score.bpm));
      int count = 0;
      for (Voice voice : score.voices) {
        String key = "Voice[" + count + "].";
        printWriter.println(key + "Clef=" + voice.clef);
        printWriter.println(key + "MidiInstrument=" + voice.midiInstrument);
        // printWriter.println(key + "Channel=" + voice.channel);
        for (Entry<Integer, Torrent> entry : voice) {
          printWriter.println("! VoiceInstance " + count + " " + entry.getKey());
          for (Note note : entry.getValue())
            printWriter.println("! " + NativeNoteFormat.INSTANCE.format(note));
        }
        ++count;
      }
      for (Entry<Integer, Triad> entry : score.triad.entrySet())
        printWriter.println("Chord@" + entry.getKey() + "=" + entry.getValue());
      // ---
      // for (Entry<Integer, Hepta> entry : score.hepta.entrySet())
      // writer.write("Hepta@" + entry.getKey() + "=" + entry.getValue() + "\n");
      // ---
      for (int voice : IntRange.positive(score.voices())) {
        for (Entry<Integer, Ornament> entry : score.voices.get(voice).shake.entrySet()) {
          ScoreEntry scoreEntry = new ScoreEntry(entry.getKey(), voice);
          printWriter.println("Shake" + scoreEntry + "=" + entry.getValue());
        }
        // ---
        for (Entry<Integer, Dynamic> entry : score.voices.get(voice).press.entrySet()) {
          ScoreEntry scoreEntry = new ScoreEntry(entry.getKey(), voice);
          printWriter.println("Press" + scoreEntry + "=" + entry.getValue());
        }
        // ---
        for (Entry<Integer, String> entry : score.voices.get(voice).lyric.entrySet()) {
          ScoreEntry scoreEntry = new ScoreEntry(entry.getKey(), voice);
          printWriter.println("Lyric" + scoreEntry + "=" + entry.getValue());
        }
        // ---
        for (Entry<Integer, Integer> entry : score.voices.get(voice).motif.entrySet()) {
          ScoreEntry scoreEntry = new ScoreEntry(entry.getKey(), voice);
          printWriter.println("Motif" + scoreEntry + "=" + entry.getValue());
        }
        // ---
        for (Entry<Integer, FiguredBass> entry : score.voices.get(voice).fbass.entrySet()) {
          ScoreEntry scoreEntry = new ScoreEntry(entry.getKey(), voice);
          printWriter.println("Fbass" + scoreEntry + "=" + entry.getValue());
        }
      }
      // ---
      for (Entry<Integer, String> entry : score.text.entrySet())
        printWriter.println("Text@" + entry.getKey() + "=" + entry.getValue());
    }
  }
}
