// code by jph
package ch.alpine.sonata.seq;

import java.nio.file.Path;

import javax.sound.midi.Sequence;

import ch.alpine.midkit.Midi;
import ch.alpine.sonata.scr.Score;

public enum MidiMiscellaneous {
  ;
  // only used in zip format
  public static void bulk(Path root, String string, Score score) {
    try {
      int center = 480 / score.quarter;
      // for (int factor : new int[] { center * 4 / 5, center, center * 5 / 4 }) {
      for (int factor : new int[] { center }) {
        // Sequence mySequence = MidiSequence.createFrom(new RealtimeSequence(myScore, factor));
        Sequence sequence = new VoicedSequence(score, score.voices).getSequence();
        Path file = root.resolve(String.format("%s-%03d.mid", string, factor));
        Midi.write(sequence, file);
        // System.out.println(myFile);
      }
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }
}
