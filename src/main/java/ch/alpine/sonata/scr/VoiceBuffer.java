// code by jph
package ch.alpine.sonata.scr;

import java.util.Map.Entry;

import ch.alpine.sonata.Dynamic;
import ch.alpine.sonata.FiguredBass;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.Ornament;
import ch.alpine.sonata.Torrent;
import ch.alpine.sonata.Voice;

public class VoiceBuffer {
  private Voice voice = new Voice();
  private int myTicks = -1; // no connection to previous instance
  private Note noteTie = null;

  public void put(Note note, boolean tie, int ticks) throws RuntimeException {
    if (noteTie == null)
      put(note, ticks);
    else { // tie
      if (myTicks != ticks)
        throw new RuntimeException("tie out of sync: " + myTicks + " vs. " + ticks + " (delta=" + (ticks - myTicks) + ")");
      if (!noteTie.tone().equals(note.tone()))
        throw new RuntimeException("tie pitch mismatch: " + noteTie.tone() + " vs. " + note.tone());
      Entry<Integer, Torrent> myEntry = voice.navigableMap.lowerEntry(ticks + 1);
      myEntry.getValue().last().setTicks(myEntry.getValue().last().ticks() + note.ticks());
      myTicks += note.ticks();
    }
    noteTie = tie ? note : null;
  }

  /** not necessarily on note beginning when reading general files.
   * for instance, a fermata might be postponed to a location after the note onset.
   * 
   * @param ornament
   * @param ticks */
  public void shake(int ticks, Ornament ornament) {
    voice.shake.put(ticks, ornament);
  }

  public void press(int ticks, Dynamic dynamics) {
    voice.press.put(ticks, dynamics);
  }

  public void lyric(String string, int ticks) {
    if (!string.isEmpty())
      voice.lyric.put(ticks, string);
  }

  public void fbass(int ticks, FiguredBass figuredBass) {
    voice.fbass.put(ticks, figuredBass);
  }

  private void put(Note note, int ticks) {
    final int prev = myTicks;
    if (prev < ticks) {
      voice.navigableMap.put(ticks, new Torrent());
      myTicks = ticks;
    } else //
    if (ticks < prev)
      throw new RuntimeException("note overlap, insert " + note + " at " + ticks + " but voice ends at " + prev);
    Entry<Integer, Torrent> myEntry = voice.navigableMap.lowerEntry(ticks + 1);
    myEntry.getValue().list.add(note);
    myTicks += note.ticks();
  }

  public boolean canAppendAt(int ticks) {
    return myTicks <= ticks;
  }

  public void untie() {
    noteTie = null;
  }

  public boolean isEmpty() {
    return voice.navigableMap.isEmpty();
  }

  public Voice getVoice() {
    return voice;
  }
}
