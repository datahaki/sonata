// code by jph
package ch.alpine.sonata.utl;

import java.util.Objects;

import ch.alpine.sonata.KeySignature;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.Tone;
import ch.alpine.sonata.scr.Score;

/** Transpose */
public class ScoreTranspose {
  /** potentially unsafe since tones are not checked
   * 
   * @param keySignature
   * @return */
  public static ScoreTranspose keySignature0(KeySignature keySignature) {
    return new ScoreTranspose(KeySignature.C, -keySignature.tonic());
  }

  // ---
  final KeySignature keySignature;
  final int pitch_delta;

  /** @param keySignature target
   * @param pitch_delta */
  public ScoreTranspose(KeySignature keySignature, int pitch_delta) {
    this.keySignature = Objects.requireNonNull(keySignature);
    this.pitch_delta = pitch_delta;
  }

  public Tone apply(Tone tone) {
    return keySignature.dodecatonicScale().getTone(tone.pitch() + pitch_delta);
  }

  public void noteInstance(Note note) {
    note.setTone(apply(note.tone()));
  }

  public void scoreInstance(Score score) {
    score.keySignature = keySignature;
    score.allNotes().forEach(this::noteInstance);
    score.triad.entrySet().stream() //
        .forEach(entry -> score.triad.put(entry.getKey(), entry.getValue().transposed(pitch_delta)));
  }
}
