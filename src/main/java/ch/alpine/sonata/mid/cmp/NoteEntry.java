// code by jph
package ch.alpine.sonata.mid.cmp;

import ch.alpine.midkit.Midi;
import ch.alpine.sonata.Note;

public class NoteEntry {
  public final int prev_ticks;
  public final Note prev_note;
  /** modifiable */
  public int next_ticks;
  public final Note next_note;
  // ---
  /** scaled between 0 ... 1 */
  public double velocity;

  public NoteEntry(int prev_ticks, Note prev_note, int next_ticks, Note next_note) {
    this.prev_ticks = prev_ticks;
    this.prev_note = prev_note;
    // System.out.println(this.prev_myNote);
    this.next_ticks = next_ticks;
    this.next_note = next_note;
  }

  public int getMidiVelocity() {
    return Midi.clip7bit((int) Math.round(velocity * 127));
  }
}
