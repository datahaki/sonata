// code by jph
package ch.alpine.sonata;

import java.io.Serializable;
import java.util.Objects;

import ch.alpine.tensor.ext.Integers;

/** for note constructors see also Scale
 * 
 * for comparison use NoteComparator */
// TODO NOTATION make class immutable
public final class Note implements Serializable {
  private Tone tone;
  private int ticks; // duration

  public Note(Tone tone, int ticks) {
    this.tone = Objects.requireNonNull(tone);
    this.ticks = Integers.requirePositive(ticks);
  }

  public Tone tone() {
    return tone;
  }

  public void setTone(Tone tone) {
    this.tone = Objects.requireNonNull(tone);
  }

  public int ticks() {
    return ticks;
  }

  public void setTicks(int ticks) {
    this.ticks = Integers.requirePositive(ticks);
  }

  public Note cloneTransposeByOctave(int octaves) {
    return new Note(tone.transposeByOctaves(octaves), ticks);
  }

  public Note copy() {
    return new Note(tone, ticks);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tone, ticks);
  }

  @Override
  public boolean equals(Object object) {
    return object instanceof Note note //
        && tone.equals(note.tone) //
        && ticks == note.ticks;
  }

  @Override
  public String toString() {
    return tone + " " + ticks;
  }
}
